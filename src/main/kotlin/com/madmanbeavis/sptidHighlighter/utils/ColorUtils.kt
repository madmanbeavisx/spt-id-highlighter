package com.madmanbeavis.sptidHighlighter.utils

import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Utility object for converting between JBColor and string representations.
 * Supports storing separate light/dark mode colors.
 */
object ColorUtils {
    private const val COLOR_SEPARATOR = "|"

    /**
     * Converts a JBColor to a string representation.
     * Format: "lightColor|darkColor" for different colors, or single hex for same color.
     */
    fun jbColorToString(color: JBColor?): String? {
        if (color == null) return null

        val lightHex = colorToHex(color)
        val darkHex = colorToHex(color.darker())

        // If light and dark are the same, store as single value
        return if (lightHex == darkHex) {
            lightHex
        } else {
            "$lightHex$COLOR_SEPARATOR$darkHex"
        }
    }

    /**
     * Converts a Color to a string representation.
     */
    fun colorToString(color: Color?): String? {
        if (color == null) return null
        return colorToHex(color)
    }

    /**
     * Converts a string representation to a JBColor.
     * Supports both "lightColor|darkColor" and single color formats.
     */
    fun stringToJBColor(colorString: String?): JBColor? {
        if (colorString == null) return null

        return try {
            if (colorString.contains(COLOR_SEPARATOR)) {
                // Dual color format: "lightColor|darkColor"
                val parts = colorString.split(COLOR_SEPARATOR)
                if (parts.size == 2) {
                    val lightColor = Color.decode(parts[0])
                    val darkColor = Color.decode(parts[1])
                    JBColor(lightColor, darkColor)
                } else {
                    null
                }
            } else {
                // Single color format
                val awtColor = Color.decode(colorString)
                JBColor(awtColor, awtColor)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts a string to a regular Color (not JBColor).
     * Takes the light color if dual format is provided.
     */
    fun stringToColor(colorString: String?): Color? {
        if (colorString == null) return null

        return try {
            if (colorString.contains(COLOR_SEPARATOR)) {
                val parts = colorString.split(COLOR_SEPARATOR)
                Color.decode(parts[0])
            } else {
                Color.decode(colorString)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a dual-color string from separate light and dark colors.
     */
    fun createDualColorString(lightColor: Color?, darkColor: Color?): String? {
        if (lightColor == null || darkColor == null) return null

        val lightHex = colorToHex(lightColor)
        val darkHex = colorToHex(darkColor)

        return if (lightHex == darkHex) {
            lightHex
        } else {
            "$lightHex$COLOR_SEPARATOR$darkHex"
        }
    }

    private fun colorToHex(color: Color): String {
        return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
    }
}
