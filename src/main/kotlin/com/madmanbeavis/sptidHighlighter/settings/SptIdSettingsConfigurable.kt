package com.madmanbeavis.sptidHighlighter.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiManager
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.madmanbeavis.sptidHighlighter.models.ThemePreset
import com.madmanbeavis.sptidHighlighter.services.SptDataService
import com.madmanbeavis.sptidHighlighter.utils.ColorUtils
import java.io.File
import javax.swing.*

class SptIdSettingsConfigurable : Configurable {
    private var settingsComponent: SptIdSettingsComponent? = null

    override fun getDisplayName(): String {
        return "SPT ID Highlighter"
    }

    override fun createComponent(): JComponent {
        settingsComponent = SptIdSettingsComponent()
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = SptIdSettingsState.getInstance()
        val component = settingsComponent ?: return false

        return component.getSelectedLanguage() != settings.language ||
                component.getCustomIdFilenames() != settings.customIdFilenames ||
                component.isHighlightStyleModified(settings) ||
                component.isHighlightColorsModified(settings)
        // || component.getPopupDelay() != settings.popupDelayMs ||
        // component.isPopupThemeModified(settings)
    }

    override fun apply() {
        val settings = SptIdSettingsState.getInstance()
        val component = settingsComponent ?: return

        val newLanguage = component.getSelectedLanguage()
        val newFilenames = component.getCustomIdFilenames()

        var needsReload = false
        var highlightingChanged = false

        if (settings.language != newLanguage) {
            settings.language = newLanguage
            needsReload = true
        }

        if (settings.customIdFilenames != newFilenames) {
            settings.customIdFilenames = newFilenames.toMutableList()
            needsReload = true
        }

        // Check if highlighting settings changed
        if (component.isHighlightStyleModified(settings) || component.isHighlightColorsModified(settings)) {
            highlightingChanged = true
        }

        // Apply highlight style settings
        component.applyHighlightStyle(settings)

        // Apply highlight colors
        component.applyHighlightColors(settings)

        // Apply popup settings (DEPRECATED - kept for backwards compatibility)
        // settings.popupDelayMs = component.getPopupDelay()
        // component.applyPopupTheme(settings)

        if (needsReload) {
            // Reload data with new settings
            SptDataService.getInstance().loadData()
        }

        if (highlightingChanged) {
            // Trigger re-highlighting of all open files
            rehighlightAllFiles()
        }
    }

    private fun rehighlightAllFiles() {
        ApplicationManager.getApplication().invokeLater {
            ProjectManager.getInstance().openProjects.forEach { project ->
                ApplicationManager.getApplication().runReadAction {
                    PsiManager.getInstance(project).dropPsiCaches()
                }
            }
        }
    }

    override fun reset() {
        val settings = SptIdSettingsState.getInstance()
        settingsComponent?.apply {
            setSelectedLanguage(settings.language)
            setCustomIdFilenames(settings.customIdFilenames)
            resetHighlightStyle(settings)
            resetHighlightColors(settings)
            // setPopupDelay(settings.popupDelayMs)
            // resetPopupTheme(settings)
        }
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }

    private class SptIdSettingsComponent {
        val panel: JPanel
        private val languageComboBox: ComboBox<String>
        private val filenamesTextField: JBTextField

        // Highlight style checkboxes
        private val boldCheckbox: JBCheckBox
        private val italicCheckbox: JBCheckBox
        private val underlineCheckbox: JBCheckBox
        private val backgroundCheckbox: JBCheckBox

        // Color pickers for different ID types
        private val colorItemPanel: ColorPanel
        private val colorQuestPanel: ColorPanel
        private val colorTraderPanel: ColorPanel
        private val colorLocationPanel: ColorPanel
        private val colorAmmoPanel: ColorPanel
        private val colorWeaponPanel: ColorPanel
        private val colorCustomizationPanel: ColorPanel

        // Popup settings (DEPRECATED - kept for backwards compatibility)
        // private val popupDelayField: JBTextField

        // Popup theme colors (DEPRECATED - kept for backwards compatibility)
        // private val popupBackgroundPanel: ColorPanel
        // private val popupForegroundPanel: ColorPanel
        // private val popupBorderPanel: ColorPanel

