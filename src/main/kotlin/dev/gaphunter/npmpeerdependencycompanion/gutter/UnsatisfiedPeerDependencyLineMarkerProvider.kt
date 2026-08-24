package dev.gaphunter.npmpeerdependencycompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.npmpeerdependencycompanion.model.PeerDependencyIssue
import dev.gaphunter.npmpeerdependencycompanion.model.PeerDependencyProblem
import dev.gaphunter.npmpeerdependencycompanion.parse.PeerDependencyChecker
import dev.gaphunter.npmpeerdependencycompanion.review.ReviewPrompt

class UnsatisfiedPeerDependencyLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "Unsatisfied npm peerDependency"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile as? JsonFile ?: return
        if (file.name != "package.json") return

        val issues = PeerDependencyChecker.findIssues(file)
        if (issues.isEmpty()) return

        val issuesByElement = issues.associateBy { it.nameElement }
        for (element in elements) {
            val issue = issuesByElement[element] ?: continue
            result.add(buildMarker(issue))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(issue: PeerDependencyIssue): LineMarkerInfo<PsiElement> {
        val tooltip = when (issue.problem) {
            PeerDependencyProblem.NOT_INSTALLED ->
                "Peer dependency \"${issue.packageName}\" (${issue.requiredRange}) isn't installed in node_modules"
            PeerDependencyProblem.VERSION_MISMATCH ->
                "Peer dependency \"${issue.packageName}\" requires ${issue.requiredRange}, " +
                    "but ${issue.installedVersion} is installed"
        }
        return LineMarkerInfo(
            issue.nameElement,
            issue.nameElement.textRange,
            PeerDependencyIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}
