# .sptids File Format Guide

## Overview
The `.sptids` file allows you to define custom SPT items, quests, traders, and more. The file should be placed in your project root or any subdirectory.

## File Format

### Basic Structure
```json
{
  "item_id_here": {
    "en": {
      "Name": "Item Name",
      "ShortName": "Short",
      "Type": "ITEM"
    }
  }
}
```

### Supported Types
- `ITEM` - General items
- `WEAPON` - Weapons
- `AMMO` - Ammunition
- `ARMOR` - Armor pieces
- `QUEST` - Quests
- `TRADER` - Traders
- `LOCATION` - Maps/locations
- `CUSTOMIZATION` - Character customization

## Quest Format

For quests, use this format to get proper tooltip information:

```json
{
  "quest_id_672419a8d4f7b3c2e8a91f23": {
    "en": {
      "Name": "Eliminate the Target",
      "ShortName": "Eliminate",
      "Type": "QUEST",
      "Trader": "Prapor",
      "TraderId": "54cb50c76803fa8b248b4571",
      "TraderLink": "https://example.com/trader/prapor",
      "QuestType": "Elimination"
    }
  }
}
```

### Quest Fields Explained

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| `Name` | ✅ Yes | Full quest name | `"Eliminate the Target"` |
| `ShortName` | ✅ Yes | Short name | `"Eliminate"` |
| `Type` | ✅ Yes | Must be `"QUEST"` | `"QUEST"` |
| `Trader` | ❌ No | Trader name | `"Prapor"` |
| `TraderId` | ❌ No | Trader MongoDB ID | `"54cb50c76803fa8b248b4571"` |
| `TraderLink` | ❌ No | URL to trader details | `"https://..."` |
| `QuestType` | ❌ No | Type of quest | `"Elimination"`, `"Collection"`, `"Exploration"` |

### Tooltip Display

When you hover over a quest ID, you'll see:

```
Eliminate the Target [Eliminate]
━━━━━━━━━━━━━━━━━━━━━━━━━━
Type: QUEST
Trader: Prapor - 54cb50c76803fa8b248b4571
Quest Type: Elimination
```

## Other Item Types

### Weapon
```json
{
  "weapon_id": {
    "en": {
      "Name": "Custom AK-47",
      "ShortName": "C-AK47",
      "Type": "WEAPON",
      "Weight": 3.5,
      "FleaBlacklisted": false
    }
  }
}
```

### Ammo
```json
{
  "ammo_id": {
    "en": {
      "Name": "Custom 5.56x45",
      "ShortName": "C-556",
      "Type": "AMMO",
      "Caliber": "Caliber556x45NATO",
      "Damage": 60,
      "ArmorDamage": 40,
      "PenetrationPower": 35,
      "Weight": 0.012
    }
  }
}
```

### Trader
```json
{
  "trader_id": {
    "en": {
      "Name": "Custom Trader",
      "ShortName": "CT",
      "Type": "TRADER",
      "Currency": "RUB",
      "UnlockedByDefault": true
    }
  }
}
```

### Location/Map
```json
{
  "location_id": {
    "en": {
      "Name": "Custom Map",
      "ShortName": "CMap",
      "Type": "LOCATION",
      "Id": "custom_map_id",
      "EscapeTimeLimit": 40,
      "Insurance": true,
      "AirdropChance": 0.25
    }
  }
}
```

## Field Name Reference

### Common Fields (All Types)
- `Name` - Full name (required)
- `ShortName` - Short name (required)
- `Type` - Item type enum (required)
- `Weight` - Weight in kg
- `FleaBlacklisted` - Boolean
- `QuestItem` - Boolean
- `Description` - Text description

### Quest-Specific Fields
- `Trader` - Trader name
- `TraderId` - Trader MongoDB ObjectId (24 hex chars)
- `TraderLink` - URL to trader documentation
- `QuestType` - Quest category

**Note:** Field names are **case-sensitive**. Use the exact capitalization shown above.

## Multi-Language Support

You can add multiple languages:

```json
{
  "quest_id": {
    "en": {
      "Name": "Eliminate Target",
      "ShortName": "Eliminate",
      "Type": "QUEST",
      "Trader": "Prapor",
      "QuestType": "Elimination"
    },
    "ru": {
      "Name": "Уничтожить цель",
      "ShortName": "Уничтожить",
      "Type": "QUEST",
      "Trader": "Прапор",
      "QuestType": "Устранение"
    }
  }
}
```

The plugin will use the language configured in: **File → Settings → Tools → SPT ID Highlighter**

## Example Complete .sptids File

```json
{
  "672419a8d4f7b3c2e8a91f23": {
    "en": {
      "Name": "Eliminate Killa",
      "ShortName": "Kill Killa",
      "Type": "QUEST",
      "Trader": "Prapor",
      "TraderId": "54cb50c76803fa8b248b4571",
      "QuestType": "Elimination"
    }
  },
  "672419a8d4f7b3c2e8a91f24": {
    "en": {
      "Name": "Collect Documents",
      "ShortName": "Documents",
      "Type": "QUEST",
      "Trader": "Therapist",
      "TraderId": "54cb57776803fa99248b456e",
      "QuestType": "Collection"
    }
  },
  "672419a8d4f7b3c2e8a91f25": {
    "en": {
      "Name": "Custom Weapon Mod",
      "ShortName": "CWM",
      "Type": "ITEM",
      "Weight": 0.5,
      "FleaBlacklisted": false
    }
  }
}
```

## Validation Tips

1. **Use a JSON validator** - Paste your `.sptids` content at jsonlint.com
2. **Check for trailing commas** - Last entry should NOT have a comma
3. **Verify all braces match** - Every `{` needs a matching `}`
4. **Use proper capitalization** - `"Type": "QUEST"` not `"type": "quest"`
5. **MongoDB IDs are 24 hex characters** - Use the MongoDB ID generator (Ctrl+Shift+Alt+W)

## Testing Your .sptids File

1. Save your `.sptids` file
2. Restart Rider (or wait for auto-reload)
3. Check logs: **Help → Diagnostic Tools → Show Log in Explorer**
4. Search for "sptids" to see loading status
5. Use **Tools → SPT ID Highlighter: Show Diagnostics** to verify items loaded
6. Hover over an ID in your code to see the tooltip

## Need Help?

If your `.sptids` file isn't loading:
1. Check the IDE logs for "Failed to load .sptids file"
2. Validate your JSON syntax
3. Verify field names match exactly (case-sensitive)
4. Ensure the file is in your project directory (not in excluded folders)