        init {
            val languageOptions = SptIdSettingsState.SUPPORTED_LANGUAGES.map { it.second }.toTypedArray()
            languageComboBox = ComboBox(languageOptions)

            filenamesTextField = JBTextField()
            filenamesTextField.toolTipText = "Comma-separated list of filenames to search for"

            // Initialize highlight style checkboxes
            boldCheckbox = JBCheckBox("Bold")
            italicCheckbox = JBCheckBox("Italic")
            underlineCheckbox = JBCheckBox("Underline")
            backgroundCheckbox = JBCheckBox("Background Highlight")

            // Initialize color pickers (null = use default)
            colorItemPanel = ColorPanel()
            colorQuestPanel = ColorPanel()
            colorTraderPanel = ColorPanel()
            colorLocationPanel = ColorPanel()
            colorAmmoPanel = ColorPanel()
            colorWeaponPanel = ColorPanel()
            colorCustomizationPanel = ColorPanel()

            // Initialize popup settings (DEPRECATED - kept for backwards compatibility)
            // popupDelayField = JBTextField()
            // popupDelayField.toolTipText = "Delay in milliseconds before popup appears"

            // Initialize popup theme colors (DEPRECATED - kept for backwards compatibility)
            // popupBackgroundPanel = ColorPanel()
            // popupForegroundPanel = ColorPanel()
            // popupBorderPanel = ColorPanel()

            // Create tabbed pane
            val tabbedPane = JTabbedPane()

            // General tab
            val generalPanel = createGeneralPanel()
            tabbedPane.addTab("General", generalPanel)

            // Highlighting tab
            val highlightingPanel = createHighlightingPanel()
            tabbedPane.addTab("Highlighting", highlightingPanel)

            // Popup tab (DEPRECATED - commented out as popups replaced with tooltips)
            // val popupPanel = createPopupPanel()
            // tabbedPane.addTab("Popup", popupPanel)

            panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.add(tabbedPane)
        }

        private fun createGeneralPanel(): JPanel {
            val filenamesHelpLabel = JBLabel("<html><i>Comma-separated list (e.g., .sptids, sptids.json)</i></html>")
            filenamesHelpLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND

            val keybindInfoLabel =
                JBLabel("<html><b>Tip:</b> To change MongoDB ID Generator keybind: <i>Settings → Keymap → 'Generate MongoDB ObjectId'</i></html>")
            keybindInfoLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND

            // Theme preset management
            val themeManagementPanel = createThemeManagementPanel()

            return FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Language:"), languageComboBox, 1, false)
                .addVerticalGap(10)
                .addLabeledComponent(JBLabel("Custom ID filenames:"), filenamesTextField, 1, false)
                .addComponent(filenamesHelpLabel)
                .addVerticalGap(15)
                .addComponent(keybindInfoLabel)
                .addVerticalGap(20)
                .addSeparator()
                .addVerticalGap(10)
                .addComponent(themeManagementPanel)
                .addComponentFillVertically(JPanel(), 0)
                .panel
        }

        private fun createThemeManagementPanel(): JPanel {
            val panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.border = BorderFactory.createTitledBorder("Theme Preset Management")

            val buttonPanel = JPanel()
            buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)

            val saveThemeButton = JButton("Save Current Theme...")
            saveThemeButton.toolTipText = "Save current highlighting and popup settings as a theme preset"
            saveThemeButton.addActionListener {
                saveCurrentTheme()
            }

            val loadThemeButton = JButton("Load Theme...")
            loadThemeButton.toolTipText = "Load a theme preset from a file"
            loadThemeButton.addActionListener {
                loadTheme()
            }

            val exportThemeButton = JButton("Export Theme...")
            exportThemeButton.toolTipText = "Export current theme to share with others"
            exportThemeButton.addActionListener {
                exportTheme()
            }

            buttonPanel.add(saveThemeButton)
            buttonPanel.add(Box.createHorizontalStrut(5))
            buttonPanel.add(loadThemeButton)
            buttonPanel.add(Box.createHorizontalStrut(5))
            buttonPanel.add(exportThemeButton)
            buttonPanel.add(Box.createHorizontalGlue())

            val helpLabel = JBLabel("<html><i>Save and share your custom theme presets with others</i></html>")
            helpLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            helpLabel.alignmentX = JComponent.LEFT_ALIGNMENT

