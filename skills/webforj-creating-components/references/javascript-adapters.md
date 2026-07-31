# Frontend Adapters and Framework Components

Wrap a stateful library behind a custom element instead of scattering setup and
string-built JavaScript through Java. Everything here lives under
`src/main/frontend` and is bound with `@BundleEntry`; see
[bundler.md](bundler.md).

## Adapter template

```ts title="src/main/frontend/charts/chart-element.ts"
import { Chart, type ChartConfiguration } from "chart.js/auto";

class AppChart extends HTMLElement {
  private canvas?: HTMLCanvasElement;
  private chart?: Chart;
  private config?: ChartConfiguration;

  connectedCallback() {
    if (this.chart) return;
    this.canvas = document.createElement("canvas");
    this.replaceChildren(this.canvas);
    this.createChart();
  }

  disconnectedCallback() {
    this.chart?.destroy();
    this.chart = undefined;
  }

  set configuration(value: ChartConfiguration | undefined) {
    this.config = value;
    this.createChart();
  }

  get configuration() {
    return this.config;
  }

  resize() {
    this.chart?.resize();
  }

  private createChart() {
    if (!this.isConnected || !this.canvas || !this.config) return;
    this.chart?.destroy();
    this.chart = new Chart(this.canvas, this.config);
  }
}

if (!customElements.get("app-chart")) {
  customElements.define("app-chart", AppChart);
}
```

```java
@BundlePackage(value = "chart.js", version = "^4.0.0")
@BundleEntry("charts/chart-element.ts")
@NodeName("app-chart")
public final class Chart extends ElementComposite {
}
```

Adapt the lifecycle to the library:

- guard duplicate initialization — `connectedCallback` runs again on every
  reattach and on every move in the DOM;
- support properties set before connection (Java may `set` before attach);
- destroy editors/charts/maps, disconnect observers, and remove global listeners
  in `disconnectedCallback`;
- avoid dispatching change events from Java-driven property setters unless the
  library's documented contract does so — otherwise a server write echoes back as
  a user event;
- dispatch user-originated events with `bubbles: true` and `composed: true` when
  consumers outside the shadow tree must observe them;
- send small JSON-serializable `detail` payloads, never live library objects.

Guard `customElements.define` with a `customElements.get` check. Two entries that
both import an element module would otherwise throw on the second definition.

## Styles inside the adapter

Import a stylesheet for its side effect and the bundler folds it into the entry's
styles. For shadow-scoped styles, import the stylesheet as text and insert it into
the shadow root — global CSS does not cross a shadow boundary:

```ts
class ColorSwatch extends HTMLElement {
  connectedCallback() {
    const root = this.attachShadow({ mode: 'open' });
    const style = document.createElement('style');
    style.textContent = sheet;
    root.append(style);
  }
}
```

Resolve non-code assets against `import.meta.url`, not the document, so the URL
points at the compiled asset wherever the output is served:

```ts
const logo = new URL(logoPath, import.meta.url).href;
```

## Lit

No extension needed — Bun compiles the TypeScript. Declare `lit` and bind the
entry.

```ts title="src/main/frontend/greeting/hello-greeting.ts"
import { LitElement, html } from 'lit';
import { customElement, property } from 'lit/decorators.js';

@customElement("hello-greeting")
class HelloGreeting extends LitElement {
  @property() name = "";

  render() {
    return html`<p>Greetings, ${this.name}</p>`;
  }
}
```

```java
@BundlePackage(value = "lit", version = "^3.0.0")
@BundleEntry("greeting/hello-greeting.ts")
@NodeName("hello-greeting")
public final class Greeting extends ElementComposite {
  private final PropertyDescriptor<String> name =
      PropertyDescriptor.property("name", "");

  public Greeting setName(String value) {
    set(name, value);
    return this;
  }
}
```

Lit reactive properties map cleanly to `PropertyDescriptor.property`. Expose state
changes as `CustomEvent`s, not as Lit internals.

## React

React renders at runtime rather than compiling to something new, so there is
**no React extension** — Bun handles the TSX. Declare the packages, wrap the
component as a custom element, and bind the entry.

```java
@BundlePackage(value = "react", version = "^19.0.0")
@BundlePackage(value = "react-dom", version = "^19.0.0")
@BundlePackage(value = "@r2wc/react-to-web-component", version = "^2.0.0")
@BundleEntry("rating/rating.tsx")
@NodeName("app-rating")
public final class Rating extends ElementComposite {
}
```

Either use a wrapper library as above, or own the root explicitly: create the
root in `connectedCallback`, `render` on every property change, and call
`root.unmount()` in `disconnectedCallback`. Never let a React root outlive its
host element.

## Svelte

`.svelte` files compile at build time, and no shipped extension covers them.
Either compile the component to a custom element with Svelte's own custom-element
output and import the result from a `.ts` entry, or write a bundler extension
that contributes `bun-plugin-svelte` (see [bundler.md](bundler.md)).

## The Java boundary

Do not make Java know React props, Svelte internals, or Lit controllers. Java
depends on the custom-element contract: properties, attributes, methods, slots,
and events. Framework state stays in the frontend.

That boundary is what makes the integration testable — the element can be tested
with Bun without a server, and the Java wrapper can be tested without a browser.

## When direct mounting is acceptable

A direct mount in `onDidCreate` can be reasonable for a tiny, private, one-view
integration with no reusable state. Even then:

- pass values through `callJsFunction*`, not string concatenation;
- store the browser instance on the host element;
- guard duplicate setup;
- destroy it in the matching lifecycle;
- move to an adapter once properties/events/methods multiply.

When cleanup depends on a live browser instance, prefer the adapter:
`disconnectedCallback` runs while that instance is still available. Java
`onDidDestroy` is appropriate for server resources but is fragile for DOM
teardown.
