package com.madmanbeavis.sptidHighlighter.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.madmanbeavis.sptidHighlighter.services.SptDataService
import javax.swing.JComponent
import javax.swing.JPanel

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
        return settingsComponent?.getSelectedLanguage() != settings.language
    }

    override fun apply() {
        val settings = SptIdSettingsState.getInstance()
        val newLanguage = settingsComponent?.getSelectedLanguage() ?: "en"

        if (settings.language != newLanguage) {
            settings.language = newLanguage
            // Reload data with new language
            SptDataService.getInstance().loadData()
        }
    }

    override fun reset() {
        val settings = SptIdSettingsState.getInstance()
        settingsComponent?.setSelectedLanguage(settings.language)
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }

    private class SptIdSettingsComponent {
        val panel: JPanel
        private val languageComboBox: ComboBox<String>

        init {
            val languageOptions = SptIdSettingsState.SUPPORTED_LANGUAGES.map { it.second }.toTypedArray()
            languageComboBox = ComboBox(languageOptions)

            panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Language for item names and descriptions:"), languageComboBox, 1, false)
                .addComponentFillVertically(JPanel(), 0)
                .panel
        }

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
    }
}
