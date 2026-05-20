# DWC Themes

**Preference, per the skill's Hard rule 6:** always prefer overriding the built-in themes (`light` at `:root`, `dark` or `dark-pure` under their `html[data-app-theme="..."]` selector) over creating a new custom theme. Only define a custom theme when overriding the built-ins cannot express the requested behavior.

## Universal facts (all versions)

### Built-in app themes

```
light        bright background, default
dark         dark background tinted with the primary color
dark-pure    fully neutral dark theme based on gray tones
system       follow the OS preference, resolve to light or dark
```

Apply with `@AppTheme` or `App.setTheme()`.

```java
@AppTheme("dark-pure")
class MyApp extends App {
  // app code
}

// or programmatically
App.setTheme("dark-pure");
```

### Following the system preference

`@AppTheme("system")` is a reserved keyword. webforJ resolves it at runtime to either the registered light or dark theme and re-resolves when the OS preference changes. With defaults (`@AppLightTheme` defaults to `"light"`, `@AppDarkTheme` defaults to `"dark"`), the annotation alone is enough:

```java
@AppTheme("system")
public class AdaptiveApp extends App {
  // OS-light -> "light" theme; OS-dark -> "dark" theme
}
```

Add `@AppLightTheme` or `@AppDarkTheme` **only when the desired theme name differs from the default**. For example, to use `dark-pure` instead of `dark` for OS-dark mode:

```java
@AppTheme("system")
@AppDarkTheme("dark-pure")
public class AdaptiveApp extends App {
  // OS-light -> "light" (default); OS-dark -> "dark-pure" (overridden)
}
```

Writing `@AppLightTheme("light")` or `@AppDarkTheme("dark")` is redundant, those are already the defaults. Use a custom theme name (e.g. `@AppLightTheme("brand-light")`) when you registered your own theme for that appearance state.

Once resolved, the `data-app-theme` attribute on `<html>` is `light` or `dark` (or the chosen custom name), not the literal string `system`. Any CSS overrides should target the resolved name.

### Override the light theme

The default `light` theme is scoped to `:root`. Override it by setting tokens in `:root`:

```css
:root {
  --dwc-color-primary-h: 215;
  --dwc-color-primary-s: 100%;
}
```

### Override the built-in dark themes

```css
html[data-app-theme="dark"] {
  --dwc-color-primary-s: 80%;
}

html[data-app-theme="dark-pure"] {
  --dwc-color-primary-h: 280;
}
```

Switching with `App.setTheme("dark")` activates the rebranded built-in. No new theme name needed.

### Custom themes

Create a fully new theme only when a coexisting variant is required (e.g. a high-contrast skin or a customer-specific brand). Pick a unique name and define it under its own `data-app-theme` block:

```css
html[data-app-theme="brand"] {
  --dwc-color-primary-h: 280;
  --dwc-color-primary-s: 100%;
}
```

For a dark variant of a custom theme on webforJ 26+, set `--dwc-dark-mode: 1` and `color-scheme: dark` on the same selector:

```css
html[data-app-theme="brand-dark"] {
  --dwc-dark-mode: 1;
  --dwc-color-primary-seed: #a855f7;
  color-scheme: dark;
}
```

Apply with `@AppTheme("brand")` or `App.setTheme("brand")`. Custom themes coexist with the defaults and can be switched at runtime.

### Component themes

Individual components support a semantic theme drawn from a fixed palette: `DEFAULT`, `PRIMARY`, `SUCCESS`, `WARNING`, `DANGER`, `INFO`, `GRAY`. This is independent of the active application theme. Apply it through the component's typed setter:

```java
Button submit = new Button("Submit");
submit.setTheme(ButtonTheme.PRIMARY);
```

Each component exposes its own typed enum (`ButtonTheme`, the shared `Theme` enum for components like `Toast`/`Icon`, etc.) and its documentation lists which values it supports under its own **Styling > Themes** section.

### Component expanse

Most webforJ controls support an expanse that unifies their size. Apply it through the typed setter:

```java
Button submit = new Button("Submit");
submit.setExpanse(Expanse.LARGE);
```

The standard values are `XSMALL`, `SMALL`, `MEDIUM`, `LARGE`, `XLARGE`; a few controls extend the range (icon buttons reach `XXSMALL` down and `XXXLARGE` up).