            panel.add(helpLabel)
            panel.add(Box.createVerticalStrut(5))
            panel.add(buttonPanel)

            return panel
        }

        private fun saveCurrentTheme() {
            val themeName = Messages.showInputDialog(
                "Enter a name for this theme preset:",
                "Save Theme Preset",
                Messages.getQuestionIcon()
            ) ?: return

            if (themeName.isBlank()) {
                Messages.showErrorDialog("Theme name cannot be empty", "Invalid Name")
                return
            }

            val description = Messages.showInputDialog(
                "Enter a description (optional):",
                "Theme Description",
                Messages.getQuestionIcon()
            ) ?: ""

            val author = Messages.showInputDialog(
                "Enter author name (optional):",
                "Theme Author",
                Messages.getQuestionIcon()
            ) ?: ""

            // Create theme from current UI state
            SptIdSettingsState.getInstance()

            // Temporarily apply current UI state to settings for export
            val tempSettings = SptIdSettingsState()
            applyHighlightStyle(tempSettings)
            applyHighlightColors(tempSettings)
            // tempSettings.popupDelayMs = getPopupDelay()
            // applyPopupTheme(tempSettings)

            val theme = ThemePreset.fromSettings(tempSettings, themeName, description, author)

            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Save Theme Preset"
            fileChooser.selectedFile = File("${themeName.replace(" ", "_")}.spttheme")

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    val file = fileChooser.selectedFile
                    if (!file.name.endsWith(".spttheme")) {
                        File("${file.absolutePath}.spttheme").writeText(theme.toJson())
                    } else {
                        file.writeText(theme.toJson())
                    }
                    Messages.showInfoMessage(
                        "Theme preset saved successfully!",
                        "Success"
                    )
                } catch (e: Exception) {
                    Messages.showErrorDialog(
                        "Failed to save theme: ${e.message}",
                        "Save Error"
                    )
                }
            }
        }

        private fun loadTheme() {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Load Theme Preset"
            fileChooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
                "SPT Theme Files (*.spttheme)", "spttheme"
            )

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    val theme = ThemePreset.importFromFile(fileChooser.selectedFile)
                    if (theme != null) {
                        applyThemeToUI(theme)
                        Messages.showInfoMessage(
                            "Theme '${theme.name}' loaded successfully!\n${if (theme.description.isNotBlank()) "Description: ${theme.description}\n" else ""}${if (theme.author.isNotBlank()) "Author: ${theme.author}" else ""}",
                            "Theme Loaded"
                        )
                    } else {
                        Messages.showErrorDialog(
                            "Invalid theme file format",
                            "Load Error"
                        )
                    }
                } catch (e: Exception) {
                    Messages.showErrorDialog(
                        "Failed to load theme: ${e.message}",
                        "Load Error"
                    )
                }
            }
        }

        private fun exportTheme() {
            // Same as save, but with a message about sharing
            saveCurrentTheme()
        }

        private fun applyThemeToUI(theme: ThemePreset) {
            // Apply highlight style
            boldCheckbox.isSelected = theme.highlightBold
            italicCheckbox.isSelected = theme.highlightItalic
            underlineCheckbox.isSelected = theme.highlightUnderline
            backgroundCheckbox.isSelected = theme.highlightBackground

            // Apply highlight colors
            colorItemPanel.selectedColor = theme.colorItem?.let { ColorUtils.stringToColor(it) }
            colorQuestPanel.selectedColor = theme.colorQuest?.let { ColorUtils.stringToColor(it) }
            colorTraderPanel.selectedColor = theme.colorTrader?.let { ColorUtils.stringToColor(it) }
            colorLocationPanel.selectedColor = theme.colorLocation?.let { ColorUtils.stringToColor(it) }
            colorAmmoPanel.selectedColor = theme.colorAmmo?.let { ColorUtils.stringToColor(it) }
            colorWeaponPanel.selectedColor = theme.colorWeapon?.let { ColorUtils.stringToColor(it) }
            colorCustomizationPanel.selectedColor = theme.colorCustomization?.let { ColorUtils.stringToColor(it) }

            // Apply popup settings (DEPRECATED - kept for backwards compatibility)
            // popupDelayField.text = theme.popupDelayMs.toString()
            // popupForegroundPanel.selectedColor = theme.popupForegroundColor?.let { ColorUtils.stringToColor(it) }
            // popupBorderPanel.selectedColor = theme.popupBorderColor?.let { ColorUtils.stringToColor(it) }
        }

        private fun createHighlightingPanel(): JPanel {
            val stylePanel = JPanel()
            stylePanel.layout = BoxLayout(stylePanel, BoxLayout.Y_AXIS)
            stylePanel.border = BorderFactory.createTitledBorder("Text Style")
            stylePanel.add(boldCheckbox)
            stylePanel.add(italicCheckbox)
            stylePanel.add(underlineCheckbox)
            stylePanel.add(backgroundCheckbox)

            val resetColorsButton = JButton("Reset All Colors")
            resetColorsButton.addActionListener {
                colorItemPanel.selectedColor = null
                colorQuestPanel.selectedColor = null
                colorTraderPanel.selectedColor = null
                colorLocationPanel.selectedColor = null
                colorAmmoPanel.selectedColor = null
                colorWeaponPanel.selectedColor = null
                colorCustomizationPanel.selectedColor = null
            }

            val colorHelpLabel = JBLabel("<html><i>Leave unset to use IDE theme defaults</i></html>")
            colorHelpLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND

            return FormBuilder.createFormBuilder()
                .addComponent(stylePanel)
                .addVerticalGap(10)
                .addSeparator()
                .addVerticalGap(10)
                .addComponent(JBLabel("<html><b>Colors by ID Type:</b></html>"))
                .addComponent(colorHelpLabel)
                .addVerticalGap(5)
                .addLabeledComponent(JBLabel("Items:"), colorItemPanel, 1, false)
                .addLabeledComponent(JBLabel("Quests:"), colorQuestPanel, 1, false)
                .addLabeledComponent(JBLabel("Traders:"), colorTraderPanel, 1, false)
                .addLabeledComponent(JBLabel("Locations:"), colorLocationPanel, 1, false)
                .addLabeledComponent(JBLabel("Ammo:"), colorAmmoPanel, 1, false)
                .addLabeledComponent(JBLabel("Weapons:"), colorWeaponPanel, 1, false)
                .addLabeledComponent(JBLabel("Customization:"), colorCustomizationPanel, 1, false)
                .addVerticalGap(10)
                .addComponent(resetColorsButton)
                .addComponentFillVertically(JPanel(), 0)
                .panel
        }

        // DEPRECATED - Popup panel removed as popups replaced with tooltips
        // private fun createPopupPanel(): JPanel {
        //     val delayHelpLabel =
        //         JBLabel("<html><i>Time in milliseconds before documentation appears on hover (default: ${SptIdSettingsState.DEFAULT_POPUP_DELAY_MS}ms)</i></html>")
        //     delayHelpLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        //
        //     val themeHelpLabel = JBLabel("<html><i>Choose a preset or customize individual colors</i></html>")
        //     themeHelpLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        //
        //     // Theme preset buttons
        //     val themeButtonPanel = JPanel()
        //     themeButtonPanel.layout = BoxLayout(themeButtonPanel, BoxLayout.X_AXIS)
        //
        //     val defaultThemeButton = JButton("Default (IDE)")
        //     defaultThemeButton.toolTipText = "Use IDE's default theme colors"
        //     defaultThemeButton.addActionListener {
        //         popupForegroundPanel.selectedColor = null
        //         popupBorderPanel.selectedColor = null
        //     }
        //
        //     val lightThemeButton = JButton("Light Theme")
        //     lightThemeButton.toolTipText = "Optimized for light IDE themes"
        //     lightThemeButton.addActionListener {
        //         popupForegroundPanel.selectedColor = Gray._40
        //         popupBorderPanel.selectedColor = Gray._200
        //     }
        //
        //     val darkThemeButton = JButton("Dark Theme")
        //     darkThemeButton.toolTipText = "Optimized for dark IDE themes"
        //     darkThemeButton.addActionListener {
        //         popupForegroundPanel.selectedColor = Gray._220
        //         popupBorderPanel.selectedColor = Gray._80
        //     }
        //
        //     val blueThemeButton = JButton("Blue")
        //     blueThemeButton.toolTipText = "Blue-tinted theme (adapts to light/dark mode)"
        //     blueThemeButton.addActionListener {
        //         // Darker blue for light theme, brighter blue for dark theme
        //         popupForegroundPanel.selectedColor = JBColor(Color(60, 120, 200), Color(156, 196, 255))
        //         popupBorderPanel.selectedColor = JBColor(Color(100, 150, 220), Color(156, 196, 255))
        //     }
        //
        //     val redThemeButton = JButton("Red")
        //     redThemeButton.toolTipText = "Red-tinted theme (adapts to light/dark mode)"
        //     redThemeButton.addActionListener {
        //         // Darker red for light theme, brighter red for dark theme
        //         popupForegroundPanel.selectedColor = JBColor(Color(180, 30, 40), Color(255, 100, 100))
        //         popupBorderPanel.selectedColor = null
        //     }
        //
        //     val purpleThemeButton = JButton("WTT")
        //     purpleThemeButton.toolTipText = "Purple-tinted theme inspired by WTT (adapts to light/dark mode)"
        //     purpleThemeButton.addActionListener {
        //         // Darker purple for light theme, brighter purple for dark theme
        //         popupForegroundPanel.selectedColor = JBColor(Color(100, 40, 140), Color(180, 120, 220))
        //         popupBorderPanel.selectedColor = null
        //     }
        //
        //     themeButtonPanel.add(defaultThemeButton)
        //     themeButtonPanel.add(Box.createHorizontalStrut(5))
        //     themeButtonPanel.add(lightThemeButton)
        //     themeButtonPanel.add(Box.createHorizontalStrut(5))
        //     themeButtonPanel.add(darkThemeButton)
        //     themeButtonPanel.add(Box.createHorizontalStrut(5))
        //     themeButtonPanel.add(blueThemeButton)
        //     themeButtonPanel.add(Box.createHorizontalStrut(5))
        //     themeButtonPanel.add(redThemeButton)
        //     themeButtonPanel.add(Box.createHorizontalStrut(5))
        //     themeButtonPanel.add(purpleThemeButton)
        //     themeButtonPanel.add(Box.createHorizontalGlue())
        //
        //     return FormBuilder.createFormBuilder()
        //         .addLabeledComponent(JBLabel("Popup delay (ms):"), popupDelayField, 1, false)
        //         .addComponent(delayHelpLabel)
        //         .addVerticalGap(15)
        //         .addSeparator()
        //         .addVerticalGap(10)
        //         .addComponent(JBLabel("<html><b>Popup Theme Presets:</b></html>"))
        //         .addComponent(themeButtonPanel)
        //         .addVerticalGap(15)
        //         .addSeparator()
        //         .addVerticalGap(10)
        //         .addComponent(JBLabel("<html><b>Custom Colors:</b></html>"))
        //         .addComponent(themeHelpLabel)
        //         .addVerticalGap(5)
        //         .addLabeledComponent(JBLabel("Text:"), popupForegroundPanel, 1, false)
        //         .addLabeledComponent(JBLabel("Border:"), popupBorderPanel, 1, false)
        //         .addComponentFillVertically(JPanel(), 0)
        //         .panel
        // }

        fun getSelectedLanguage(): String {
            val selectedIndex = languageComboBox.selectedIndex
            return if (selectedIndex >= 0) {
                SptIdSettingsState.SUPPORTED_LANGUAGES[selectedIndex].first
            } else {
                "en"
            }
        }

        fun setSelectedLanguage(languageCode: String) {
            val index = SptIdSettingsState.SUPPORTED_LANGUAGES.indexOfFirst { it.first == languageCode }
            if (index >= 0) {
                languageComboBox.selectedIndex = index
            }
        }

        fun getCustomIdFilenames(): List<String> {
            return filenamesTextField.text
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }

        fun setCustomIdFilenames(filenames: List<String>) {
            filenamesTextField.text = filenames.joinToString(", ")
        }

        // Highlight style methods
        fun isHighlightStyleModified(settings: SptIdSettingsState): Boolean {
            return boldCheckbox.isSelected != settings.highlightBold ||
                    italicCheckbox.isSelected != settings.highlightItalic ||
                    underlineCheckbox.isSelected != settings.highlightUnderline ||
                    backgroundCheckbox.isSelected != settings.highlightBackground
        }

        fun applyHighlightStyle(settings: SptIdSettingsState) {
            settings.highlightBold = boldCheckbox.isSelected
            settings.highlightItalic = italicCheckbox.isSelected
            settings.highlightUnderline = underlineCheckbox.isSelected
            settings.highlightBackground = backgroundCheckbox.isSelected
        }

        fun resetHighlightStyle(settings: SptIdSettingsState) {
            boldCheckbox.isSelected = settings.highlightBold
            italicCheckbox.isSelected = settings.highlightItalic
            underlineCheckbox.isSelected = settings.highlightUnderline
            backgroundCheckbox.isSelected = settings.highlightBackground
        }

        // Highlight color methods
        fun isHighlightColorsModified(settings: SptIdSettingsState): Boolean {
            return ColorUtils.colorToString(colorItemPanel.selectedColor) != settings.colorItem ||
                    ColorUtils.colorToString(colorQuestPanel.selectedColor) != settings.colorQuest ||
                    ColorUtils.colorToString(colorTraderPanel.selectedColor) != settings.colorTrader ||
                    ColorUtils.colorToString(colorLocationPanel.selectedColor) != settings.colorLocation ||
                    ColorUtils.colorToString(colorAmmoPanel.selectedColor) != settings.colorAmmo ||
                    ColorUtils.colorToString(colorWeaponPanel.selectedColor) != settings.colorWeapon ||
                    ColorUtils.colorToString(colorCustomizationPanel.selectedColor) != settings.colorCustomization
        }

        fun applyHighlightColors(settings: SptIdSettingsState) {
            settings.colorItem = ColorUtils.colorToString(colorItemPanel.selectedColor)
            settings.colorQuest = ColorUtils.colorToString(colorQuestPanel.selectedColor)
            settings.colorTrader = ColorUtils.colorToString(colorTraderPanel.selectedColor)
            settings.colorLocation = ColorUtils.colorToString(colorLocationPanel.selectedColor)
            settings.colorAmmo = ColorUtils.colorToString(colorAmmoPanel.selectedColor)
            settings.colorWeapon = ColorUtils.colorToString(colorWeaponPanel.selectedColor)
            settings.colorCustomization = ColorUtils.colorToString(colorCustomizationPanel.selectedColor)
        }

        fun resetHighlightColors(settings: SptIdSettingsState) {
            colorItemPanel.selectedColor = ColorUtils.stringToColor(settings.colorItem)
            colorQuestPanel.selectedColor = ColorUtils.stringToColor(settings.colorQuest)
            colorTraderPanel.selectedColor = ColorUtils.stringToColor(settings.colorTrader)
            colorLocationPanel.selectedColor = ColorUtils.stringToColor(settings.colorLocation)
            colorAmmoPanel.selectedColor = ColorUtils.stringToColor(settings.colorAmmo)
            colorWeaponPanel.selectedColor = ColorUtils.stringToColor(settings.colorWeapon)
            colorCustomizationPanel.selectedColor = ColorUtils.stringToColor(settings.colorCustomization)
        }

        // Popup methods (DEPRECATED - kept for backwards compatibility)
        // fun getPopupDelay(): Int {
        //     return popupDelayField.text.toIntOrNull() ?: SptIdSettingsState.DEFAULT_POPUP_DELAY_MS
        // }

        // fun setPopupDelay(delay: Int) {
        //     popupDelayField.text = delay.toString()
        // }

        // fun isPopupThemeModified(settings: SptIdSettingsState): Boolean {
        //     return ColorUtils.colorToString(popupForegroundPanel.selectedColor) != settings.popupForegroundColor ||
        //             ColorUtils.colorToString(popupBorderPanel.selectedColor) != settings.popupBorderColor
        // }

        // fun applyPopupTheme(settings: SptIdSettingsState) {
        //     settings.popupForegroundColor = ColorUtils.colorToString(popupForegroundPanel.selectedColor)
        //     settings.popupBorderColor = ColorUtils.colorToString(popupBorderPanel.selectedColor)
        // }

        // fun resetPopupTheme(settings: SptIdSettingsState) {
        //     popupForegroundPanel.selectedColor = ColorUtils.stringToColor(settings.popupForegroundColor)
        //     popupBorderPanel.selectedColor = ColorUtils.stringToColor(settings.popupBorderColor)
        // }

    }
}
