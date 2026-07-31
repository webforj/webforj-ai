# Element Wrappers

## Base classes

```java
@NodeName("acme-widget")
public final class Widget extends ElementComposite {
}
```

Use `ElementCompositeContainer` when the underlying element declares a default
or named slot. `@NodeName` must match the registered custom-element tag.

## Properties versus attributes

Use a property for current runtime state, booleans, numbers, objects, arrays, and
frequent updates. Use an attribute for markup configuration, ARIA, selectors, or
string-like values the component explicitly documents as attributes.

```java
private final PropertyDescriptor<String> value =
    PropertyDescriptor.property("value", "");
private final PropertyDescriptor<String> ariaLabel =
    PropertyDescriptor.attribute("aria-label", "");
```

Keep descriptors private and expose fluent accessors. `get(descriptor)` reads
the server cache. Use `get(descriptor, true)` when the browser can change the
value independently. Supply an explicit `Type` for parameterized values when
runtime type information isn't enough.

Prefer typed records/beans for structured data. Use enums with
`@SerializedName` for fixed third-party string values.

### `disabled` is owned by the framework, not by your descriptor

Don't model `disabled` as a `PropertyDescriptor`. webforJ manages enablement
itself and reapplies its own state when the component attaches, so a descriptor
write lands first and is then overwritten. The element renders enabled while the
server believes it is disabled — the write appears to succeed and nothing errors.

Implement the `HasEnablement` concern instead. It is the framework's contract for
this state, so it forwards to the bound element correctly and gives callers the
same `setEnabled`/`isEnabled` API every other webforJ component exposes:

```java
@NodeName("acme-toggle")
public final class AcmeToggle extends ElementComposite
    implements HasEnablement<AcmeToggle> {
}
```

If the wrapped element's own vocabulary is `disabled`, express that on top of the
concern rather than beside it:

```java
public AcmeToggle setDisabled(boolean disabled) {
  return setEnabled(!disabled);
}

public boolean isDisabled() {
  return !isEnabled();
}
```

Treat this as the general lesson: when the framework already defines a concern
for a piece of state (`HasEnablement`, `HasVisibility`, `HasFocus`), implement
the concern. A descriptor that competes with framework-managed state loses when
the component attaches, and only a browser-level test catches it.

## Keeping a property in step with the client

`get(descriptor)` returns the server's cached value — what the server last wrote.
When the browser can change a value on its own, choose one of:

- `get(descriptor, true)` reads the live client value on demand. Use this when a
  read is occasional and a round trip is acceptable.
- Synchronization keeps the cache current: when a named client event fires, the
  value rides the event payload back to the server. Use this when the element
  reports a change only through an event, or when reads are frequent enough that
  per-read round trips hurt.

Synchronization arrives in webforJ 26.02 (`@Synchronize` on the descriptor field,
or the `synchronize(...)` method when the registration must be removed or
replaced at runtime). Verify availability against the project's version; on
earlier lines, add a listener for the client event and update the descriptor from
the payload by hand — which is exactly what the shorthand does.

## Slots

Use constants and narrow methods:

```java
private static final String FOOTER_SLOT = "footer";

public Dialog addToFooter(Component... components) {
  getElement().add(FOOTER_SLOT, components);
  return this;
}
```

Default-slot children use `add(...)`. Use `getComponentsInSlot`,
`getFirstComponentInSlot`, and `findComponentSlot` in tests when needed.

## Methods

Call an existing browser method rather than reproducing it:

```java
public void open() {
  getElement().callJsFunctionVoidAsync("show");
}

public PendingResult<Object> exportData() {
  return getElement().callJsFunctionAsync("exportData");
}
```

Async variants wait for attachment and custom-element definition. Use sync calls
only when a blocking result is genuinely required and attachment is guaranteed.

## Concern interfaces

Add a concern only after confirming the underlying element obeys that contract.
Generic host capabilities such as class name/style are usually safe. Value,
label, focus, required, and read-only concerns may require overrides or may not
fit the third-party API at all.

Enablement is the exception to the caution: if the element can be disabled at
all, implement `HasEnablement` rather than modelling `disabled` as a property —
see [`disabled` is owned by the framework](#disabled-is-owned-by-the-framework-not-by-your-descriptor).

## Public API design

- expose only requested/stable features;
- validate nulls, ranges, and incompatible states;
- use Java naming and types rather than mirroring awkward JS names;
- document the third-party behavior, especially event timing and client-fetched
  getters;
- keep convenience constructors thin and delegate to setters.
