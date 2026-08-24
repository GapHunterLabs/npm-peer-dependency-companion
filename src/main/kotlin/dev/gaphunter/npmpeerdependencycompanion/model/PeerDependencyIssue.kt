package dev.gaphunter.npmpeerdependencycompanion.model

import com.intellij.psi.PsiElement

enum class PeerDependencyProblem { NOT_INSTALLED, VERSION_MISMATCH }

data class PeerDependencyIssue(
    val packageName: String,
    val requiredRange: String,
    val installedVersion: String?,
    val problem: PeerDependencyProblem,
    val nameElement: PsiElement,
)
