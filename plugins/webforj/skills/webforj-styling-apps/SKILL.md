---
name: webforj-styling-apps
description: Styles and themes webforJ applications using the DWC design-token system (--dwc-* CSS custom properties). Covers OKLCH palette configuration, component styling via CSS variables and ::part(), layout with design tokens, dark mode, and theme creation. Use when writing CSS for webforJ apps, choosing DWC tokens, styling components, theming, or when reviews flag hardcoded colors/spacing.
---

# webforj Styling Apps

## Overview

Style webforJ applications using `--dwc-*` CSS custom properties. Never hardcode colors, sizes, or spacing — use DWC tokens so dark mode and themes work automatically.

The DWC theme engine uses **OKLCH color space** internally. Palette seeds are HSL inputs (`-h`, `-s`) but all shades are generated via OKLCH for perceptual uniformity. No build step — everything recalculates at runtime via CSS.

## Workflow

1. **Classify** the task
2. **Scan** the app's Java source for components, themes, and expanses
3. **Look up** every component's styling API
4. **Write CSS** — nested, compact, tokens only
5. **Validate** every name and pattern

### Step 1: Classify

| Task | Approach |
|------|----------|
| Color reskin | Override `-h`, `-s` at `:root` (or use `-seed`) |
| Component styling | Look up component, use CSS vars > `::part()` |
| Layout / spacing | `--dwc-space-*`, `--dwc-size-*` — [tokens.md](references/tokens.md) |
| Typography | `--dwc-font-*` — [tokens.md](references/tokens.md) |
| Full theme | Palette + semantic remapping — [themes.md](references/themes.md) |
| Table styling | No CSS vars — `::part()` only, sizing via Java — [table.md](references/table.md) |
| Google Charts | JSON theme file + Gson — [google-charts.md](references/google-charts.md) |

### Step 2: Scan the app

Read Java source to find every component, theme, and expanse in use. Search for `ButtonTheme.`, `Theme.`, `.setTheme(`, `.setExpanse(`, `GoogleChart.Type.` and similar. Every variant found must be accounted for in the CSS.

### Step 3: Look up every component

**Do not write any CSS until you complete this step for every component.**

Call the webforJ MCP server:

- `styles_get_component` with `name` set to a DWC tag (`dwc-button`) or
  a webforJ Java class name (`Button`, `TextField`). Returns the real
  CSS custom properties, `::part()` names, reflected attributes, and
  slots for the target `webforjVersion`.
- `styles_get_component` with `mode: "list"` to enumerate every valid
  DWC tag for that version.
- `styles_get_component` with `mode: "map"` for the Java class -> DWC
  tag mapping.

For global tokens (palette, surfaces, spacing, typography) call
`styles_list_tokens` with a `prefix` (e.g. `--dwc-color-primary-`) or
`contains` (e.g. `seed`) filter. The returned list is verbatim and
complete for the filter passed - do not invent "implied" tokens.

**Use only names returned by these tools.** Anything else silently fails.
Never carry tokens across versions.

### Step 4: Write CSS

#### Styling hierarchy — follow this order

1. **Global tokens at `:root`** — palette h/s (or seed), surfaces, spacing. These cascade into ALL components including shadow DOM. Don't use `::part()` to repeat what a global token controls.
2. **Component CSS variables** on the tag selector — the designed API.
3. **`::part()` selectors** — only when no CSS variable covers the need.

**Never use `::part()` when a component CSS variable exists.**

#### CSS rules

- **Use CSS nesting.** `&` instead of repeating parent selectors.
- **Minimal comments.** No banners, dividers, or per-property explanations.
- **Override the source, not each consumer.** Change `--dwc-surface-2` at `:root` — don't write `::part()` for every panel.
- **Group logically:** palette -> surfaces/globals -> component CSS vars -> `::part()`.

#### Example: palette reskin (two tokens per palette)

```css
:root {
  --dwc-color-primary-h: 220;
  --dwc-color-primary-s: 75%;
}
```

#### Example: direct seed override (any CSS color)

```css
:root {
  --dwc-color-primary-seed: #6366f1;
}
```

#### Example: nested component styling

```css
dwc-button {
  &:not([theme]) {
    --dwc-button-background: var(--my-panel);
    &::part(control) { border: 2px solid var(--my-bevel-light); }
  }
  &[theme~="primary"] {
    --dwc-button-background: var(--my-accent);
  }
}
```

No bare `dwc-button` selector — breaks themed variants.

### Step 5: Validate

Call `styles_validate_tokens` with the generated or edited file
`content` (CSS, Java, MDX, or Markdown). It checks every `--dwc-*`
reference against the real global tokens and all component CSS variables
for the target `webforjVersion`, and flags any invalid token with its
line number and ranked similar-name suggestions. Run this before writing
any stylesheet to disk.

Then manually verify:

1. Every `::part()` name appears in that component's `styles_get_component` output
2. No `::part()` where a CSS variable covers the same thing
3. No bare tag selectors on components with `theme`/`expanse` — use `:not([theme])`
4. Zero hardcoded colors outside `:root` — every color a `var()` reference
5. Custom properties use a theme prefix (`--xp-*`), never `--dwc-*`
6. Shade numbers are multiples of 5

