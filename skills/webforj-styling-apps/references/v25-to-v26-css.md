# webforJ 25 -> 26 CSS Migration

The OpenRewrite recipe in `webforj-upgrading-versions` handles Java/code
migration. It does NOT touch CSS. This file is the manual sweep for
stylesheets and inline `@InlineStyleSheet` blocks that depend on v25
token names or behavior.

webforJ 26 ships a refreshed DWC token system. Most v25 token names still resolve, but several
defaults, units, and meanings shifted. Pure-default apps need no CSS
changes; apps that customized seeds, palettes, fonts, shadows, focus
rings, or radii usually need a small sweep.

## Quick verdict

| If the app... | What to expect |
|---|---|
| Uses default styling | Visual refresh only. Primary palette retuned (h=211/s=100% -> h=223/s=91%), shadows more layered, components rounder. No CSS edit needed. |
| Overrides `--dwc-color-{name}-h` and `-s` | Keeps working. HSL seed path is preserved. |
| Overrides individual palette steps (e.g. `--dwc-color-primary-40`) | The step now resolves to a different color (lightness is now perceptually uniform). Switch to a variation token for mode-aware behavior. |
| Uses `--dwc-color-{name}-c` | Delete. The light/dark text flip is now computed per shade automatically; `-c` is a no-op. |
| References named font-size tokens (`--dwc-font-size-m`, `-l`, ...) | The scale shifted down one bucket. `m` is now 14px (was 16px). Step up one bucket to keep the v25 size. |
| Uses `--dwc-font-weight-semibold` to get 500-weight | `semibold` is now 600. Switch to the new `--dwc-font-weight-medium` for 500. |
| Reserves padding around focusables with `--dwc-focus-ring-width` | Add `--dwc-focus-ring-gap` (new) to that padding or the ring overflows. |
| Used `hsla(var(--dwc-shadow-color), 0.07)` | Shadow color is now full OKLCH, not an HSL triplet. Switch to a shadow token (`var(--dwc-shadow-m)`) or rewrite as `oklch(from var(--dwc-shadow-color) l c h / 0.07)`. |
| Customized button hover/ripple | Ripples are gone. Press feedback is `--dwc-scale-press` / `--dwc-scale-press-deep`. |
| Has a custom dark theme with 20 lines of overrides | Most of it can be deleted. Keep only `--dwc-dark-mode: 1` and the seed override. |

If none of the above apply, the upgrade is done.

## Token deltas

### Removed / no-op

| Token | What to do |
|---|---|
| `--dwc-color-{name}-c` | Delete. No effect in v26. |
| `--dwc-focus-ring-l` | Delete. Lightness is computed per mode now. |
| `--dwc-ripple-color` | Still emitted, no longer used. Switch to `--dwc-scale-press`. |

### Meaning changed

| Token | v25 meaning | v26 meaning |
|---|---|---|
| `--dwc-color-{name}-text-{step}` | Pure black/white text **on** the step as background | Surface-safe tinted text usable on **neutral** page backgrounds |
| `--dwc-color-{name}-alt` | Palette step 95 (near-white solid) | Seed at 12% opacity (translucent tint) |
| `--dwc-border-color-{name}` | The saturated palette shade | A mode-aware subtle separator tone |
| `--dwc-shadow-color` | HSL triplet (`h, s%, l%`) | Full OKLCH color |

### New tokens

| Token | Use |
|---|---|
| `--dwc-color-on-{name}-text-{step}` | Text placed **on** the colored shade as a background (auto-contrast, WCAG AA) |
| `--dwc-color-{name}-tint` | Seed at 12% opacity, alt backgrounds |
| `--dwc-border-color-{name}-emphasis` | Stronger mode-aware border for hover/focus/active |
| `--dwc-font-weight-medium` | 500-weight (the value `semibold` had in v25) |
| `--dwc-focus-ring-gap` | Gap between the element and the focus ring |
| `--dwc-scale-press`, `--dwc-scale-press-deep` | Press feedback (replaces ripple) |
| `--dwc-shadow-strength` | Multiplier for shadow intensity; ramps up automatically in dark mode |
| `--dwc-border-radius-3xl`, `-4xl` | New large-radius steps |

### Numeric defaults that shifted

| Variable | v25 | v26 |
|---|---|---|
| `--dwc-font-size-2xs` | 12px | 11px |
| `--dwc-font-size-xs` | 13px | 12px |
| `--dwc-font-size-s` | 14px | 13px |
| `--dwc-font-size-m` | 16px | 14px |
| `--dwc-font-size-l` | 18px | 16px |
| `--dwc-font-size-xl` | 22px | 20px |
| `--dwc-font-size-2xl` | 28px | 26px |
| `--dwc-font-size-3xl` | 36px | 34px |
| `--dwc-font-weight-semibold` | 500 | 600 |
| `--dwc-space-3xs` | 1.2px | 1px |
| `--dwc-space-2xs` | 2.4px | 2px |
| `--dwc-focus-ring-width` | 3px | 2px |
| `--dwc-focus-ring-a` | 0.4 | 0.75 |
| `--dwc-transition-slow` | 500ms | 300ms |
| `--dwc-transition-x-fast` | 50ms | 100ms |
| `--dwc-border-radius` unit | `em` | `rem` |
| `--dwc-border-radius` default | 4px | 8px |

