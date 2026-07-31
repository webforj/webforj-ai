# Events and JavaScript Interop

## Typed custom events

```java
@EventName("value-changed")
@EventOptions(data = {
    @EventData(key = "value", exp = "event.detail.value")
})
public final class ValueChangedEvent extends ComponentEvent<Picker> {
  public ValueChangedEvent(Picker source, Map<String, Object> data) {
    super(source, data);
  }

  public String getValue() {
    return (String) getData().get("value");
  }
}
```

`EventData` is nested inside `EventOptions`, so the bare `@EventData` form above
needs `import com.webforj.component.element.annotation.EventOptions.EventData;`.
Verify the annotation surface for the target version rather than copying old
examples.

`@EventOptions` also carries `filter` (a client-side expression that suppresses
the event before it crosses to the server), `code`, `debounce` (a nested
`@DebounceSettings(value = ..., phase = ...)`), and `throttle`. Prefer these
declarative forms for a fixed policy, and `ElementEventOptions` when the policy
varies per registration.

Expose a listener method that returns `ListenerRegistration`:

```java
public ListenerRegistration<ValueChangedEvent> onValueChanged(
    EventListener<ValueChangedEvent> listener) {
  return addEventListener(ValueChangedEvent.class, listener);
}
```

Convert numeric browser values through `Number`:

```java
public int getDatasetIndex() {
  Object value = getData().get("datasetIndex");
  if (!(value instanceof Number number)) {
    throw new IllegalStateException("Missing numeric datasetIndex event data");
  }
  return number.intValue();
}
```

A second `addValueChangedListener` alias is optional and should follow the local
component convention, not be generated mechanically.

## Payload design

- inspect the real event shape;
- extract only fields Java needs;
- preserve `0`, `false`, and empty strings: don't use `x || fallback` where those
  are valid values;
- use optional chaining/nullish coalescing where appropriate;
- avoid `JSON.stringify` unless the transport can't serialize the object
  directly;
- add `event.target.isSameNode(component)` only when bubbled descendant events
  would create false positives.

For rapid input/scroll/move events, configure debounce or throttle in
`ElementEventOptions` so unnecessary events never cross to the server.

```java
ElementEventOptions options = new ElementEventOptions()
    .addData("value", "component.value")
    .setDebounce(300, DebouncePhase.TRAILING);

addEventListener(InputEvent.class, listener, options);
```

Verify the overload and debounce enum for the target version. Don't duplicate or
contradict payload keys already defined by the typed event annotation.

## JavaScript calls

Use:

- `callJsFunctionAsync(name, args...)` for a method with a result;
- `callJsFunctionVoidAsync(name, args...)` for fire-and-forget;
- `executeJsAsync(script)` only for arbitrary logic with a result;
- `executeJsVoidAsync(script)` for arbitrary fire-and-forget logic.

`callJsFunction*` serializes primitives, collections, maps, beans, and component
references. Async calls wait for component arguments to attach.

`callJsFunctionAsync` waits for a hyphenated custom element to be defined before
invoking its method. Raw `executeJsAsync` doesn't provide that contract; chain
`getElement().whenDefined()` first when its script assumes a custom-element API.

Never concatenate user/library data into JavaScript source. If the browser
contract doesn't expose a callable method, add one to the frontend adapter.
