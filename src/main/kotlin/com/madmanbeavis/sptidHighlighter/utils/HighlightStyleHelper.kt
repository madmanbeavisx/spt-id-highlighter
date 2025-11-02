package com.madmanbeavis.sptidHighlighter.utils

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import com.madmanbeavis.sptidHighlighter.models.ItemDetailType
import com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState
import java.awt.Color
import java.awt.Font

object HighlightStyleHelper {

    fun getTextAttributesForType(itemType: ItemDetailType?): TextAttributes {
        val settings = SptIdSettingsState.getInstance()
        val attributes = TextAttributes()

        // Apply text style
        var fontType = Font.PLAIN
        if (settings.highlightBold) {
            fontType = fontType or Font.BOLD
        }
        if (settings.highlightItalic) {
            fontType = fontType or Font.ITALIC
        }
        attributes.fontType = fontType

        // Apply color based on type (use default cyan if no custom color)
        val color = getColorForType(itemType, settings) ?: getDefaultHighlightColor()
        attributes.foregroundColor = color

        // Apply background if enabled
        if (settings.highlightBackground) {
            attributes.backgroundColor = getBackgroundColor()
        }

        // Apply effects (underline, etc.)
        if (settings.highlightUnderline) {
            attributes.effectType = com.intellij.openapi.editor.markup.EffectType.LINE_UNDERSCORE
            attributes.effectColor = color
        }

        return attributes
    }

    private fun getColorForType(itemType: ItemDetailType?, settings: SptIdSettingsState): Color? {
        val colorString = when (itemType) {
            ItemDetailType.QUEST -> settings.colorQuest
            ItemDetailType.TRADER -> settings.colorTrader
            ItemDetailType.LOCATION -> settings.colorLocation
            ItemDetailType.AMMO -> settings.colorAmmo
            ItemDetailType.WEAPON -> settings.colorWeapon
            ItemDetailType.CUSTOMIZATION -> settings.colorCustomization
            else -> settings.colorItem // Default for all other item types
        }

        return colorString?.let { ColorUtils.stringToJBColor(it) }
    }

    private fun getDefaultHighlightColor(): Color {
        // Default cyan color that works well in both light and dark themes
        return JBColor(
            Color(0, 128, 192),  // Light theme: darker cyan
            Color(100, 200, 255)  // Dark theme: brighter cyan
        )
    }

    private fun getBackgroundColor(): Color {
        // Use a semi-transparent background that works with both light and dark themes
        return JBColor(
            Color(200, 200, 255, 30), // Light theme: light blue tint
            Color(100, 100, 150, 30)  // Dark theme: darker blue tint
        )
    }

    /**
     * Creates a TextAttributesKey for a specific item type.
     * Falls back to default highlighting if no custom color is set.
     */
    fun getTextAttributesKey(itemType: ItemDetailType?): TextAttributesKey {
        val settings = SptIdSettingsState.getInstance()

        // Check if a custom color is set for this type
        val hasCustomColor = when (itemType) {
            ItemDetailType.QUEST -> settings.colorQuest != null
            ItemDetailType.TRADER -> settings.colorTrader != null
            ItemDetailType.LOCATION -> settings.colorLocation != null
            ItemDetailType.AMMO -> settings.colorAmmo != null
            ItemDetailType.WEAPON -> settings.colorWeapon != null
            ItemDetailType.CUSTOMIZATION -> settings.colorCustomization != null
            else -> settings.colorItem != null
        }

        // If no custom settings, use default IDE highlighting
        return if (!hasCustomColor && !settings.highlightBold &&
            settings.highlightItalic && settings.highlightUnderline &&
            !settings.highlightBackground
        ) {
            DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE
        } else {
            // Custom highlighting will be applied via TextAttributes
            TextAttributesKey.createTextAttributesKey("SPT_ID_${itemType?.name ?: "DEFAULT"}")
        }
    }
}
