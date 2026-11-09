---
name: PagoControl Design System
colors:
  surface: '#f9faf3'
  surface-dim: '#d9dbd4'
  surface-bright: '#f9faf3'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f4ed'
  surface-container: '#edefe8'
  surface-container-high: '#e7e9e2'
  surface-container-highest: '#e1e3dc'
  on-surface: '#191c18'
  on-surface-variant: '#4e4634'
  inverse-surface: '#2e312d'
  inverse-on-surface: '#f0f1ea'
  outline: '#807662'
  outline-variant: '#d1c5ae'
  surface-tint: '#765b00'
  primary: '#765b00'
  on-primary: '#ffffff'
  primary-container: '#f4c542'
  on-primary-container: '#6b5200'
  inverse-primary: '#efc13e'
  secondary: '#006c48'
  on-secondary: '#ffffff'
  secondary-container: '#94f6c4'
  on-secondary-container: '#00734d'
  tertiary: '#ac3231'
  on-tertiary: '#ffffff'
  tertiary-container: '#ffbab5'
  on-tertiary-container: '#9f292a'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdf94'
  primary-fixed-dim: '#efc13e'
  on-primary-fixed: '#241a00'
  on-primary-fixed-variant: '#594400'
  secondary-fixed: '#94f6c4'
  secondary-fixed-dim: '#78d9aa'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffdad7'
  tertiary-fixed-dim: '#ffb3ae'
  on-tertiary-fixed: '#410004'
  on-tertiary-fixed-variant: '#8b191d'
  background: '#f9faf3'
  on-background: '#191c18'
  surface-variant: '#e1e3dc'
typography:
  display-currency:
    fontFamily: Manrope
    fontSize: 38px
    fontWeight: '800'
    lineHeight: 44px
  display-currency-mobile:
    fontFamily: Manrope
    fontSize: 30px
    fontWeight: '800'
    lineHeight: 36px
  headline-lg:
    fontFamily: Manrope
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Manrope
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  headline-sm:
    fontFamily: Manrope
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-lg:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-md:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Manrope
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
  currency-tabular:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '700'
    lineHeight: 22px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  margin: 1rem
  margin-tablet: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system is engineered for an automated personal finance management application on Android (Material Design 3 / Jetpack Compose), crafted specifically for the Peruvian market. The brand personality is dependable, agile, and transparent, transforming complex automated transaction reading (SMS, bank notifications, push alerts from local digital wallets) into effortless clarity.

The aesthetic blends **Modern Material Design 3** with a **Tactile-Refined Finish**:
- **Clarity over clutter:** Dense financial data is organized into high-legibility cards with generous internal touch targets and soft, distinct surface elevations.
- **Peruvian digital fluency:** Native visual recognition for local payment infrastructure (Yape, Plin, BCP, BBVA, Interbank) through calibrated badge systems and source tags.
- **Energetic confidence:** An authoritative ochre-yellow anchor conveys optimism and control, always paired with deep obsidian typography for strict WCAG AAA contrast compliance.

## Colors

The color palette enforces deliberate semantic mapping to handle automated financial inputs, alerts, and transactional flow.

### Light Mode Palette
- **Primary Yellow (`#F4C542`):** Primary action triggers, FAB button, active indicators, and high-priority metrics. Text on this surface must always be Text Primary (`#171A16`).
- **Primary Pressed (`#D9A91C`):** State layer for interactive primary triggers.
- **Background (`#FAFAF7`):** Warm off-white canvas preventing digital eye fatigue.
- **Surface (`#FFFFFF`):** Base container for cards, transaction lists, and bottom sheets.
- **Surface Secondary (`#F4F4EF`):** Input containers, nested chips, and inactive card backgrounds.
- **Text Primary (`#171A16`):** Dominant label color for balances, merchant names, and titles.
- **Text Secondary (`#73776F`):** Contextual metadata, timestamps, notification sources, and descriptions.
- **Border (`#E7E9E1`):** Structural division lines and card borders.
- **Income Green (`#16845B`):** Credits, inbound transfers, budget surpluses, and savings gains.
- **Expense Red (`#D9534F`):** Debits, outgoing payments, fees, and exceeded budgets.
- **Warning Orange (`#E59A23`):** Uncategorized automated transactions, sync alerts, and budget limits.

