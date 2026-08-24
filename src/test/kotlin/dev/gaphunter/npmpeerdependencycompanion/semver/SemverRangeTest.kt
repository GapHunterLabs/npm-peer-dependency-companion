package dev.gaphunter.npmpeerdependencycompanion.semver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SemverRangeTest {

    private fun v(s: String) = SemverRange.parseVersion(s)!!

    @Test
    fun `caret range accepts same major, rejects lower and higher major`() {
        assertEquals(true, SemverRange.satisfies(v("18.2.0"), "^18.0.0"))
        assertEquals(false, SemverRange.satisfies(v("17.9.0"), "^18.0.0"))
        assertEquals(false, SemverRange.satisfies(v("19.0.0"), "^18.0.0"))
    }

    @Test
    fun `tilde range accepts same major and minor only`() {
        assertEquals(true, SemverRange.satisfies(v("1.4.9"), "~1.4.2"))
        assertEquals(false, SemverRange.satisfies(v("1.5.0"), "~1.4.2"))
    }

    @Test
    fun `exact and comparison ranges`() {
        assertEquals(true, SemverRange.satisfies(v("2.0.0"), "2.0.0"))
        assertEquals(false, SemverRange.satisfies(v("2.0.1"), "2.0.0"))
        assertEquals(true, SemverRange.satisfies(v("3.0.0"), ">=2.0.0"))
        assertEquals(false, SemverRange.satisfies(v("1.9.9"), ">=2.0.0"))
    }

    @Test
    fun `wildcard and empty range always satisfied`() {
        assertEquals(true, SemverRange.satisfies(v("5.0.0"), "*"))
        assertEquals(true, SemverRange.satisfies(v("5.0.0"), ""))
    }

    @Test
    fun `unrecognized range shape returns null, never a false failure`() {
        assertNull(SemverRange.satisfies(v("1.0.0"), "1.0.0 || 2.0.0"))
        assertNull(SemverRange.satisfies(v("1.0.0"), "1.0.0 - 2.0.0"))
    }
}
