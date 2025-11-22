package com.madmanbeavis.sptidHighlighter.models

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

/**
 * Represents a complete theme preset for the SPT ID Highlighter plugin.
 * Can be exported to JSON and imported from JSON for sharing.
 */
data class ThemePreset(
    val name: String,
    val description: String = "",
    val author: String = "",

    // Highlight style settings
    val highlightBold: Boolean = false,
    val highlightItalic: Boolean = true,
    val highlightUnderline: Boolean = true,
    val highlightBackground: Boolean = false,

    // Highlight colors by type
    val colorItem: String? = null,
    val colorQuest: String? = null,
    val colorTrader: String? = null,
    val colorLocation: String? = null,
    val colorAmmo: String? = null,
    val colorWeapon: String? = null,
    val colorCustomization: String? = null,

    // Popup settings
    val popupDelayMs: Int = 300,
    val popupBackgroundColor: String? = null,
    val popupForegroundColor: String? = null,
    val popupBorderColor: String? = null
) {
    companion object {
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        /**
         * Export this theme preset to a JSON file
         */
        fun ThemePreset.exportToFile(file: File) {
            file.writeText(gson.toJson(this))
        }

        /**
         * Import a theme preset from a JSON file
         */
        fun importFromFile(file: File): ThemePreset? {
            return try {
                gson.fromJson(file.readText(), ThemePreset::class.java)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Import a theme preset from a JSON string
         */
        fun importFromJson(json: String): ThemePreset? {
            return try {
                gson.fromJson(json, ThemePreset::class.java)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Create a theme preset from current settings
         */
        fun fromSettings(
            settings: com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState,
            name: String,
            description: String = "",
            author: String = ""
        ): ThemePreset {
            return ThemePreset(
                name = name,
                description = description,
                author = author,
                highlightBold = settings.highlightBold,
                highlightItalic = settings.highlightItalic,
                highlightUnderline = settings.highlightUnderline,
                highlightBackground = settings.highlightBackground,
                colorItem = settings.colorItem,
                colorQuest = settings.colorQuest,
                colorTrader = settings.colorTrader,
                colorLocation = settings.colorLocation,
                colorAmmo = settings.colorAmmo,
                colorWeapon = settings.colorWeapon,
                colorCustomization = settings.colorCustomization,
                popupDelayMs = settings.popupDelayMs,
                popupBackgroundColor = null, // DEPRECATED
                popupForegroundColor = null, // DEPRECATED
                popupBorderColor = null // DEPRECATED
            )
        }
    }

    /**
     * Apply this theme preset to settings
     */
    fun applyToSettings(settings: com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState) {
        settings.highlightBold = highlightBold
        settings.highlightItalic = highlightItalic
        settings.highlightUnderline = highlightUnderline
        settings.highlightBackground = highlightBackground
        settings.colorItem = colorItem
        settings.colorQuest = colorQuest
        settings.colorTrader = colorTrader
        settings.colorLocation = colorLocation
        settings.colorAmmo = colorAmmo
        settings.colorWeapon = colorWeapon
        settings.colorCustomization = colorCustomization
        settings.popupDelayMs = popupDelayMs
        // DEPRECATED - popup colors no longer used
        // settings.popupBackgroundColor = popupBackgroundColor
        // settings.popupForegroundColor = popupForegroundColor
        // settings.popupBorderColor = popupBorderColor
    }

    /**
     * Export this theme preset to a JSON string
     */
    fun toJson(): String {
        return gson.toJson(this)
    }
}