### Dark Mode Palette
- **Primary Yellow (`#FFD45A`):** High-luminance accent for dark surfaces. Paired with `#111410` text.
- **Primary Pressed (`#E5B933`):** Active interaction state.
- **Background (`#111410`):** Deep olive-tinted obsidian base.
- **Surface (`#1B201A`):** Card elevation level 1.
- **Surface Secondary (`#252B23`):** Card elevation level 2 and nested pills.
- **Text Primary (`#F5F6F0`):** High-contrast readout.
- **Text Secondary (`#A8B0A4`):** Secondary metadata.
- **Border (`#353C33`):** Subtle card stroke.
- **Income Green (`#55D59C`):** High-legibility credit indicators.
- **Expense Red (`#FF7777`):** Debit alerts.
- **Warning Orange (`#FFBC57`):** System notifications.

### Institution & Channel Badges
- **Yape:** Surface `#742284` with text `#FFFFFF`
- **Plin:** Surface `#00B5E2` with text `#FFFFFF`
- **BCP:** Surface `#002A8F` with accent `#FF7800`
- **Interbank:** Surface `#009B3A` with text `#FFFFFF`
- **BBVA:** Surface `#004481` with text `#FFFFFF`
- **Gmail / SMS Parser:** Surface `#EA4335` with text `#FFFFFF`

## Typography

The design system relies entirely on **Manrope**, chosen for its geometric precision, contemporary rhythm, and clear numeric differentiation.

### Currency Formatting Rules
- Peruvian Sol currency values must always follow the format: `S/ 1,250.00` or `S/ 48.50`.
- The prefix `S/ ` is displayed at the same font weight or one step lighter (`SemiBold 600`) than the numeric value to emphasize data magnitude.
- Positive transactions (Credits) must prefix with `+ S/ `, while negative transactions (Debits) must prefix with `- S/ `.
- Financial tables and transaction list amounts must enable OpenType tabular numbers (`tnum`) to maintain vertical decimal alignment across dynamic updates.

## Layout & Spacing

The layout is built for fluid mobile-first Android devices conforming to a strict **4dp / 8dp linear spacing model**.

### Grid and Margins
- **Mobile Handsets (< 600dp):** Single column fluid feed with `16dp` (`margin: 1rem`) screen padding on left and right borders. Gutters between horizontal summary cards are set to `12dp` (`gutter-sm: 0.75rem`).
- **Foldables & Tablets (600dp+):** Dual-pane layout (List on left, detail canvas on right) with `24dp` outer margins and `16dp` gutters.
- **Safe Areas:** Strict clearance for Android Edge-to-Edge display mode. The bottom navigation bar accounts for window insets plus navigation gesture heights.

### Internal Card Rhythm
- Standard card padding is strictly `16dp` (`space-md`), increasing to `20dp` on hero balance banners.
- Vertical space between discrete transaction rows inside list cards is `12dp`.

## Elevation & Depth

In line with Material Design 3 guidelines optimized for high-performance Jetpack Compose rendering, depth is communicated through **Tonal Surface Layering** combined with soft, directional micro-shadows.

### Surface Tiers (Light Mode)
- **Level 0 (Canvas):** Background (`#FAFAF7`).
- **Level 1 (Default Containers):** Surface (`#FFFFFF`) with a 1dp crisp outline of `#E7E9E1`. Zero blur or `offset: (0dp, 2dp)`, `blur: 6dp`, `color: rgba(23, 26, 22, 0.04)`.
- **Level 2 (Floating Action & Modals):** Surface (`#FFFFFF`) with `offset: (0dp, 8dp)`, `blur: 16dp`, `color: rgba(23, 26, 22, 0.08)`.
- **Primary Hero Card / FAB:** Highlighted using `#F4C542` with an ambient glow: `offset: (0dp, 4dp)`, `blur: 12dp`, `color: rgba(217, 169, 28, 0.35)`.

