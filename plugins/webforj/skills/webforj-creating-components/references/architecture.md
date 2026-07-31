# Architecture Decision

## Decision order

1. Search for a built-in webforJ component.
2. If the UI is made from webforJ components, use `Composite`.
3. If a custom element already exists:
   - use `Element` for a small one-off;
   - use `ElementComposite` for a reusable typed API;
   - use `ElementCompositeContainer` when Java components fill its slots.
4. If a plain JS library owns a DOM mount point, adapt it behind a custom
   element, then use the same wrapper choices.
5. If no DOM widget exists, use a page-level service instead of inventing one.

## Why composition is the default

`Composite` hides the bound component and internal controls, so the public API
can express the application's domain rather than exposing every implementation
method. Configure ordinary layout, children, and listeners in the constructor.

Never extend `Component` or `DwcComponent` directly. Do not subclass built-in
components; they may be final and inheritance exposes an unstable surface.

## One-off `Element` versus wrapper

Use `new Element("tag-name")` when all of these hold:

- one view uses the element;
- only a few direct properties/listeners are needed;
- no shared validation, methods, concerns, or tests are required.

Create an `ElementComposite` when any of these hold:

- more than one view uses it;
- callers need a Java-friendly API;
- properties need validation or typing;
- events need typed payloads;
- slots or methods need convenience APIs;
- the integration belongs in a reusable library.

## Version gate

The frontend bundler starts in webforJ 26.01. Read the project version before
choosing it. For older projects, either use static asset annotations or invoke
the upgrading skill if the user wants npm/framework integration.

For 26.01+, resource selection is bundler-first. Architecture selection
(`Composite`, `Element`, or `ElementComposite`) doesn't change that rule: if the
component comes from npm or authored frontend source, load it through the
bundler. Static annotations are an exception for an already-owned standalone
file with no package/import/build needs.

## Where the frontend annotations belong

Architecture and resource loading are separate decisions, but they intersect at
one point: `@BundleEntry`/`@BundlePackage` bind to the class they annotate, and
the output loads when a component of that class is created.

- A reusable wrapper (`ElementComposite`, `Composite`) should carry its own
  annotations, so it works in every view that instantiates it.
- A view carries annotations only for frontend that view alone owns.
- The `App` class carries app-wide styles and anything that must exist before any
  view renders.

If a wrapper's frontend is declared on a view instead, the component breaks the
moment it is reused elsewhere — a failure that compiles cleanly.

## Cost of each choice

| Choice | Ongoing cost |
|---|---|
| Built-in component | none |
| `Composite` | Java only, no browser contract to maintain |
| `Element` | untyped call sites, repeated in every user |
| `ElementComposite` | one typed surface to keep aligned with the library |
| Authored adapter | a frontend lifecycle you own and must test |
| Static vendored file | manual version, license, and asset tracking forever |

Pick the cheapest row that satisfies what was actually asked.
