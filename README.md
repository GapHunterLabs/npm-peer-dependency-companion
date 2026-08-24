# NPM Peer Dependency Companion

Warning icon on a `package.json` `peerDependencies` entry whose real
installed version (from `node_modules`) doesn't satisfy the declared
semver range — or isn't installed at all. npm's own peer dependency
model is opt-in and silent: since npm 7 it auto-installs a compatible
peer if it can, but a version mismatch, an unresolved conflict, or a
peer installed via a workspace/hoisting quirk all produce no warning
in the editor, only a runtime failure the first time the mismatched
API is actually called.

## Why it exists

A library declares `"react": "^18.0.0"` as a peer dependency, the
project actually has React 17 installed, `npm install` doesn't fail —
and the first real signal is a confusing runtime error deep inside a
hooks call, far from the `package.json` line that's actually wrong.

## Why built this way

- **100% static JSON PSI analysis** — reads `peerDependencies` and the
  installed package's own `package.json` `"version"` field using the
  bundled JSON plugin's real PSI, no npm CLI invocation, no network
  calls.

## v0.1 scope — stated honestly, not exhaustively

The semver-range checker covers `^`/`~`/comparison/exact/`*` ranges —
an OR range (`||`) or a hyphen range is skipped, never reported as a
false failure. A monorepo/workspace layout where `node_modules` lives
at a different level than this `package.json` isn't resolved (no
hoisting simulation), a possible false positive in that specific
setup.

## Usage

Open any `package.json` with a `peerDependencies` block. An entry
whose installed version doesn't satisfy the declared range, or isn't
installed at all, shows a warning icon.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