Setting expanse forces a consistent control height and font size, so a row of `Button`, `TextField`, `ComboBox` controls with the same expanse line up exactly. Without expanse and without explicit sizing, controls render at inconsistent dimensions — set an expanse in responsive layouts where the layout drives width and height.

### Reduced motion

webforJ respects the user's "reduce motion" OS accessibility preference. When enabled, non-essential animations are disabled across all components automatically. No Java or CSS code is required.

## Token-usage patterns

Habits that keep custom CSS aligned with the design system and prevent drift in dark mode or across major versions.

### Always reference tokens with `var(...)`

Hardcoded color literals (`#3b82f6`, `rgb(59 130 246)`) don't adapt to dark mode and don't track palette changes.

```css
/* avoid */
.my-panel {
  background: #ffffff;
  color: #1f2937;
  border: 1px solid #e5e7eb;
}

/* prefer */
.my-panel {
  background: var(--dwc-surface-3);
  color: var(--dwc-color-body-text);
  border: 1px solid var(--dwc-border-color);
}
```

### Prefer variation tokens over raw step numbers

Variation tokens (`--dwc-color-primary`, `-dark`, `-light`, `-text`, `-alt`) resolve to a different step in light vs dark mode automatically. Raw step numbers (`--dwc-color-primary-50`) do not.

```css
/* avoid - frozen at step 50 in both modes */
.badge { background: var(--dwc-color-primary-50); }

/* prefer - shifts step in dark mode */
.badge { background: var(--dwc-color-primary); }
```

### Use the suffix that matches the role

| Suffix | Role |
|---|---|
| `--dwc-color-{name}` | Solid fill at full strength (buttons, badges, banners) |
| `--dwc-color-{name}-dark` | Active / pressed state |
| `--dwc-color-{name}-light` | Hover / focus background |
| `--dwc-color-{name}-alt` | Subtle tinted background for callouts and alt rows |
| `--dwc-color-{name}-text` | Colored text on a neutral surface |
| `--dwc-color-on-{name}-text` | Text placed **on** the colored shade as background (auto-contrast, webforJ 26+ only) |
| `--dwc-border-color-{name}` | Borders and dividers |

### Reserve surfaces and borders for their roles

Surfaces (`--dwc-surface-1` / `-2` / `-3`) build the page hierarchy. Borders (`--dwc-border-color`, `--dwc-border-color-*`) draw separators. Reusing palette steps for these roles works visually but loses the automatic mode adaptation that the dedicated tokens carry.

### Override at the seed level in custom themes

When building a custom theme, set the seed (`--dwc-color-{name}-h`, `-s`, or `-seed` in webforJ 26+) rather than overriding individual steps. The generator rebuilds the full palette around the seed, keeping the tonal range consistent.

```css
/* avoid - leaves other steps inconsistent */
html[data-app-theme="brand"] {
  --dwc-color-primary-50: #6366f1;
}

/* prefer - regenerates the whole palette */
html[data-app-theme="brand"] {
  --dwc-color-primary-seed: #6366f1; /* webforJ 26+ */
}
```

### Apply the same pattern to spacing, sizing, radius, and transitions

The same rule extends across the rest of the design system. Reference tokens, never magic numbers.

```css
/* avoid */
.my-panel {
  padding: 16px;
  border-radius: 8px;
  transition: background-color 250ms;
}

/* prefer */
.my-panel {
  padding: var(--dwc-space-m);
  border-radius: var(--dwc-border-radius);
  transition: background-color var(--dwc-transition);
}
```

Hardcoded values bypass user-preference font-size scaling, lock in a fixed shape language, and skip the design system's eased timing curves.

### Use `::part(...)` to reach into shadow DOM

DWC components are shadow DOM. Internal markup is hidden from outside selectors, so `.dwc-button-label { ... }` will not match anything. Target the exposed parts:

```css
dwc-button[theme='primary']::part(label) {
  letter-spacing: 0.02em;
}
```

Use `webforj-mcp:styles_get_component` to discover the parts a component exposes.

### Scope token overrides with a wrapper selector

CSS custom properties cascade. Setting a token on a wrapper retunes everything inside it without affecting the rest of the app.

