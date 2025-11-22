# Changelog

All notable changes to the SPT ID Highlighter plugin will be documented in this file.

## [1.5.0] - 11/20/2025

### Performance Optimization Release 🚀

This release addresses critical performance issues that caused IDE freezing and thread contention.

### Fixed

- **Critical: Thread Contention in Inlay Hints** - Fixed severe performance issue where `SptIdInlayHintProvider` caused
  6+ JobScheduler threads to block on expensive HashMap TreeNode operations
    - Replaced `HashSet<Int>` with `TIntHashSet` for O(1) primitive lookups without TreeNode overhead
    - Added `ProgressManager.checkCanceled()` calls to allow operation cancellation
    - Added limit of 500 hints per file to prevent unbounded memory growth
    - Reordered validation checks to perform fast checks before expensive service calls
- **Thread Safety** - Replaced `MutableMap` with `ConcurrentHashMap` in `SptDataService` for lock-free concurrent reads
- **UI Freezing** - Added cancellation checks and early exit conditions in `SptIdAnnotator` and
  `SptIdDocumentationProvider`
- **Excessive Recursion** - Limited recursion depth to 5 and children processing to 10 nodes in documentation provider
- **Logging Overhead** - Reduced logging noise by only logging when debug mode is enabled

### Changed

- **Performance Improvements**:
    - Pre-compiled regex patterns to avoid object creation on every call
    - Added early length validation checks (text must be ≥24 chars to contain SPT IDs)
    - Limited regex match processing to prevent excessive iterations
    - Optimized annotation limits (50 per element) to prevent performance degradation
    - Added size limits on text processing (skip blocks >10,000 chars in annotator, >1,000 in documentation)
- **Build System**:
    - Added Trove4j dependency for high-performance primitive collections
    - Enabled Kotlin incremental compilation for faster builds
    - Added compiler optimization flags (`-Xjvm-default=all`)

### Technical Details

This release eliminates thread contention that previously caused:

- 6+ background threads stuck in `HashMap$TreeNode.getTreeNode()` and `HashMap$TreeNode.putTreeVal()`
- UI thread unable to acquire write locks, freezing the IDE during typing
- Unbalanced HashMap tree structures causing O(log n) operations instead of O(1)

All hot paths now use lock-free concurrent collections and have proper cancellation support.

## [1.3.0] - 11/02/2025

> This update was big enough that I decided to skip a version. 🤣🤣

### I've been no lifing this whole plugin so far...

#### Someone help me...

##### Send help...

###### Send Melatonin... 💀💀

![img_1.png](img_1.png)

### Added

- **Comprehensive Database Coverage**: Now includes ALL SPT database items (4,195+ items) including Node types and items
  without locale entries
- **Category Support**: Added support for 87 handbook categories from the SPT database
- **Enhanced Build System**: Items from items.json are now included even if they don't have locale translations
- **Fallback Names**: Items without locale entries now use their `_name` field from items.json as fallback
- **Customizable Highlighting Styles**: Configure bold, italic, underline, and background highlighting
- **Type-Specific Colors**: Different colors for Items, Ammo, Weapons, Quests, Traders, Locations, and Customization
  items
- **Popup Theme Customization**: Customize popup colors and delays
- **Theme Preset System**: Save, load, and share theme configurations (.spttheme files)
- **Built-in Theme Presets**: Quick access to Light, Dark, Blue, Red, and Purple themes
- **Custom ID Filenames**: Configure which files to search for custom IDs (.sptids, sptids.json)
- **Tabbed Settings UI**: Organized settings into General, Highlighting, and Popup tabs

### Changed

- **Database Build Process**: Updated BuildDataTask to include all items from items.json, not just those with locale
  entries
- **Node Type Handling**: Special handling for items with special types (e.g., `_type: "Node"` for the RadioTransmitter)
- **Category Loading**: Categories are now loaded from handbook.json and included in all language databases
- **Settings Interface**: Complete redesign with improved organization and usability
- **Highlight Refresh**: Added automatic cache invalidation when highlighting settings change
- **Theme Management**: Import/export functionality for sharing themes with team members
- **Build Logging**: Replaced println statements with Gradle's proper logging mechanism (lifecycle, info, warn, error
  levels)
- **Task Validation**: Added proper validation and error handling for signPlugin and publishPlugin tasks with clear
  error messages
- **Plugin Description**: Simplified plugin.xml description for better maintainability; full documentation available in
  README
- **ID Matching**: Uses a flexible regex pattern with database validation to prevent false positives while maintaining
  accuracy
- **Code Quality**: Centralized constants (DEFAULT_POPUP_DELAY_MS, DEFAULT_FALLBACK_LANGUAGE), added @JvmField
  annotations, refactored nested conditionals
- **Custom ID Loading**: Improved robustness with a language fallback mechanism (current language → English → any
  available language)
- **Build Task Logging**: Replaced all `println` statements in `BuildDataTask` with proper `java.util.logging.Logger`
  for better log level management (info, warning, fine)
- **Code Quality Improvements**:
    - Replaced hardcoded strings with constants in `SptDataService` (DATABASE_PATH_PREFIX, TRANSLATIONS_PATH_PREFIX,
      JSON_EXTENSION, DEFAULT_LANGUAGE)
    - Improved code maintainability and reduced risk of typos
- **Popup Theme Presets**: Updated default colors with proper light/dark mode adaptation
    - Blue theme: For those who like blue things
    - Red theme: For those who like red things
    - Purple/WTT theme: Inspired by the WTT team <3

### Fixed

- Missing items like RadioTransmitter (62e9103049c018f425059f38) and other types now properly recognized
- All 4,195 items from the SPT database are now available for highlighting and documentation
- Categories are properly loaded and displayed with appropriate metadata
- Configuration cache compatibility in Gradle build tasks
- Highlight changes now apply immediately without requiring IDE restart
- Custom IDs now work across all languages via a fallback mechanism (no longer silently skip IDs without current
  language data)
- Quest trader name resolution refactored to eliminate duplicate fallback logic
- Unused ItemDetails fields removed (detailLink, parent, parentID, parentDetailLink)
- Environment variable handling in Gradle tasks now prevents runtime errors with helpful messages

### Removed

- **Unused ItemDetails Fields**: Removed detailLink, parent, parentID, and parentDetailLink properties that were never
  populated during the build process
    - I plan to add detailLink and parentID back in a future release, but I'm not sure how to handle the links yet.

## [1.1.0] - 10/31/2025

### Added

- **MongoDB ID Generator**: Generate valid MongoDB ObjectIds with Ctrl+Shift+Alt+W
- **Custom ID Support**: Add your own IDs via .sptids files with live auto-reload
- **Documentation Popups**: Hover over IDs to see detailed item information
- **Multilingual Support**: 17 languages including English, Russian, Chinese, and more

### Changed

- Improved ID detection algorithm for better accuracy
- Enhanced popup formatting and styling

### Fixed

- Various performance improvements
- Memory leak in file watcher service

## [1.0.0] - 10/31/2025

### Added

- Initial release
- Basic SPT ID highlighting in JSON, TypeScript, JavaScript, and C# files
- Support for 4,000+ items from the SPT database
- Quest, Trader, and Location ID recognition
- Configurable language selection for item names
- Basic settings interface

---

## Legend

- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security vulnerability fixes
