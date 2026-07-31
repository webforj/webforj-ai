# Frontend Bundler (webforJ 26.01+)

The bundler is a Bun-powered build step driven by the webforJ Maven/Gradle build
plugin. A class names the frontend it needs with annotations; the build installs
the npm packages, compiles the sources, and the runtime loads the output when a
component of that class is created.

## Default rule

On 26.01+, start here for every third-party or compiled frontend concern: npm
packages, authored JS/TS adapters, React/Svelte/Lit components, SCSS/Less/Tailwind,
imported CSS and assets, and frontend tests.

Use static asset annotations only when the input is an existing standalone file
owned by the project that needs no npm resolution, compilation, imports, or
emitted assets. Record that reason in the implementation. Existing `@JavaScript`
and `@StyleSheet` usages keep working unchanged — adopting the bundler does not
require migrating them.

## Adding the build plugin

Archetype projects on 26.01+ already have it.

```xml title="pom.xml"
<plugin>
  <groupId>com.webforj</groupId>
  <artifactId>webforj-maven-plugin</artifactId>
  <version>${webforj.version}</version>
  <extensions>true</extensions>
</plugin>
```

`<extensions>true</extensions>` binds the goals to the lifecycle with no
`<executions>` blocks to write. Align the plugin version with the webforJ
version property already in the project.

```groovy title="build.gradle"
buildscript {
  repositories { mavenCentral() }
  dependencies {
    classpath "com.webforj:webforj-gradle-plugin:${webforjVersion}"
  }
}

apply plugin: 'com.webforj'
```

Before editing a build file, inspect inherited and plugin-management
configuration so the addition doesn't conflict with a parent POM.

## Goals and tasks

| Maven goal | Gradle task | Phase | What it does |
|---|---|---|---|
| `bundle` | `webforjBundle` | `prepare-package` | Compiles the frontend for production |
| `test` | `webforjTest` | `test` | Runs the Bun test runner over `src/main/frontend` |
| `clean` | `webforjCleanFrontend` | `clean` | Removes `src/main/frontend/generated` |
| `watch` | `webforjWatch` | run by hand | Rebuilds on change during development |

Production verification must run `mvn package` / `gradle build` (or the explicit
bundle goal). `mvn compile` does not prove the frontend compiled.

Development watch:

```bash
mvn compile webforj:watch spring-boot:run   # Spring Boot
mvn compile webforj:watch jetty:run         # Maven Jetty plugin
```

webforJ archetypes set this as the default goal, so bare `mvn` starts both. A
stylesheet or image change patches in place; anything else reloads the view.
Adding or removing a `@BundleEntry` takes effect on the next server restart.

## Options

Set as Maven `<configuration>` elements or `-D` properties, and as Gradle
`webforj { }` values.

| Purpose | Maven `<configuration>` | Maven property | Gradle | Default |
|---|---|---|---|---|
| Pin the Bun version | `<bunVersion>` | `webforj.bundler.version` | `bunVersion` | managed |
| Use an existing Bun binary | `<bunPath>` | `webforj.bundler.path` | `bunPath` | download |
| Bun binary cache | `<cacheDir>` | `webforj.bundler.cacheDir` | `cacheDir` | `${user.home}/.webforj/bun` |
| Frontend source root | `<sourceRoot>` | `webforj.bundler.sourceRoot` | `sourceRoot` | `src/main/frontend` |
| Plugin work directory | `<workDir>` | `webforj.bundler.workDir` | `workDir` | `target/bundle` |
| Enable/disable extensions by id | `<plugins>` | — | `plugins` | per extension |
| Skip packages in the annotation scan | `<excludePackages>` | `webforj.bundler.excludePackages` | `excludePackages` | — |
| Single eager bundle at app start | `<eager>` | `webforj.bundler.eager` | `eager` | `false` |
| Extra `bun test` arguments | `<testArgs>` | `webforj.bundler.testArgs` | `testArgs` | — |
| Skip frontend tests | — | `skipTests`, `maven.test.skip` | — | `false` |

```xml
<configuration>
  <plugins>
    <webforj-tailwind>true</webforj-tailwind>
    <webforj-scss>false</webforj-scss>
  </plugins>
</configuration>
```

```groovy
webforj {
  plugins.put('webforj-tailwind', 'true')
  eager = true
}
```

Bun is downloaded and cached by the plugin. Users never install Node, and the
project never needs a separate frontend project or npm script.

## `@BundlePackage`

```java
import com.webforj.bundle.annotation.BundlePackage;

@BundlePackage(value = "chart.js", version = "^4.0.0")
@BundlePackage(value = "typescript", version = "^5.0.0", dev = true)
```

- `value` is the npm package name; `version` is an npm semver range, resolved by
  Bun at install time. Both are required.
- `dev = true` installs into `devDependencies` — for a package needed only to
  compile, never shipped to the browser.
