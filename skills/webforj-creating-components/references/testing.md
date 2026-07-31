# Component Integration Testing

Three layers, each proving something the others cannot: Java unit tests for the
server-side API, Bun tests for authored frontend logic, browser tests for the
real DOM contract.

## Java: property descriptors

```java
import com.webforj.component.element.PropertyDescriptorTester;

@Test
void propertiesRoundTrip() {
  PropertyDescriptorTester.run(AcmeToggle.class, new AcmeToggle());
}
```

Use `@PropertyExclude` for a descriptor that can't follow the conventional test,
or `@PropertyMethods(getter = "...", setter = "...", target = SomeClass.class)`
when accessors use different names or live elsewhere. There is also an overload
taking a filter predicate when only some descriptors should be covered.

Client-fetched getters (`get(descriptor, true)`) need a browser test; a
server-only unit test can't prove live DOM state.

## Java: fluent API and validation

```java
@Test
void setterIsFluentAndValidates() {
  Rating rating = new Rating();
  assertSame(rating, rating.setValue(3));
  assertEquals(3, rating.getValue());
  assertThrows(IllegalArgumentException.class, () -> rating.setValue(-1));
}
```

## Java: slots

Expose the underlying `Element` package-privately only when a test needs it, then
assert the exact named/default slot with `getFirstComponentInSlot` or
`getComponentsInSlot`.

## Java: composite events

Register a listener, trigger public behavior, assert its payload, remove the
returned registration, trigger again, and assert no second call. Merely counting
listeners doesn't prove dispatch or removability.

## Frontend: Bun tests

Authored frontend logic is tested with the Bun test runner, and it runs as part
of the build — `mvn test` runs it alongside the Java tests, and a failing
frontend test fails the build. No extra command to invoke.

Tests live under `src/main/frontend` next to the sources they cover. A file named
`*.test.ts`, `*.spec.ts`, `*_test.*`, or `*_spec.*` is a test.

```ts title="src/main/frontend/charts/config.ts"
export const clampPointIndex = (index: number, length: number): number =>
  Math.min(Math.max(index, 0), Math.max(length - 1, 0));
```

```ts title="src/main/frontend/charts/config.test.ts"
import { expect, test } from "bun:test";
import { clampPointIndex } from "./config";

test("clamps below zero", () => {
  expect(clampPointIndex(-4, 5)).toBe(0);
});
```

Cover the parts of an adapter that are pure logic: payload shaping, option
merging, index math, serialization. Custom-element lifecycle needs a DOM, so keep
that in the browser layer rather than faking it here.

Extra runner arguments go through `testArgs` — for example a JUnit reporter for
CI. A reporter output path must be absolute, because Bun runs from the frontend
source root and won't create the directory.

## Browser: DOM and custom-element behavior

Use a browser test to prove:

- the bundle or static script registers the expected tag;
- interaction dispatches the documented event;
- `false`, `0`, empty strings, and numeric payloads survive extraction;
- Java receives the typed values;
- a client-fetched getter reads the live value.

For editors, charts, and maps, attach, remove, and reattach the component. Assert
the old instance was destroyed, one current instance remains, and an event fires
once rather than accumulating duplicate handlers.

Run `mvn package` / `gradle build` before browser tests so the generated frontend
is the artifact under test. A test run against a stale or absent bundle proves
nothing.