```css
.danger-section {
  --dwc-color-primary-seed: #ef4444; /* webforJ 26+ */
}
```

Every component inside `.danger-section` (buttons, links, focus rings) now uses the danger-red hue, while the global theme stays unchanged.

### Test in both light and dark mode

Before shipping any custom CSS, switch the app to `dark` and `dark-pure` and walk the screens. The most common regression is hardcoded color values that look fine in one mode and read as illegible in the other.

### Don't reach for `!important`

It escapes the cascade and makes every future override harder. If a rule isn't winning, the cause is almost always a specificity mismatch with a cleaner fix — target the same selector the framework uses, or add a parent qualifier. Reserve `!important` for genuinely third-party styling with no other way to defeat.

## Loading a custom stylesheet

Two annotation-based paths and one runtime path.

### `@StyleSheet` for an external file

```java
@StyleSheet("ws://app.css")
public class MyApp extends App {
  // app code
}
```

`ws://app.css` resolves to `resources/static/app.css` in the project. Place CSS file overrides under that path so the web server serves them.

### `@InlineStyleSheet` for inline CSS

```java
@InlineStyleSheet("""
  :root {
    --dwc-color-primary-seed: #6366f1;
  }
  """)
public class MyApp extends App {
  // app code
}
```

### `Page.getCurrent()` at runtime

```java
Page page = Page.getCurrent();
page.addStyleSheet("ws://css/brand.css");
page.addInlineStyleSheet(":root { --dwc-color-primary-seed: #6366f1; }");
```

Use the runtime form when the stylesheet depends on data resolved at app startup (e.g. tenant-specific branding).

## webforJ 25 and prior only (webforJ 25 and prior)

### `:root` example with `-c`

webforJ 25 supported a contrast threshold variable. v2 removed it; it's a no-op there.

```css
:root {
  --dwc-color-primary-h: 215;
  --dwc-color-primary-s: 100%;
  --dwc-color-primary-c: 50;
  --dwc-font-size: var(--dwc-font-size-m);
}
```

### Dark theme example

```css
html[data-app-theme="dark"] {
  --dwc-color-primary-s: 9%;
  --dwc-color-white: hsl(210, 17%, 82%);
}
```

### Custom theme with `-c`

```css
html[data-app-theme="new-theme"] {
  --dwc-color-primary-h: 280;
  --dwc-color-primary-s: 100%;
  --dwc-color-primary-c: 60;
}
```

## webforJ 26+ only (webforJ 26+)

### `:root` example without `-c`

webforJ 26+ dropped `-c`. Text contrast is computed automatically per shade and clamped for WCAG AA.

```css
:root {
  --dwc-color-primary-h: 215;
  --dwc-color-primary-s: 100%;
  --dwc-font-size: var(--dwc-font-size-l);
}
```

### Direct seed override

Each palette also exposes `--dwc-color-{name}-seed` accepting any CSS color (hex, `rgb()`, `oklch()`, `lab()`):

```css
:root {
  --dwc-color-primary-seed: #6366f1;
}
```

The seed is a seed, not a target. Where it lands on the 5..95 step scale depends on its natural OKLCH lightness. To pin an exact color at an exact step, set the step explicitly: `--dwc-color-primary-50: #1d4ed8;`.

### Dark theme example

```css
html[data-app-theme="dark"] {
  --dwc-color-primary-s: 80%;
}
```

### Custom dark theme

To make a v2 custom theme dark, set `--dwc-dark-mode: 1` and `color-scheme: dark`:

```css
html[data-app-theme="new-dark-theme"] {
  --dwc-dark-mode: 1;
  --dwc-color-primary-h: 280;
  --dwc-color-primary-s: 100%;
  color-scheme: dark;
}
```

`color-scheme: dark` tells the browser to render native surfaces (scrollbars, autofill, default page bg) in dark mode. Without it, scrollbars and autofill rectangles stay light by default and look out of place over dark surfaces. Light themes don't need the declaration; browsers default to light.

## Migration

For a project moving from webforJ 25 to 26 (webforJ 25 -> 26), see [`v25-to-v26-css.md`](./v25-to-v26-css.md) for the full token-by-token migration sweep.