- Repeatable and `@Inherited`.
- Declarations are collected from the whole classpath, merged into the project's
  root `package.json`, and installed with `bun install`. Hand edits to
  `package.json` are preserved.
- **Duplicate names with different versions:** the first declaration wins and the
  build logs a warning. Keep one version range per package across the project.
- A project that declares no packages and has no `package.json` keeps none — npm
  stays out of a build that doesn't need it.

## `@BundleEntry`

```java
import com.webforj.bundle.annotation.BundleEntry;

@BundleEntry("charts/chart-element.ts")               // authored source
@BundleEntry("@ui5/webcomponents/dist/Input.js")      // npm module
@BundleEntry(value = "devtools/overlay.ts", debug = true)
```

The value is one of two things:

1. **A path relative to `src/main/frontend`** — a `.ts`, `.tsx`, `.js`, `.jsx`,
   `.css`, `.scss`, `.sass`, or `.less` file you authored. Any layout is fine as
   long as the file exists.
2. **A module path inside an `@scope`d npm package** — the build writes a small
   re-export stub so the module survives tree shaking (a `sideEffects: false`
   package would otherwise be dropped whole, taking its element registration with
   it).

### Unscoped npm packages are not npm entries

Only a value starting with `@` is treated as an npm specifier. Anything else is
assumed to be a local file. `@BundleEntry("leaflet/dist/leaflet.js")` matches no
file, logs `no source file under ... for @BundleEntry 'leaflet/dist/leaflet.js'`,
and is **dropped** — the build still succeeds and the component silently fails in
the browser. Bind a one-line local entry instead:

```ts title="src/main/frontend/vendor/leaflet.ts"
import "leaflet";
import "leaflet/dist/leaflet.css";
```

```java
@BundlePackage(value = "leaflet", version = "^1.9.0")
@BundleEntry("vendor/leaflet.ts")
```

Always read the build log for that warning after adding an entry.

### `src/main/frontend` must exist, even for pure npm entries

The frontend source directory has to be present for **any** entry to resolve,
including one that names only an npm module. A class whose sole entry is
`@BundleEntry("@ui5/webcomponents/dist/Input.js")` resolves to nothing when
`src/main/frontend` is absent — the normal state of a project that has never
authored a frontend file.

This is worse than the unscoped-package case, because no warning is emitted. The
build logs `resolved 0 entry source(s) to build` / `nothing to bundle`, reports
**BUILD SUCCESS**, and the component is dead in the browser with nothing pointing
at why.

Create the directory whenever you add a bundler entry to a project that lacks it,
and keep it alive with a tracked placeholder so a clean checkout still builds:

```bash
mkdir -p src/main/frontend && touch src/main/frontend/.gitkeep
```

The reliable check is the log line, not the build status: `resolved N entry
source(s)` must match the number of entries you declared.

### Other facts

- Repeatable and `@Inherited`. A base class's entries load for a subclass that
  declares nothing of its own.
- `debug = true` builds the entry normally but injects it only when the webforJ
  environment reports debug mode — right for a development-only diagnostic.
- An entry may be a plain `.css` file with no script; the runtime loads it as
  styles for the bound class.

## How entries load at runtime

Output is bound to the **declaring class name** and injected the first time a
component of that class is created — wherever it is used and however deeply it is
nested. A routed view and a layout are components like any other, so binding is
to component creation, not routing. An entry on the `App` class loads at app
start.

Consequences worth designing around:

- Put the annotation on the **component that needs the frontend**, not on one
  view that happens to use it. A wrapper carrying its own `@BundleEntry` works
  everywhere it is instantiated.
- App-wide CSS belongs on the `App` class (`@BundleEntry("app.css")`), which is
  what archetypes do.
- Each output loads once per page; scripts load as `type="module"`.
- Two entries importing the same package share a chunk rather than duplicating it.

## Build outputs and reserved paths

| Path | Owner | Notes |
|---|---|---|
| `src/main/frontend` | you | authored sources and their tests |
| `src/main/frontend/bun.config.ts` | you | extension options and extra Bun plugins |
| `src/main/frontend/generated` | build | wiped every run, self-gitignored — never author here |
| `package.json`, `bun.lock`, `node_modules` at project root | build | commit `package.json`, ignore `node_modules` |
| `target/bundle` | build | driver, config, metafile, watch staging |
| `target/classes/static/frontend` | build | compiled output the runtime serves |

Production builds emit hashed, minified files (`[dir]/[name]-[hash].[ext]`);
development builds keep stable, readable names so the watch can swap one file in
place. Because minification is part of the bundle step, a bundler project needs
no separate minifier plugin for its frontend.

Never commit `node_modules`, `src/main/frontend/generated`, or anything under
`target`.

## Extensions

An extension contributes a compiler for one kind of source, declares the npm
packages that compiler needs, and can generate entries of its own. Each carries
an id used both for enabling and for keying its options.

