# SPT ID Highlighter Theme Presets

This directory contains example theme presets that you can import into the SPT ID Highlighter plugin.

## How to Use Theme Presets

1. **Load a Theme:**
    - Open IntelliJ/Rider settings
    - Navigate to **Tools → SPT ID Highlighter**
    - Go to the **General** tab
    - Click **"Load Theme..."** in the Theme Preset Management section
    - Select a `.spttheme` file

2. **Save Your Own Theme:**
    - Configure your highlighting and popup settings
    - Click **"Save Current Theme..."**
    - Enter a name, description, and author
    - Choose where to save the file

3. **Share Your Theme:**
    - Export your theme using **"Export Theme..."**
    - Share the `.spttheme` file with other developers
    - They can load it using the steps above

## Example Themes

### Cyberpunk Theme

**File:** `Cyberpunk_Theme.spttheme`

A vibrant cyberpunk-inspired theme with neon colors:

- **Items:** Cyan (`#00ffff`)
- **Quests:** Magenta (`#ff00ff`)
- **Traders:** Yellow (`#ffff00`)
- **Locations:** Green (`#00ff00`)
- **Ammo:** Orange (`#ff6600`)
- **Weapons:** Hot Pink (`#ff0066`)
- **Customization:** Purple (`#9900ff`)
- **Popup:** Dark blue background with magenta border

## Creating Your Own Themes

Theme files are simple JSON files with the following structure:

```json
{
  "name": "My Theme Name",
  "description": "Optional description",
  "author": "Your Name",
  "highlightBold": false,
  "highlightItalic": true,
  "highlightUnderline": true,
  "highlightBackground": false,
  "colorItem": "#00ffff",
  "colorQuest": "#ff00ff",
  "colorTrader": "#ffff00",
  "colorLocation": "#00ff00",
  "colorAmmo": "#ff6600",
  "colorWeapon": "#ff0066",
  "colorCustomization": "#9900ff",
  "popupDelayMs": 300,
  "popupBackgroundColor": "#1a1a2e",
  "popupForegroundColor": "#eeeeff",
  "popupBorderColor": "#ff00ff"
}
```

### Fields

- **name** (required): Display name of your theme
- **description**: Brief description of the theme
- **author**: Your name or username
- **highlightBold/Italic/Underline/Background**: Text style options (boolean)
- **color***: Hex color codes for different ID types (null to use defaults)
- **popupDelayMs**: Milliseconds before popup appears (default: 300)
- **popup*Color**: Hex color codes for popup styling (null to use IDE defaults)

## Sharing Your Themes

If you create a cool theme, consider sharing it with the SPT modding community:

1. Create a pull request to add it to this directory
2. Share it on the SPT Discord or forums
3. Include a screenshot showing the theme in action

## Built-in Presets

The plugin includes these quick-access presets in the Popup tab:

- **Default (IDE)** - Uses your IDE's theme
- **Light Theme** - Clean light gray theme
- **Dark Theme** - Sleek dark gray theme
- **Blue** - Blue-tinted theme
- **Red** - Red-tinted theme
- **Purple** - Purple-tinted theme
