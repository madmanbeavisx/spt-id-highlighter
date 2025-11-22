package com.madmanbeavis.sptidHighlighter.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.refringe.sptidHighlighter.settings.SptIdSettingsState",
    storages = [Storage("SptIdHighlighterSettings.xml")]
)
class SptIdSettingsState : PersistentStateComponent<SptIdSettingsState> {
    var language: String = "en"
    var customIdFilenames: MutableList<String> = mutableListOf(".sptids", "sptids.json")

    // Highlight style settings
    var highlightBold: Boolean = false
    var highlightItalic: Boolean = true
    var highlightUnderline: Boolean = true
    var highlightBackground: Boolean = false

    // Highlight colors (stored as hex strings, format: "lightColor|darkColor" or single color, null = use default)
    var colorItem: String? = null
    var colorQuest: String? = null
    var colorTrader: String? = null
    var colorLocation: String? = null
    var colorAmmo: String? = null
    var colorWeapon: String? = null
    var colorCustomization: String? = null

    // Documentation popup delay in milliseconds (DEPRECATED - kept for backwards compatibility)
    var popupDelayMs: Int = DEFAULT_POPUP_DELAY_MS

    // Popup theme colors (DEPRECATED - popups replaced with tooltips, kept for backwards compatibility)
    // var popupBackgroundColor: String? = null
    // var popupForegroundColor: String? = null
    // var popupBorderColor: String? = null

    companion object {
        const val DEFAULT_POPUP_DELAY_MS = 300
        const val DEFAULT_FALLBACK_LANGUAGE = "en"

        fun getInstance(): SptIdSettingsState {
            return ApplicationManager.getApplication().getService(SptIdSettingsState::class.java)
        }

        @JvmField
        val SUPPORTED_LANGUAGES = listOf(
            "ch" to "Simplified Chinese",
            "cz" to "Czech",
            "en" to "English",
            "es-mx" to "Mexican Spanish",
            "es" to "Spanish",
            "fr" to "French",
            "ge" to "German",
            "hu" to "Hungarian",
            "it" to "Italian",
            "jp" to "Japanese",
            "kr" to "Korean",
            "pl" to "Polish",
            "po" to "Portuguese",
            "ro" to "Romanian",
            "ru" to "Russian",
            "sk" to "Slovak",
            "tu" to "Turkish"
        )
    }

    override fun getState(): SptIdSettingsState {
        return this
    }

    override fun loadState(state: SptIdSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
