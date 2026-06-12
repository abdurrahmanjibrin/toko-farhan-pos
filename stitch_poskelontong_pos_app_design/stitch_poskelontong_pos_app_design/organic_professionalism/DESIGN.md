---
name: Organic Professionalism
colors:
  surface: '#fbf9f7'
  surface-dim: '#dbdad7'
  surface-bright: '#fbf9f7'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f1'
  surface-container: '#efeeeb'
  surface-container-high: '#e9e8e6'
  surface-container-highest: '#e4e2e0'
  on-surface: '#1b1c1b'
  on-surface-variant: '#434844'
  inverse-surface: '#30312f'
  inverse-on-surface: '#f2f0ee'
  outline: '#737873'
  outline-variant: '#c3c8c2'
  surface-tint: '#4f6356'
  primary: '#4f6356'
  on-primary: '#ffffff'
  primary-container: '#9db2a3'
  on-primary-container: '#324539'
  inverse-primary: '#b6ccbc'
  secondary: '#4a6176'
  on-secondary: '#ffffff'
  secondary-container: '#cae2fb'
  on-secondary-container: '#4e657a'
  tertiary: '#745758'
  on-tertiary: '#ffffff'
  tertiary-container: '#c8a4a5'
  on-tertiary-container: '#543a3b'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d2e8d8'
  primary-fixed-dim: '#b6ccbc'
  on-primary-fixed: '#0d1f15'
  on-primary-fixed-variant: '#384b3f'
  secondary-fixed: '#cde5fe'
  secondary-fixed-dim: '#b1c9e1'
  on-secondary-fixed: '#031d30'
  on-secondary-fixed-variant: '#32495d'
  tertiary-fixed: '#ffdada'
  tertiary-fixed-dim: '#e3bdbe'
  on-tertiary-fixed: '#2b1617'
  on-tertiary-fixed-variant: '#5b4041'
  background: '#fbf9f7'
  on-background: '#1b1c1b'
  surface-variant: '#e4e2e0'
typography:
  headline-lg:
    fontFamily: Nunito Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Nunito Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  title-lg:
    fontFamily: Nunito Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Nunito Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Nunito Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Nunito Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  margin-mobile: 20px
  gutter-mobile: 16px
  touch-target-min: 48px
---

## Brand & Style

The brand personality is rooted in the concept of a "trusted neighborhood partner." It balances the organic, calming nature of a traditional small-shop environment with the efficiency of modern technology. The UI is designed to feel approachable and non-intimidating for users who may not be digital natives, emphasizing clarity over complexity.

This design system utilizes a **Modern Tactile** style. It leverages soft depth, generous white space, and a comforting color palette to reduce "tech-anxiety." Every interaction is designed to feel intentional and physically grounded, using soft shadows and large touch targets to provide clear visual affordances for every action.

## Colors

The palette is inspired by natural, muted tones to create a low-stress environment for high-frequency daily tasks.

- **Primary (Soft Sage):** Reserved for primary calls-to-action, success states, and key navigational highlights. It signals "go" and "growth" without the harshness of high-vibrancy greens.
- **Secondary (Dusty Blue):** Used for supportive information, badges, and secondary buttons. It provides a cool contrast to the warm background.
- **Neutral Background (Warm Cream):** Reduces eye strain during long shifts. It feels more welcoming and "homely" than a clinical pure white background.
- **Surface (Pure White):** Used for cards and interactive containers to make them "pop" against the cream background.
- **Typography (Dark Charcoal):** High-contrast for readability while avoiding the starkness of pure black.

## Typography

This design system prioritizes legibility by using **Nunito Sans**, a font known for its rounded terminals and friendly, open letterforms. 

The type scale is intentionally larger than standard mobile apps to accommodate users in fast-paced environments or those with varying visual acuity. The minimum size for any interactive label or body text is 14px, with 16px being the standard for all primary reading tasks. Font weights are used sparingly—Bold for high-level hierarchy and Regular for general information—ensuring a clean, uncluttered interface.

## Layout & Spacing

The layout follows a **Fluid Grid** model optimized for handheld mobile use. 

- **Grid:** A 4-column layout for mobile devices with a 20px outer margin to prevent thumbs from obscuring content at the screen edges.
- **Rhythm:** An 8px-based spatial system ensures consistent vertical rhythm.
- **Touch Targets:** All interactive elements (buttons, toggles, list items) must maintain a minimum height of 48px to ensure they are easily tappable by users of all ages and dexterity levels.
- **Padding:** Generous internal padding (minimum 16px) is used within cards and containers to keep the UI feeling airy and organized.

## Elevation & Depth

Hierarchy is established through **Ambient Shadows** and **Tonal Layering**. 

Interactive containers (Cards) are rendered in Pure White and sit atop the Warm Cream background with a soft, diffused drop shadow. The shadow uses a low-opacity Dark Charcoal tint (#2D3748 at 8-12% opacity) with a large blur radius (12px to 20px). This "floating" effect clearly distinguishes the interactive surface from the static background. 

When an item is pressed, it should visually "sink" (shadow becomes smaller and sharper) to provide tactile feedback, mimicking a physical button.

## Shapes

The shape language is consistently **Rounded**. 

The use of 12dp to 16dp corner radii removes "visual sharpness" from the interface, reinforcing the friendly and safe brand personality. 
- **Small Elements (Chips, Badges):** 8px (rounded-md)
- **Medium Elements (Buttons, Input Fields):** 12px (rounded-lg)
- **Large Elements (Cards, Modals, Bottom Sheets):** 16px to 24px (rounded-xl)

All corners should feel soft and "pill-like" where possible, especially for action-oriented components.

## Components

- **Buttons:** Primary buttons use the Soft Sage background with White text. Secondary buttons use the Dusty Blue background or a Sage outline. All buttons have a minimum height of 48px and use SemiBold typography.
- **Input Fields:** Use a subtle 1px border (#D1D5DB) or a slightly darker cream fill. Labels always sit above the input field, never inside as placeholders, to maintain visibility during data entry.
- **Cards:** The central container for product items and transaction details. They feature a 16px corner radius and a soft shadow.
- **Chips & Badges:** Used for category filters and status indicators (e.g., "Paid," "Pending"). They use the Dusty Blue palette with high-contrast text.
- **List Items:** Feature a minimum 56px height with 16px horizontal padding. A subtle bottom divider (#E2E8F0) separates items without creating visual noise.
- **Numeric Keypad:** Custom-designed for the POS experience with oversized keys (64px+ height) and haptic feedback to ensure fast, error-free price entry.