### Surface Tiers (Dark Mode)
- Ambient shadows are disabled. Depth is communicated strictly via tonal shifts (`#111410` base, `#1B201A` cards, `#252B23` secondary elevated surfaces) and 1dp border lines (`#353C33`).

## Shapes

The design system enforces a soft, modern geometry centered around **Level 2 (Rounded)** with purposeful variations for specialized mobile touch components:

- **Cards & Summary Banners:** `16dp` to `24dp` (`rounded-lg` to `rounded-xl`). Balance hero cards use `24dp`, while nested micro-cards use `16dp`.
- **Buttons & Input Fields:** `12dp` to `16dp`.
- **Pills, Transaction Badges & Bank Chips:** Full pill radius (`999dp`).
- **Bottom Sheets:** Top-left and top-right radii set to `28dp`, bottom edges flat (`0dp`).
- **Floating Action Button (FAB):** Rounded squircle (`16dp` radius) or classic circular pill (`28dp` radius) centered on the bottom navigation dock.

## Components

### Bottom Navigation Bar (5 Destinations)
- **Structure:** Anchored bottom bar housing 5 slots:
  1. *Inicio* (Home outline / filled icon)
  2. *Movimientos* (Receipt / list icon)
  3. *Agregar (+)* (Center prominent Yellow FAB `#F4C542`, `56dp x 56dp`, icon `#171A16`)
  4. *Reportes* (Pie chart / trend icon)
  5. *Perfil* (User avatar / settings icon)
- **Active State:** The active item features an icon fill and a bold title in Text Primary (`#171A16` / `#F5F6F0`). Inactive items use Text Secondary (`#73776F` / `#A8B0A4`).

### Buttons
- **Primary Button:** Background `#F4C542`, text `#171A16` (`label-lg`), height `52dp`, corner radius `14dp`. Active pressed state shifts to `#D9A91C`.
- **Secondary Button:** Surface Secondary (`#F4F4EF` / `#252B23`), text `#171A16` / `#F5F6F0`, border 1dp (`#E7E9E1` / `#353C33`).
- **Ghost / Destructive:** Transparent surface, red text (`#D9534F` / `#FF7777`).

### Transaction List Items
- **Layout:** Horizontal three-zone item:
  - *Leading:* `44dp` rounded circle or squircle showing the merchant icon or Peruvian bank badge.
  - *Center:* Top row with Merchant / Concept name (`title-md`), bottom row with Date, Time, and Source Channel pill (`body-sm`).
  - *Trailing:* Top row with formatted amount (`currency-tabular`), bottom row with automated status badge (e.g., "Auto-leído"). Positive amounts in `#16845B`, negative amounts in `#171A16` (or `#D9534F` if flagged/overdraft).

### Wallet & Channel Badges
- Compact pill indicators with `height: 22dp`, padding `2dp 8dp`, font `label-sm`.
- Distinct color pairs for fast visual triage: Yape (Purple), Plin (Cyan), BCP (Blue/Orange), BBVA (Navy), Interbank (Green), SMS (Neutral Slate).

### Cards
- **Account Summary Card:** High-contrast card housing total available balance (`display-currency`), income/expense month-to-date dual split chips, and active automated synchronization state ("Sincronizado vía SMS").
- **Transaction Card:** Grouped card housing multiple list rows with light divider lines (`#E7E9E1` / `#353C33`).

### Input Fields
- Filled container style with height `56dp`, background `#F4F4EF` (`#252B23` in Dark Mode), corner radius `12dp`. 
- Focus state switches bottom border or outline to `#F4C542` with 2dp thickness.