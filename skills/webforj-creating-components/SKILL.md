---
name: webforj-creating-components
description: "Builds and integrates reusable components in webforJ, including composites made from webforJ controls, third-party Web Components, npm component libraries, and plain JavaScript or TypeScript libraries. Use whenever the user asks to create, wrap, integrate, or expose a custom component, install a frontend package, use @BundleEntry or @BundlePackage, add React/Svelte/Lit code, author SCSS/Less/Tailwind for a component, bridge JavaScript events or properties to Java, or turn a browser widget into a type-safe webforJ API. This skill must be used before hand-copying npm assets or inventing JavaScript interop."
---

# Creating and Integrating Components in webforJ

Build the smallest durable bridge between Java and the browser. Prefer a built-in
webforJ component, then Java composition, then a direct custom-element wrapper.
Adapt a plain JavaScript library behind a custom element only when it doesn't
already expose one.

## Is this a Java-only component?

If the component is built purely from existing webforJ components — no npm
package, no authored JavaScript or CSS, no third-party custom element — then none
of the frontend machinery below applies. Write a `Composite`, skip every
reference file, and stop.

That case is common and cheap to get right, so treat the rest of this skill as
the exception path. Reading the bundler and interop references for a task that
never touches the browser costs real time and adds no correctness.

Go to [Compose webforJ components](#compose-webforj-components), then
[Verify](#verify). Everything between is for integrations that cross into the
browser.

## Bundler-first rule

On webforJ 26.01 and newer, **the frontend bundler is the default path** for
every frontend concern that is more than an already-owned static file: npm
packages, authored JavaScript/TypeScript, React/Svelte/Lit components, SCSS,
Less, Tailwind, imported CSS and assets, and frontend tests. Reach for
`@BundlePackage` and `@BundleEntry` first, not as an advanced alternative.

Use `@JavaScript`/`@StyleSheet` instead only when the project predates 26.01, or
the resource is an already-owned standalone file needing no npm resolution,
compilation, imports, or asset processing. On 26.01+, state why the bundler adds
no value before taking that exception. Never hand-copy an npm package into
`resources/static`, add a separate frontend project, add a CDN `<script>`, or ask
the user to install Node — the build plugin manages Bun.

Read [references/bundler.md](references/bundler.md) before editing build files or
loading any third-party resource.

## Start with facts, not assumptions

1. Read the project's webforJ version from `pom.xml` or `build.gradle`.
2. Call `webforj-mcp:get_versions` if the target line is unclear.
3. Search the webforJ knowledge base before using a framework class, annotation,
   lifecycle hook, or concern interface.
4. Read the third-party library's package exports and component API. Verify tag
   names, module paths, properties versus attributes, slots, methods, events,
   payloads, styling hooks, and cleanup requirements.
5. For generated Java, run `webforj-mcp:fqcn_validate` before writing it.

Do not infer a wrapper API from examples for another library. A wrong event name,
property binding, or package entry usually fails silently in the browser.

## Choose the architecture

| Situation | Use |
|---|---|
| A built-in webforJ component already solves it | Use the built-in component |
| Reusable UI made only from webforJ components | `Composite` |
| One-off use of a custom element with little Java API | `Element` |
| Reusable/type-safe wrapper for a custom element | `ElementComposite` |
| Custom element accepts default or named slots | `ElementCompositeContainer` |
| npm package already registers custom elements | `@BundlePackage` + module `@BundleEntry`, then `Element` or `ElementComposite` |
| Plain JS/TS library mounts into a DOM node | Author a custom-element adapter under `src/main/frontend`, bundle it, then wrap it |
| React / Svelte / Lit component | Author it under `src/main/frontend`, expose a custom element, then wrap it |
| Component-scoped or app-wide styles that must compile | A CSS/SCSS/Less `@BundleEntry` on the owning class |
| Existing owned standalone JS/CSS file, no npm/compile/import graph | Static annotations as a documented exception |
| Browser/page API with no widget | A focused Java service using `Page`; don't fake a visual component |

Read [references/architecture.md](references/architecture.md) when the choice is
not obvious.

## The bundler path (webforJ 26.01+)

### 1. Confirm the build plugin

Archetype projects already have it. Add it once to an existing project; declaring
`<extensions>true</extensions>` binds `bundle`, `test`, and `clean` with no
execution blocks:

```xml
<plugin>
  <groupId>com.webforj</groupId>
  <artifactId>webforj-maven-plugin</artifactId>
  <version>${webforj.version}</version>
  <extensions>true</extensions>
</plugin>
```

Gradle applies `com.webforj` through a `buildscript` classpath dependency. Both
are covered in [references/bundler.md](references/bundler.md).

### 2. Declare packages and entries on the owning class

`@BundlePackage` declares an npm dependency; `@BundleEntry` names what to build.
Both are repeatable and inherited, and both live on the class that needs them —
the wrapper component, not a random view.

```java
import com.webforj.bundle.annotation.BundleEntry;
import com.webforj.bundle.annotation.BundlePackage;

@BundlePackage(value = "@ui5/webcomponents", version = "^2.0.0")
@BundleEntry("@ui5/webcomponents/dist/Input.js")
@NodeName("ui5-input")
public final class Ui5Input extends ElementComposite {
}
```

`@BundleEntry` takes **either** a path relative to `src/main/frontend`
(`"charts/chart-element.ts"`, `"theme/theme.css"`) **or** a module path inside an
`@scope`d npm package (`"@ui5/webcomponents/dist/Input.js"`). Declare narrow
module entries: the build tree-shakes and shares common chunks across entries.

> **Unscoped npm packages cannot be named directly in `@BundleEntry`.** Only a
> value starting with `@` is treated as an npm specifier. `@BundleEntry("leaflet/dist/leaflet.js")`
> resolves to no local file, logs a warning, and is silently dropped from the
> build. For an unscoped package, author a one-line local entry that imports it
> and bind that file instead.

### 3. Author frontend sources under `src/main/frontend`

Put adapters, framework components, styles, and their tests there. `.ts`, `.tsx`,
`.js`, `.jsx`, `.css`, `.scss`, `.sass`, and `.less` all compile. Import CSS for
its side effect; import assets and resolve the emitted URL against
`import.meta.url`. Never author into `src/main/frontend/generated` — the build
wipes and owns that directory.

### 4. Load the right compiler

SCSS/Sass and Less extensions activate automatically when a source of that type
is present. Tailwind ships off and is enabled by id. React needs no extension —
Bun compiles its TypeScript and JSX directly; you only declare its packages.

### 5. Run the real build

`mvn package` / `gradle build` compiles the frontend (`prepare-package`), and
`mvn test` runs the Bun frontend tests. `mvn compile` alone proves nothing about
the frontend. During development, `mvn compile webforj:watch spring-boot:run`
rebuilds on change; adding or removing a `@BundleEntry` takes effect on the next
restart.

### Static exception or webforJ before 26.01

```java
@JavaScript(
    value = "ws://components/my-widget.js",
    attributes = @Attribute(name = "type", value = "module")
)
@StyleSheet("ws://components/my-widget.css")
```

`ws://` maps to `src/main/resources/static`. `context://` addresses classpath
resources for APIs that read and inline content. Prefer project-owned static
files over a pinned HTTPS URL. Never use bundler annotations on a version that
predates 26.01.

## Implement the selected path

### Compose webforJ components

Extend `Composite`, keep internals private, configure the bound component in the
constructor, and expose a domain-focused API. Never extend `Component`,
`DwcComponent`, or a final built-in component.

```java
public final class SearchBox extends Composite<FlexLayout> {
  private final FlexLayout self = getBoundComponent();
  private final TextField query = new TextField("Search");
  private final Button submit = new Button("Search");
  private final EventDispatcher dispatcher = new EventDispatcher();

  public SearchBox() {
    self.add(query, submit);
    submit.onClick(event ->
        dispatcher.dispatchEvent(new SearchEvent(this, query.getValue())));
  }

  public SearchBox setValue(String value) {
    query.setValue(value);
    return this;
  }

  public String getValue() {
    return query.getValue();
  }

  public ListenerRegistration<SearchEvent> onSearch(
      EventListener<SearchEvent> listener) {
    return dispatcher.addListener(SearchEvent.class, listener);
  }

  public static final class SearchEvent extends EventObject {
    private final String query;

    public SearchEvent(SearchBox source, String query) {
      super(source);
      this.query = query;
    }

    public String getQuery() {
      return query;
    }
  }
}
```

Use `initBoundComponent()` only when the bound component needs a parameterized
constructor. Use `onDidCreate`, `whenAttached`, or `onDidDestroy` only for
DOM-dependent setup or real cleanup.

For Java-only composite events, use `EventDispatcher`: register listeners with
`addListener(EventClass.class, listener)` and dispatch an `EventObject` carrying
already-known Java state. DOM `ComponentEvent` annotations belong to browser
events, not ordinary composite business events.

### Wrap a custom element

Use `@NodeName` with `ElementComposite`; switch to `ElementCompositeContainer`
when the element exposes slots.

```java
@NodeName("acme-rating")
public final class Rating extends ElementComposite {
  private final PropertyDescriptor<Integer> value =
      PropertyDescriptor.property("value", 0);

  public Rating setValue(int value) {
    set(this.value, value);
    return this;
  }

  public int getValue() {
    return get(value);
  }
}
```

Map runtime state and non-string values to properties. Map markup configuration,
ARIA hooks, and CSS-selector state to attributes. Use typed beans for structured
objects and enums with `@SerializedName` for fixed string values. Validate public
inputs before calling `set`.

Add concern interfaces only when their forwarding behavior matches the wrapped
element — not because the name sounds useful. The flip side matters just as much:
when the element genuinely has that state, the concern is the right way to expose
it. Enablement in particular is framework-managed, so model it with
`HasEnablement`; a `disabled` `PropertyDescriptor` is overwritten when the
component attaches.

Read [references/element-wrappers.md](references/element-wrappers.md) for
properties, attributes, synchronization, slots, methods, and concerns.

### Adapt a plain JavaScript library

Do not bury a stateful library in repeated `executeJs` strings. Create a small
custom element in `src/main/frontend` that:

1. owns a stable mount element;
2. initializes the library in `connectedCallback`;
3. avoids duplicate initialization on reconnect;
4. destroys/unsubscribes in `disconnectedCallback`;
5. exposes Java-friendly properties and methods;
6. dispatches documented `CustomEvent`s with minimal serializable payloads.

Bundle the adapter and its npm dependency, then wrap the registered tag with
`ElementComposite`. This separates browser lifecycle from the Java API and makes
the integration testable and reusable. The same shape carries React, Svelte, and
Lit: keep framework state inside the frontend component and let Java depend only
on the custom-element contract.

Read [references/javascript-adapters.md](references/javascript-adapters.md).

## Bridge events and methods deliberately

Create a typed `ComponentEvent<T>` annotated with `@EventName` and
`@EventOptions` when callers need a reusable Java event API. Extract only the
fields Java consumes. Add a target filter only when bubbled child events could be
mistaken for the host event.

For high-frequency events, debounce or throttle client-side with
`ElementEventOptions`. Do not fetch the same state from the client after an event
if it can travel in the event payload.

Call existing element methods with `callJsFunctionAsync` or
`callJsFunctionVoidAsync`; arguments are serialized safely and component
arguments resolve to client elements after attachment. Use `executeJs*` only for
logic that cannot be represented as a method call. Never concatenate user values
into JavaScript source.

Read [references/events-and-interop.md](references/events-and-interop.md).

## Style without guessing

Prefer the component's documented CSS custom properties, shadow parts, slots, and
host attributes. For DWC components, query `webforj-mcp:styles_get_component`
before writing `--dwc-*` variables or `::part()` selectors, and validate tokens
with `webforj-mcp:styles_validate_tokens`.

For third-party components, use only styling hooks documented by that library.
Global CSS cannot pierce an unexported shadow root — a Tailwind utility or an app
stylesheet styles a component's outer box, never its internals.

For a component's own styles on 26.01+, ship a CSS/SCSS/Less `@BundleEntry` bound
to the component class so the styles load exactly when the component does.

## Complete the integration

The deliverable is not only a wrapper class. Cover every relevant surface:

- build plugin and dependency declarations;
- frontend entry/adapter and imported styles/assets;
- Java wrapper or composite;
- typed properties, methods, events, and slots actually requested;
- cleanup for observers, editors, charts, maps, and subscriptions;
- unit tests for server-side API wiring;
- Bun tests for authored frontend logic;
- browser-level test when behavior depends on custom-element registration,
  shadow DOM, or third-party runtime behavior.

Keep the public API smaller than the third-party API. Expose what the application
needs now, with types and names natural to Java.

## Verify

1. Validate imports with `webforj-mcp:fqcn_validate`.
2. Run the project's targeted compile/tests.
3. For bundler integrations, run `mvn package`/`gradle build` so the frontend is
   actually compiled, then confirm the log says `resolved N entry source(s)` with
   N matching the entries you declared. A dropped entry never fails the build, and
   the two ways to lose one differ: an unresolvable path logs `no source file
   under ...`, while a missing `src/main/frontend` directory drops every entry
   with no warning at all. Checking the count catches both; checking for the
   warning catches only one.
4. Inspect browser console and network failures when an app can be launched.
5. Confirm the custom element is defined, the entry loads once, properties and
   events work, and teardown doesn't leak observers or duplicate instances.

Use [references/verification.md](references/verification.md) as the final
checklist. Read [references/testing.md](references/testing.md) for executable
unit, frontend, and browser-test patterns.
