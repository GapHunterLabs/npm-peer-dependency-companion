<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# NPM Peer Dependency Companion Changelog

## [Unreleased]

### Changed

- The rating prompt's local counter keeps one-way fingerprints of findings
  instead of their file paths, and deletes the list that earlier versions
  kept.
- `PRIVACY.md` describes the values the plugin keeps in the IDE's local
  settings.

## [0.1.1]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.1.0]

### Added

- Warning icon on a `package.json` `peerDependencies` entry unsatisfied
  by the real installed `node_modules` version, or not installed at
  all.
- 100% static JSON PSI analysis, no npm CLI invocation, no network
  calls, no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/npm-peer-dependency-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/npm-peer-dependency-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/npm-peer-dependency-companion/commits/0.1.0
