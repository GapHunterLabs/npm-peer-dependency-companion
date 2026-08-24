package dev.gaphunter.npmpeerdependencycompanion.parse

import com.intellij.json.psi.JsonFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.gaphunter.npmpeerdependencycompanion.model.PeerDependencyProblem

class PeerDependencyCheckerTest : BasePlatformTestCase() {

    fun `test peer dependency not installed is flagged`() {
        myFixture.configureByText(
            "package.json",
            """
            {
                "name": "demo",
                "peerDependencies": {
                    "react": "^18.0.0"
                }
            }
            """.trimIndent(),
        )
        val file = myFixture.file as JsonFile
        val issues = PeerDependencyChecker.findIssues(file)
        assertEquals(1, issues.size)
        assertEquals(PeerDependencyProblem.NOT_INSTALLED, issues[0].problem)
    }

    fun `test peer dependency satisfied by installed version is not flagged`() {
        myFixture.addFileToProject(
            "node_modules/react/package.json",
            """{"name": "react", "version": "18.2.0"}""",
        )
        myFixture.configureByText(
            "package.json",
            """
            {
                "name": "demo",
                "peerDependencies": {
                    "react": "^18.0.0"
                }
            }
            """.trimIndent(),
        )
        val file = myFixture.file as JsonFile
        assertTrue(PeerDependencyChecker.findIssues(file).isEmpty())
    }

    fun `test peer dependency installed outside range is flagged`() {
        myFixture.addFileToProject(
            "node_modules/react/package.json",
            """{"name": "react", "version": "17.0.2"}""",
        )
        myFixture.configureByText(
            "package.json",
            """
            {
                "name": "demo",
                "peerDependencies": {
                    "react": "^18.0.0"
                }
            }
            """.trimIndent(),
        )
        val file = myFixture.file as JsonFile
        val issues = PeerDependencyChecker.findIssues(file)
        assertEquals(1, issues.size)
        assertEquals(PeerDependencyProblem.VERSION_MISMATCH, issues[0].problem)
        assertEquals("17.0.2", issues[0].installedVersion)
    }
}
