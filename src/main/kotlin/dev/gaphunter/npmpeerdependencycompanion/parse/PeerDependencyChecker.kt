package dev.gaphunter.npmpeerdependencycompanion.parse

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import dev.gaphunter.npmpeerdependencycompanion.model.PeerDependencyIssue
import dev.gaphunter.npmpeerdependencycompanion.model.PeerDependencyProblem
import dev.gaphunter.npmpeerdependencycompanion.semver.SemverRange

/**
 * Reads `"peerDependencies"` from an already-parsed `package.json`
 * ([JsonFile]) via the bundled JSON plugin's real PSI, resolves the
 * installed version from the sibling `node_modules/<pkg>/package.json`
 * (the same resolution npm itself uses), and flags either "not
 * installed at all" or "installed but outside the declared range" --
 * same "don't reinvent a parser for a format the platform already
 * parses correctly" principle proven in `unused-npm-script-companion`.
 *
 * **v0.1 scope, stated honestly:** the range checker ([SemverRange])
 * covers `^`/`~`/comparison/exact/`*` ranges -- an OR range (`||`) or a
 * hyphen range is treated as "can't evaluate" and silently skipped,
 * never reported as a false failure. A monorepo/workspace layout where
 * `node_modules` lives at a different level than this `package.json`
 * isn't resolved (no hoisting simulation) -- honestly returns
 * NOT_INSTALLED in that case, a possible false positive documented in
 * the README.
 */
object PeerDependencyChecker {

    fun findIssues(file: JsonFile): List<PeerDependencyIssue> {
        val root = file.topLevelValue as? JsonObject ?: return emptyList()
        val peerDepsProperty = root.findProperty("peerDependencies") ?: return emptyList()
        val peerDepsObject = peerDepsProperty.value as? JsonObject ?: return emptyList()

        val packageJsonDir = file.virtualFile?.parent ?: return emptyList()
        val nodeModules = packageJsonDir.findChild("node_modules")

        return peerDepsObject.propertyList.mapNotNull { property ->
            toIssue(property, nodeModules, file)
        }
    }

    private fun toIssue(property: JsonProperty, nodeModules: VirtualFile?, contextFile: JsonFile): PeerDependencyIssue? {
        val nameElement = leafOf(property.nameElement as? JsonStringLiteral ?: return null)
        val rangeLiteral = property.value as? JsonStringLiteral ?: return null
        val packageName = property.name
        val range = rangeLiteral.value

        val installedVersion = resolveInstalledVersion(nodeModules, packageName, contextFile)
            ?: return PeerDependencyIssue(packageName, range, null, PeerDependencyProblem.NOT_INSTALLED, nameElement)

        val parsedInstalled = SemverRange.parseVersion(installedVersion) ?: return null
        val satisfied = SemverRange.satisfies(parsedInstalled, range) ?: return null
        if (satisfied) return null

        return PeerDependencyIssue(packageName, range, installedVersion, PeerDependencyProblem.VERSION_MISMATCH, nameElement)
    }

    /** Descends to a real leaf PSI element -- LineMarkerInfo must never anchor on a composite node. */
    private fun leafOf(element: com.intellij.psi.PsiElement): com.intellij.psi.PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }

    private fun resolveInstalledVersion(nodeModules: VirtualFile?, packageName: String, contextFile: JsonFile): String? {
        if (nodeModules == null) return null
        val packageDir = nodeModules.findFileByRelativePath(packageName) ?: return null
        val installedPackageJson = packageDir.findChild("package.json") ?: return null

        val psiFile = PsiManager.getInstance(contextFile.project).findFile(installedPackageJson) as? JsonFile ?: return null
        val root = psiFile.topLevelValue as? JsonObject ?: return null
        val versionLiteral = (root.findProperty("version")?.value as? JsonStringLiteral) ?: return null
        return versionLiteral.value
    }
}
