# Verification Checklist

## Architecture

- A built-in webforJ component wasn't overlooked.
- `Composite` is used for Java-only composition; nothing extends `Component`,
  `DwcComponent`, or a built-in component.
- `ElementCompositeContainer` is used only when slots exist.
- A plain JS library is lifecycle-owned by an adapter or a deliberately small
  direct mount.

## Version and resources

- The project's webforJ version was read from the build file.
- Bundler annotations appear only on 26.01+.
- On 26.01+, npm/compiled/framework frontend went through the bundler, and any
  static-annotation exception is justified in the implementation.
- No `node_modules` copied into `resources/static`, no CDN script, no separate
  frontend project, no npm build script added.

## Bundler

- The `com.webforj:webforj-maven-plugin` (or Gradle `com.webforj` plugin) is
  present, versioned with webforJ, and declared `<extensions>true</extensions>`.
- `@BundlePackage` names a real package with an explicit semver range; build-only
  packages carry `dev = true`.
- No duplicate `@BundlePackage` for the same package with a different version.
- Every `@BundleEntry` either resolves to a file under `src/main/frontend` or is
  an `@scope`d npm module path — no unscoped npm specifier, which is silently
  dropped.
- The build log shows no `no source file under ... for @BundleEntry` warning.
- Annotations sit on the component that owns the frontend, not on one consuming
  view; app-wide styles sit on the `App` class.
- Nothing was authored into `src/main/frontend/generated`.
- `node_modules`, `generated`, and build output are not committed; `package.json`
  is.
- Extensions used are enabled the right way: SCSS/Less activate on file type,
  Tailwind by id, React needs none.
- Extension options, if any, live in `src/main/frontend/bun.config.ts` under the
  extension id.

## Wrapper contract

- `@NodeName` equals the registered tag.
- Properties versus attributes match third-party docs.
- Structured values are typed; enums carry `@SerializedName` where needed.
- Public values are validated; setters are fluent.
- Values the browser changes on its own are read with `get(descriptor, true)` or
  kept current by synchronization — not read from a stale cache.
- Slot names, method names, event names, and payload paths are verified against
  the library.
- Concern interfaces match real behavior.
- No user-controlled value is concatenated into JS.

## Lifecycle and performance

- `customElements.define` is guarded by a `customElements.get` check.
- Initialization is idempotent across reattach.
- State set before attachment is applied after connection.
- Destroy/disconnect removes observers, listeners, and library instances.
- Java-driven setters don't echo back as user events.
- High-frequency events are debounced/throttled client-side.
- Event payloads avoid follow-up client queries.

## Tests

- `PropertyDescriptorTester` covers conventional descriptors.
- Unit tests cover validation, fluent returns, slots, and listener registration.
- Java-only events are tested by triggering behavior, checking payload, removing
  the registration, and confirming no second delivery.
- Authored frontend logic has Bun tests under `src/main/frontend`.
- A browser test covers actual registration/loading for runtime-dependent
  integrations.
- Browser event tests include meaningful `false`, `0`, empty, and numeric values.
- Reconnect tests prove cleanup and prevent duplicate instances or events.

## Build and runtime

- `mvn package` / `gradle build` succeeds — not `mvn compile` alone — so the
  frontend actually compiled.
- Frontend tests ran and passed as part of that build.
- Browser console/network show no missing modules, assets, duplicate custom
  element definitions, or CSP failures.
- The compiled entry loads once per page and only for the views that need it.
