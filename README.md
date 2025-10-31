# SPT ID Highlighter for JetBrains Rider

This plugin aids in the development of the [Single Player Tarkov](https://github.com/sp-tarkov) project by providing additional information about items, quests, and other game objects directly within JetBrains Rider.

## Features

- **Hover Information**: Hover over a SPT ID to see detailed information about the object.
- **Visual Highlighting**: SPT IDs are highlighted with italic and underline styling in your code.
- **Multilingual Support**: Choose from 17 languages for item names and descriptions.
- **Custom ID Support**: Add a `.sptids` file to your project to provide information on custom IDs.

## Requirements

- JetBrains Rider 2025.2 or later
- Java 17 or later

## Installation

### From JetBrains Marketplace (Coming Soon)
1. Open Rider
2. Go to **File → Settings → Plugins**
3. Search for "SPT ID Highlighter"
4. Click **Install**

### Manual Installation
1. Download the latest release from the [releases page](https://github.com/madmanbeavisx/spt-id-highlighter/releases)
2. Open Rider
3. Go to **File → Settings → Plugins**
4. Click the gear icon and select **Install Plugin from Disk...**
5. Select the downloaded `.zip` file


## Custom ID Support

You can add custom IDs by creating a `.sptids` file in your project root. The file should follow this structure:

```json
{
  "your_custom_id_here": {
    "en": {
      "Name": "Custom Item Name",
      "ShortName": "Custom",
      "Type": "ITEM",
      "Weight": 0.5,
      "Description": "A custom item"
    }
  }
}
```

Each ID can have translations for any supported language. The plugin will automatically detect changes to this file and reload the data.

[See the wiki for more examples](https://github.com/madmanbeavisx/spt-id-highlighter/wiki/Custom-ID-Support)

## Development

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/madmanbeavisx/spt-id-highlighter.git
   cd spt-id-highlighter
   ```

2. Build the database files:
   ```bash
   ./gradlew buildData
   ```

3. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```

The plugin will be available in `build/distributions/`.

### Project Structure

```
src/main/
├── kotlin/com/madmanbeavis/sptidHighlighter/
│   ├── annotator/          # Text highlighting
│   ├── documentation/      # Hover documentation provider
│   ├── listeners/          # Project lifecycle listeners
│   ├── models/             # Data models
│   ├── services/           # Core services (data loading, file watching)
│   └── settings/           # Settings UI and state
└── resources/
    ├── assets/             # Raw SPT data from game files
    ├── database/           # Processed database files (generated)
    ├── translations/       # UI translations
    └── META-INF/
        └── plugin.xml      # Plugin configuration
```


## Known Issues

No known issues at this time. Please report any issues on the [GitHub issues page](https://github.com/madmanbeavisx/spt-id-highlighter/issues).

## License

This plugin is licensed under the [MIT](LICENSE). The locale assets are provided by the [SPT project](https://github.com/sp-tarkov/server) and are licensed under the [NCSA License](src/main/resources/assets/LICENSE).

## Credits

Developed by [MadManBeavis](https://github.com/madmanbeavisx)(https://github.com/madmanbeavisx)
Original code for VSCode by [Refringe](https://github.com/refringe)(https://github.com/refringe)  
  
## Thanks
Thanks for [Refringe](https://github.com/refringe) giving his blessing for this to be published.
