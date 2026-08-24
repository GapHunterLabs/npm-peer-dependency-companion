# Demo data — NPM Peer Dependency Companion

For capturing the real Marketplace screenshot:

1. `./gradlew runIde`
2. Open the `demo/` folder as a project (or copy `package.json` +
   `node_modules/react/package.json` into any sandbox project) inside
   the sandbox IDE.
3. `package.json` declares `"react": "^18.0.0"` as a peer dependency,
   but the installed `node_modules/react` is version `17.0.2` — the
   `"react"` key in `peerDependencies` shows the gutter warning icon.
   Hover it for the tooltip.
4. Enter Full Screen (`View > Appearance > Enter Full Screen`), capture
   with `Win+Shift+S`, save directly to `docs/screenshots/` in this
   repo.