| Extension | Id | Activates on | Installs (dev) |
|---|---|---|---|
| SCSS/Sass | `webforj-scss` | a `.scss` or `.sass` source is present | `sass` |
| Less | `webforj-less` | a `.less` source is present | `less` |
| Tailwind | `webforj-tailwind` | off by default, enabled by id | `tailwindcss`, `bun-plugin-tailwind` |

Authoring the file is the signal — you do not enable SCSS or Less. Enabling by id
overrides the file-type default in both directions.

**React, and anything that only renders at runtime, needs no extension.** Bun
compiles TypeScript and JSX already. Declare the React packages with
`@BundlePackage`, wrap the component as a custom element (for example with
`@r2wc/react-to-web-component`), and bind the entry. There is no React compiler
to contribute.

**Tailwind** generates its own stylesheet from the utility class names found in
the project's Java sources and loads it for every view. It omits Tailwind's
preflight reset so it doesn't fight webforJ component styling. A utility class
styles a component's outer box only — it never crosses a shadow boundary into a
component's internals; use `::part()` and the component's CSS custom properties
for that.

### Configuring an extension

Options live in `src/main/frontend/bun.config.ts`, keyed by extension id, and are
forwarded straight to the tool the extension wraps:

```ts title="src/main/frontend/bun.config.ts"
export const options = {
  'webforj-scss': { loadPaths: ['styles'] }
};
```

The same file can append extra Bun plugins through a default export:

```ts
import myPlugin from './my-plugin';

export default [myPlugin()];
```

### Writing your own extension

For a source type with no shipped compiler (Svelte, Vue, MDX), implement
`com.webforj.bundle.bun.BundleExtension`, register it as a `ServiceLoader`
service, and depend on `com.webforj:webforj-bundle-bun` with `provided` scope.
`getId()` names it, `isEnabledByDefault(context)` typically checks
`context.getSourceExtensions()`, and `onWillBundle(context)` calls `addPackage`,
`addPlugin`, and optionally `addEntry`. The extension API is marked experimental
and its signatures may shift between releases.

## Shipping frontend from a reusable library JAR

A component library distributed as a Maven dependency can ship its frontend
sources inside the JAR under `META-INF/webforj/frontend/...`. The build extracts
them into the consuming project's generated directory before resolving entries,
so a `@BundleEntry` on a library class resolves against the shipped file exactly
as it would against local source. The consumer authors nothing and installs
nothing extra.

This mechanism is source-verified rather than documented; confirm it against the
target webforJ line before relying on it in a published library.

## Eager mode

By default each class loads only the frontend it uses. `eager = true` folds the
whole frontend into one bundle loaded at app start. Required where the runtime
serves no static folder of its own (the bundle is then inlined from the
classpath); otherwise it is a scale/latency trade-off. Leave it off unless one of
those applies.

## Frontend tests

`*.test.ts`, `*.spec.ts`, `*_test.*`, and `*_spec.*` files under
`src/main/frontend` run with the Bun test runner during the `test` phase. The
step is skipped when no test files exist, and a failing frontend test fails the
build. See [testing.md](testing.md).

## Static asset annotations

For webforJ before 26.01, or as a documented 26.01+ exception:

- `ws://file` resolves under `src/main/resources/static`.
- `context://` addresses classpath resources for APIs that read and inline
  content.
- HTTPS URLs are allowed; pin versions and understand offline/CSP consequences.
- Add `type="module"` only for ES modules.
- Assets load when the annotated component attaches and are deduplicated.
- `@InlineJavaScript` and `@InlineStyleSheet` need a stable ID when duplicate
  injection would be harmful.

Avoid copying an npm dependency's whole distribution into `static`. If a
pre-26.01 project must vendor files, copy only documented runtime artifacts and
their referenced assets, record the package version and license, and test the
resulting URLs.

## Troubleshooting

| Symptom | Cause |
|---|---|
| `no source file under ... for @BundleEntry 'x'` | local path typo, or an unscoped npm specifier — bind a local entry that imports the package |
| `resolved 0 entry source(s)` / `nothing to bundle`, no warning, build green | `src/main/frontend` does not exist, so every entry was dropped before resolution — create the directory |
| Element never defined, build green | entry dropped (above), or the package module doesn't register the element |
| `nothing to bundle, skipping Bun invocation` | no entry resolved on the whole classpath |
| Entry works in one view only | annotation sits on that view instead of the shared component |
| New `@BundleEntry` ignored during watch | entry-set changes apply on the next server restart |
| Frontend absent from the deployed artifact | `mvn compile` was run instead of `mvn package` |
| Package installed at the wrong version | duplicate `@BundlePackage` with a different range; first wins, check the build warning |
| Custom element registered twice | the same module bound both as an npm entry and through a local import |
| Scan is slow on a large project | add `excludePackages` prefixes for packages that never declare bundle annotations |