## DWC Rules

### Seven palettes only

`primary` · `success` · `warning` · `danger` · `info` · `default` · `gray`

`secondary`, `accent`, `neutral` do not exist and silently fail. `info` is the accent palette.

### Palette config: two tokens (+ optional seed)

| Token | Range | Meaning |
|-------|-------|---------|
| `-h` | 0–360 | Hue |
| `-s` | 0%–100% | Saturation |
| `-seed` | any CSS color | Direct override (hex, rgb, oklch, lab, etc.) |

When `-seed` is set, it overrides `-h` and `-s`. The engine extracts hue and saturation from the seed via OKLCH.

**`-c` does not exist.** Text contrast is automatic — the OKLCH engine flips text color at the 0.59 lightness threshold. WCAG AA (4.5:1) is guaranteed.

### Hue rotation

`--dwc-color-hue-rotate` (default: 3 degrees) subtly shifts hue across the palette — darker steps shift warm, lighter shift cool. Set to `0` to disable.

### Semantic tokens

| Token | Use |
|-------|-----|
| `--dwc-color-{palette}` | Normal |
| `--dwc-color-{palette}-dark` | Active / pressed |
| `--dwc-color-{palette}-light` | Hover / focus |
| `--dwc-color-{palette}-alt` | Tint (subtle highlight) |
| `--dwc-color-{palette}-text` | Text on palette bg |
| `--dwc-color-{palette}-tint` | Seed at 12% opacity |

Shades: `--dwc-color-{palette}-{5..95}` (step 5). Step 5 = always darkest, step 95 = always lightest (regardless of light/dark mode).

Surfaces: `--dwc-surface-1` (body) · `-2` (panels) · `-3` (windows). Tinted with primary hue.

### Mode-aware colors

`--dwc-color-black` and `--dwc-color-white` are **mode-aware** — black becomes near-white in dark mode, and vice versa. They are NOT static.

### Reflected attributes

`theme` and `expanse` need special care — external styles on the host beat `:host([attr])`, so bare tag selectors break built-in variants.

```css
/* WRONG */
dwc-button { --dwc-button-color: gray; }
/* CORRECT */
dwc-button:not([theme]) { --dwc-button-color: gray; }
```

### `::part()` limits

- Cannot select inside parts (`::part(label) span` = nothing)
- Cannot chain `::part()` selectors
- Cannot use vendor-prefixed pseudo-elements
- Pseudo-classes work: `::part(label):hover`
- Pseudo-elements work: `::part(toggle)::after`

### Accessibility

- **Never hide focus rings.** Restyle, don't remove.
- **Use `-text-{shade}` tokens** for text on colored backgrounds (WCAG 4.5:1 automatic).
- **Respect reduced motion.** Custom animations: `@media (prefers-reduced-motion: reduce) { animation: none; }`
- **Minimum font size:** `--dwc-font-size-xs`. Use `--dwc-font-size-s` for body text.

### Dark mode

Tokens adapt automatically via `--dwc-dark-mode` flag. Write CSS once. **Never write per-component `html[data-app-theme]` selectors.** Only valid use: text readability tuning in [themes.md](references/themes.md).

## Google Charts

Google Charts render in `<canvas>`/`<svg>` — `--dwc-*` CSS variables don't apply. Use JSON theme file + Gson. See [google-charts.md](references/google-charts.md).

## Loading CSS

```java
@StyleSheet("ws://css/app.css")  // -> resources/static/css/app.css
public class MyApp extends App { }
```

Themes: `light` · `dark` · `dark-pure` · `system`. Set via `@AppTheme("dark")` or `App.setTheme("dark")`.

## Resources

### MCP tools (webforJ MCP server)
- **`get_versions`** — list available webforJ majors so styling tools can
  target the right catalog. Call first when the target version is unknown.
- **`styles_get_component`** — CSS styling surface for a DWC component
  (CSS vars, parts, reflected attributes, slots). Also supports
  `mode: "list"` for all tags and `mode: "map"` for Java -> DWC mappings.
- **`styles_list_tokens`** — authoritative catalog of global `--dwc-*`
  tokens (palette seeds, shades, surfaces, spacing, typography, borders).
  Filter with `prefix` or `contains`.
- **`styles_validate_tokens`** — validate every `--dwc-*` reference in
  CSS/Java/MDX/MD text against the real DWC tokens and component CSS
  variables. Run before writing generated stylesheets to disk.
- **`create_theme`** — generate a full DWC theme from a primary HSL color.

### references/
- **[colors.md](references/colors.md)** — OKLCH shade scale, text contrast, border/focus patterns
- **[tokens.md](references/tokens.md)** — Spacing, typography, shadows, borders, sizing
- **[themes.md](references/themes.md)** — Palette reskin, seed override, dark mode
- **[component-styling.md](references/component-styling.md)** — Shadow parts, CSS variables, Java styling
- **[table.md](references/table.md)** — Table: zero CSS vars, `::part()` only, sizing via Java
- **[google-charts.md](references/google-charts.md)** — Chart types, options reference, Gson loading