The default body font size still resolves to **14px** (via `--dwc-font-size-m` in v26 vs `--dwc-font-size-s` in v25).

## Mechanics changed

### Palette generator

v25 used HSL with a per-step lightness scheme that flipped in dark mode (step 5 was darkest in light, lightest in dark). v26 uses OKLCH with perceptually uniform steps: **step 5 is always darkest, step 95 is always lightest**, regardless of mode. Mode adaptation happens in the variation layer, not the step layer:

```css
/* v26 - variations point at fixed steps; dark mode swaps via --dwc-dark-mode */
--dwc-color-primary-dark:  var(--dwc-color-primary-45);
--dwc-color-primary:       var(--dwc-color-primary-50);
--dwc-color-primary-light: var(--dwc-color-primary-55);
```

The implication: CSS that uses **variation tokens** (`--dwc-color-primary`, `-dark`, `-light`, `-text`, `-alt`) keeps the same mode-aware behavior as before. CSS that hardcodes **raw steps** (`--dwc-color-primary-40`) sees the same OKLCH lightness in both modes.

### Dark mode

v25 dark themes redefined surfaces, shadows, and palette variations manually. v26 derives all of that from a single variable:

```css
html[data-app-theme='my-dark-theme'] {
  --dwc-dark-mode: 1;
  --dwc-color-primary-h: 280;
  color-scheme: dark;
}
```

If a v25 custom dark theme had a 20-line override block, most lines can usually be deleted in v26.

### Seed colors are seeds, not targets

In v26 the hue you set via `--dwc-color-{name}-h`/`-s` (or `-seed`) is **not guaranteed to appear at step 50**. Because lightness steps are absolute OKLCH values, where the seed lands depends on its natural lightness. Bright hues (cyan, yellow) settle near step 80-85; darker hues land around 50. To pin an exact color at an exact step, set the step explicitly:

```css
:root {
  --dwc-color-primary-50: #1d4ed8;
}
```

### Border radius is seeded

```css
:root {
  --dwc-border-radius-seed: 0.5rem; /* default; rescales s..4xl */
}
```

`2xs` and `xs` remain fixed pixel values. If components feel too rounded after upgrade and you want v25 sizing, halve the seed:

```css
:root { --dwc-border-radius-seed: 0.25rem; /* 4px */ }
```

### Focus ring has a gap

The ring now has a surface-colored gap before the colored ring. If CSS reserves padding for the ring, add the gap:

```css
/* v25 */
dwc-button { padding: var(--dwc-focus-ring-width); }

/* v26 */
dwc-button {
  padding: calc(var(--dwc-focus-ring-width) + var(--dwc-focus-ring-gap));
}
```

### Press feedback

Material-style ripples are gone. The new feedback is a small scale-down:

```css
--dwc-scale-press: 0.97;       /* 3% shrink */
--dwc-scale-press-deep: 0.93;  /* 7% shrink, used for buttons */
```

## Pragmatic upgrade checklist

Run inside the project root. The greps below are designed to be safe on any stylesheet or `@InlineStyleSheet` block.

```bash
# 1. Delete -c overrides (no-op in v26)
grep -rn -- "--dwc-color-[a-z]*-c\b" src

# 2. Replace legacy shadow-color triplet form
grep -rn "hsla(var(--dwc-shadow-color)" src

# 3. Find direct palette step references (consider switching to variations)
grep -rn -E -- "--dwc-color-[a-z]+-[0-9]+" src

# 4. Find named font-size references (the scale shifted down one bucket)
grep -rn -E -- "--dwc-font-size-(2?xs|s|m|l|xl|2xl|3xl)" src

# 5. Find semibold uses (now 600, not 500)
grep -rn -- "--dwc-font-weight-semibold" src

# 6. Find focus-ring-width padding reservations (add the gap)
grep -rn -- "--dwc-focus-ring-width" src
```

After applying changes, validate with `webforj-mcp:styles_validate_tokens` against `webforjVersion: "26.00"` and re-list any palette to confirm the names you used still exist in the v26 catalog.

## Behavior preserved (no action)

These continue to work in v26 with no edits:

- `--dwc-color-{name}-h` and `-s` (HSL seed path)
- `--dwc-color-{name}` and the variation siblings `-dark` / `-light`
- `--dwc-surface-1` / `-2` / `-3` (now mode-adaptive automatically)
- `--dwc-shadow-xs` ... `--dwc-shadow-2xl` (retuned but same name and count)
- Custom themes scoped under `html[data-app-theme='your-name']`
- The component-level `theme` attribute (`default`, `primary`, `success`, ...)
- The component-level `expanse` attribute (`xs`, `s`, `m`, `l`, `xl`)
ok