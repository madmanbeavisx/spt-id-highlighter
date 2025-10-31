# Development Guide

## Testing the Plugin Locally

There are two ways to test the plugin:

### Option 1: Run Plugin in Development Mode (Recommended)

This launches a new Rider instance with your plugin installed:

1. **Build the database files first:**
   ```bash
   ./gradlew buildData
   ```

2. **Run the plugin in a development Rider instance:**
   ```bash
   ./gradlew runIde
   ```

   This will:
   - Download the Rider IDE if needed
   - Compile your plugin
   - Launch a new Rider window with your plugin installed
   - Any changes you make will require rerunning this command

### Option 2: Install Plugin Manually

Build and install the plugin into your current Rider installation:

1. **Build the database files:**
   ```bash
   ./gradlew buildData
   ```

2. **Build the plugin ZIP:**
   ```bash
   ./gradlew buildPlugin
   ```

3. **Install in Rider:**
   - Open your Rider IDE
   - Go to **File → Settings → Plugins**
   - Click the gear icon (⚙️) → **Install Plugin from Disk...**
   - Navigate to `build/distributions/spt-id-highlighter-1.0.0.zip`
   - Click **OK**
   - Restart Rider when prompted

4. **Verify Installation:**
   - After restart, go to **File → Settings → Plugins**
   - Search for "SPT ID Highlighter"
   - It should appear in the Installed list

## Testing the Features

1. **Test Hover Documentation:**
   - Create or open a TypeScript or JSON file
   - Type a valid SPT ID (24 characters, e.g., `5449016a4bdc2d6f028b456f`)
   - Hover over it with your mouse
   - You should see documentation popup

2. **Test Visual Highlighting:**
   - SPT IDs should appear with italic and underlined text

3. **Test Settings:**
   - Go to **File → Settings → Tools → SPT ID Highlighter**
   - Change the language
   - Click **OK**
   - Hover over an ID to see if the language changed

4. **Test Custom IDs:**
   - Create a `.sptids` file in your project root
   - Add custom ID data (see README.md for format)
   - Save the file
   - The plugin should automatically reload
   - Hover over your custom ID to verify

## Debugging

### Enable Debug Logging

Add this to your run configuration or when launching:

```bash
./gradlew runIde --debug-jvm
```

Then attach a debugger to port 5005.

### View Plugin Logs

When running via `runIde`, logs appear in the console. Look for lines starting with:
- `SPT Data Service`
- `SPT ID Highlighter`

### Common Issues

**Issue: "Database files not found"**
- Solution: Run `./gradlew buildData` first

**Issue: "Plugin doesn't highlight IDs"**
- Check that you're in a TypeScript or JSON file
- Verify the ID is exactly 24 characters
- Run `buildData` to ensure database is populated

**Issue: "Settings page doesn't appear"**
- Check `META-INF/plugin.xml` is properly configured
- Restart Rider completely

## Development Workflow

1. Make code changes
2. If you changed data processing: `./gradlew buildData`
3. Test: `./gradlew runIde`
4. Iterate

## Building for Release

```bash
./gradlew buildData
./gradlew buildPlugin
```

The distributable ZIP will be in `build/distributions/`.
