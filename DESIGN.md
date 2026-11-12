---
name: "轻点"
description: "A lightweight native Android task console for controlled repetitive taps."
colors:
  primary: "#2458BD"
  on-primary: "#FFFFFF"
  background: "#F6F8FC"
  surface: "#FFFFFF"
  surface-variant: "#EAF0FA"
  text: "#182438"
  text-secondary: "#536279"
  outline: "#CDD6E4"
  success: "#266344"
  error: "#B3261E"
  dark-primary: "#AEC6FF"
  dark-on-primary: "#102D63"
  dark-background: "#10151E"
  dark-surface: "#1A2230"
  dark-surface-variant: "#263349"
  dark-text: "#E6ECF7"
  dark-text-secondary: "#B5C2D8"
  dark-outline: "#465368"
  dark-success: "#9AD6B0"
  dark-error: "#FFB4AB"
typography:
  headline:
    fontFamily: "sans-serif-medium"
    fontSize: "26sp"
    fontWeight: 500
  title:
    fontFamily: "sans-serif-medium"
    fontSize: "20sp"
    fontWeight: 500
  field-value:
    fontFamily: "sans"
    fontSize: "18sp"
    fontWeight: 400
  item-title:
    fontFamily: "sans-serif-medium"
    fontSize: "16sp"
    fontWeight: 500
  button:
    fontFamily: "sans-serif-medium"
    fontSize: "15sp"
    fontWeight: 500
  body:
    fontFamily: "sans"
    fontSize: "14sp"
    fontWeight: 400
  label:
    fontFamily: "sans-serif-medium"
    fontSize: "14sp"
    fontWeight: 500
  detail:
    fontFamily: "sans"
    fontSize: "13sp"
    fontWeight: 400
  caption:
    fontFamily: "sans"
    fontSize: "12sp"
    fontWeight: 400
rounded:
  control: "12dp"
  surface: "16dp"
  circle: "24dp"
spacing:
  xs: "4dp"
  helper: "6dp"
  sm: "8dp"
  compact: "12dp"
  md: "16dp"
  lg: "24dp"
  section: "28dp"
  xl: "32dp"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button}"
    rounded: "{rounded.surface}"
    padding: "12dp 18dp"
  button-secondary:
    backgroundColor: "{colors.surface-variant}"
    textColor: "{colors.primary}"
    typography: "{typography.button}"
    rounded: "{rounded.surface}"
    padding: "12dp 18dp"
  numeric-input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    typography: "{typography.field-value}"
    rounded: "{rounded.control}"
    padding: "10dp 14dp"
  permission-row:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.surface}"
    padding: "16dp 12dp 16dp 16dp"
  mode-group:
    backgroundColor: "{colors.surface-variant}"
    textColor: "{colors.text}"
    rounded: "{rounded.surface}"
    padding: "4dp"
  target-marker:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.circle}"
    size: "48dp"
---

# Design System: 轻点

## Overview

**Creative North Star: "Android Quick-Settings Task Console"**

A quiet native console makes tapping explicit and controllable. Cold white surfaces, a blue action color, permission rows, and numeric controls support deliberate actions. Android supplies the dark theme.

Native Views, platform widgets, system fonts, and drawn icons keep the app small and familiar. This record describes the implementation; selected screenshots live in docs/images/.

**Key Characteristics:**

- One action accent with semantic success and error feedback.
- Scalable system type and generous touch targets.
- Scrollable settings and a separate movable overlay.
- Flat app surfaces; raised controls above other apps.

## Colors

Frontmatter records exact resource values. Unprefixed roles are light; dark-prefixed roles are their values-night counterparts. Components resolve corresponding roles at runtime.

### Primary

Blue primary marks actions, selected controls, test counts, and numbered targets. On-primary supplies ink inside filled controls.

### Neutral

Background forms the canvas; surface holds permission rows, fields, and the overlay. Surface-variant groups mode choices, secondary buttons, and the test area. Text and text-secondary separate primary content from instructions. Outline supplies the inset permission divider.

Success marks confirmed permission states; error marks validation feedback. They are semantic states, not additional brand accents.

**The Role Pair Rule.** Switch each background and foreground pair together through Android resources.

## Typography

System sans is intentional: sans for regular text and sans-serif-medium for emphasis, with Android locale fallback for Chinese glyphs. No bundled or downloaded font is needed.

The frontmatter records the hierarchy. Headline serves activity titles and the test counter; title serves sections; item-title serves permission names. Field-value serves numeric entries, button serves actions, body/detail serve instructions, label serves field names, and caption serves helpers and overlay status. Markers use 18sp medium numerals.

Ui.text removes extra font padding; native buttons and inputs retain platform metrics. Line height follows Android font metrics, with small line-spacing additions on selected instructions.

