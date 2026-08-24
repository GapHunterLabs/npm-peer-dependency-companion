package dev.gaphunter.npmpeerdependencycompanion.semver

/**
 * A deliberately narrow semver-range checker -- covers the npm range
 * shapes that show up in a real `peerDependencies` block: `^x.y.z`,
 * `~x.y.z`, `>=`/`<=`/`>`/`<` x.y.z, an exact `x.y.z`, and `*`/`""`
 * (any version). Does NOT implement full npm range grammar (OR ranges
 * `||`, hyphen ranges `1.0.0 - 2.0.0`, `x`/`X` wildcards mid-version) --
 * an unrecognized range is treated as "can't evaluate", never as a
 * false failure, so v0.1 stays honest about what it can check.
 */
object SemverRange {

    data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
        override fun compareTo(other: Version): Int {
            major.compareTo(other.major).let { if (it != 0) return it }
            minor.compareTo(other.minor).let { if (it != 0) return it }
            return patch.compareTo(other.patch)
        }
    }

    private val VERSION_PATTERN = Regex("""^(\d+)\.(\d+)\.(\d+)""")

    fun parseVersion(raw: String): Version? {
        val match = VERSION_PATTERN.find(raw.trim()) ?: return null
        val (major, minor, patch) = match.destructured
        return Version(major.toInt(), minor.toInt(), patch.toInt())
    }

    /** Null means "range shape not recognized -- can't evaluate", not "satisfied". */
    fun satisfies(installed: Version, range: String): Boolean? {
        val trimmed = range.trim()
        if (trimmed.isEmpty() || trimmed == "*" || trimmed == "latest") return true
        if (trimmed.contains("||") || trimmed.contains(" - ") || trimmed.contains(" ")) return null

        when {
            trimmed.startsWith("^") -> {
                val base = parseVersion(trimmed.substring(1)) ?: return null
                if (installed < base) return false
                return if (base.major > 0) {
                    installed.major == base.major
                } else if (base.minor > 0) {
                    installed.major == 0 && installed.minor == base.minor
                } else {
                    installed.major == 0 && installed.minor == 0 && installed.patch == base.patch
                }
            }
            trimmed.startsWith("~") -> {
                val base = parseVersion(trimmed.substring(1)) ?: return null
                return installed.major == base.major && installed.minor == base.minor && installed >= base
            }
            trimmed.startsWith(">=") -> return parseVersion(trimmed.substring(2))?.let { installed >= it }
            trimmed.startsWith("<=") -> return parseVersion(trimmed.substring(2))?.let { installed <= it }
            trimmed.startsWith(">") -> return parseVersion(trimmed.substring(1))?.let { installed > it }
            trimmed.startsWith("<") -> return parseVersion(trimmed.substring(1))?.let { installed < it }
            else -> return parseVersion(trimmed)?.let { installed == it }
        }
    }
}
