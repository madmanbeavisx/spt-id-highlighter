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

    companion object {
        fun getInstance(): SptIdSettingsState {
            return ApplicationManager.getApplication().getService(SptIdSettingsState::class.java)
        }

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