**The Scalable Type Rule.** Use sp for text and dp for geometry; let content grow and scroll with font scale.

## Layout

Settings use a centered column capped at 640dp in a fillViewport ScrollView, with 24dp top/side padding and 32dp bottom padding. Sections use 28dp separation. The recurring spacing scale is in frontmatter; helper gaps use 6dp. Timing fields share a row equally with a 12dp gutter, and radio choices share a row equally.

The test page uses a fillViewport ScrollView with 24dp side/bottom and 32dp top padding. Its target starts at 200dp with weight 1 and expands into spare height. Short viewports and large text scroll the complete page, keeping all three actions reachable. Portrait and landscape at 150% font scale were reviewed; docs/images/test-large-landscape.png preserves a selected capture.

Activity content respects system bars and display cutouts. Settings also include IME insets on Android 11+. There are no CSS breakpoints.

**The Coordinate Rule.** Overlay windows use physical top-left display coordinates, independent of locale. Target centers are stored as normalized screen coordinates. Panel and markers are clamped inside safe bounds and do not participate in activity scrolling.

## Elevation & Depth

Settings and test surfaces separate by tone, without elevation. Native elevation lifts the floating panel (6dp) and markers (3dp). Buttons disable the platform state-list elevation animator.

Press feedback uses Android RippleDrawable with primary ink at alpha 0x22. There is no custom motion system. The 180ms delay before restoring stopped markers protects gesture delivery; it is not an animation token.

## Shapes

Numeric fields and overlay icon controls use the control radius; buttons and grouped surfaces use the surface radius. Help buttons and markers use the circular radius. The permission divider is 1dp, inset 16dp on each side.

Icons are drawn in a centered 24dp canvas, with rounded stroke ends/joins and a 1.9-unit stroke in that canvas. They are not font glyphs or library assets.

## Components

### Buttons

Primary actions use primary/on-primary; secondary actions use surface-variant/primary. Both have a 52dp minimum height, frontmatter padding, centered medium text, native ripple, and no automatic capitalization. Height may grow with content.

### Permission rows

Rows have an 80dp minimum height and a weighted label column beside the status. The whole row is focusable and opens its Android setting. Confirmed states use success; actionable missing states use primary.

### Inputs and choices

Numeric EditText fields have a 52dp minimum height, an associated visible label, and helper text. Validation uses native field errors plus visible error text. Focus, selection, keyboard, and error affordances remain platform behaviors.

Mode choices are primary-tinted native RadioButtons, each at least 48dp high, inside the padded tonal group. End conditions use a native dropdown Spinner with a 52dp minimum height, a surface-colored 12dp rounded field, 16sp label, and drawn chevron. Its 16dp rounded menu matches the field width; 6dp outer padding surrounds 52dp-minimum rows with 16dp horizontal padding. The selected row uses surface-variant/primary with a trailing check. Native selection, dismissal, keyboard, and accessibility behavior remain in place. There are no navigation tabs or chips.

### Floating panel and targets

The panel is 288dp wide, with 8dp top/side and 10dp bottom padding. Five 48dp controls use 8dp gaps: drag, start/stop, add, remove, return to app. The return arrow stops taps, dismisses overlay windows, and brings the existing main settings screen forward. Only start/stop is filled. Icons have explicit content descriptions and native long-press tooltips; unavailable add/remove controls use 32% opacity.

Circular 48dp targets use medium numbered labels. Dragging moves their tap centers. They hide during countdown/execution and return when idle. Panel status shows countdown or running counts while preserving immediate stop.

### Test target

A tonal rounded region centers the primary-colored click count and provides native ripple. Its counter reports clicks received by that activity. It follows the scroll-and-grow layout above.

### Usage guide

The optional help dialog uses a 16dp rounded surface capped at 360dp wide, with 24dp internal padding and at least 24dp outside margins. Its 22sp title leads three short, sequential instructions, a tonal start/stop/return icon legend, one drag hint, and a full-width primary dismiss button. Instructions use 14sp, other labels 13sp. Content scrolls on short viewports or with enlarged system text. It contains usage instructions only; first-use permission consent is separate.

The legend has three equal-width columns. Each label fills its column and centers its text directly below its icon, including when the label wraps.

## Do's and Don'ts

### Do:

- **Do** resolve light and dark role pairs through Android resources.
- **Do** keep interactive icons and markers at least 48dp and text in sp.
- **Do** preserve reachable actions with scrollable content and system insets.
- **Do** use native feedback and explicit start/stop state.

### Don't:

- **Don't** add font or icon libraries for an appearance already supplied by the platform.
- **Don't** replace physical overlay coordinates with activity-relative positions.
- **Don't** use elevation on flat settings groups.
- **Don't** treat sidecar preview CSS or synthesized swatch ramps as Android implementation tokens.
