package com.madmanbeavis.sptidHighlighter.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.JBColor
import com.madmanbeavis.sptidHighlighter.models.ItemDetailType
import com.madmanbeavis.sptidHighlighter.models.ItemDetails
import com.madmanbeavis.sptidHighlighter.services.SptDataService

class SptIdDocumentationProvider : AbstractDocumentationProvider() {

    // SPT IDs are 24-character hexadecimal strings
    private val sptIdPattern = Regex("[0-9a-f]{24}", RegexOption.IGNORE_CASE)

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val text = element?.text ?: return null
        val dataService = SptDataService.getInstance()

        // First try exact match after cleaning
        val cleanText = text.trim('"', '\'', ' ', '\n', '\r', '\t')
        dataService.getItemDetails(cleanText)?.let {
            return buildDocumentation(it, dataService)
        }

        // If no exact match, search for SPT IDs within the text
        val matches = sptIdPattern.findAll(text)
        for (match in matches) {
            val potentialId = match.value
            dataService.getItemDetails(potentialId)?.let {
                return buildDocumentation(it, dataService)
            }
        }

        return null
    }

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        return contextElement
    }

    private fun buildDocumentation(item: ItemDetails, dataService: SptDataService): String {
        val sb = StringBuilder()
        val settings = com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState.getInstance()

        // Get custom colors or use defaults (no background color - let IDE handle it)
        // Extract color appropriate for current theme (light or dark)
        val fgColor =
            settings.popupForegroundColor?.let { extractColorForCurrentTheme(it) } ?: getDefaultForegroundColor()
        val borderColor = settings.popupBorderColor?.let { extractColorForCurrentTheme(it) } ?: "#3C3F41"

        // Simple wrapper - no background, let IDE theme handle it
        val colorStyle = if (fgColor != "inherit") "color: $fgColor;" else ""
        sb.append("<div style='padding: 8px; $colorStyle'>")

        // Header with name
        sb.append("<div style='font-size: 14px; font-weight: bold; margin-bottom: 8px;'>")
        sb.append(item.name)
        sb.append("</div>")

        // Separator line
        sb.append("<div style='border-bottom: 1px solid $borderColor; margin-bottom: 8px;'></div>")

        // Content in monospace
        sb.append("<div style='font-family: monospace;'>")

        appendValueIfDefined(sb, dataService.getTranslation("Type:"), item.type?.name)

        // Type-specific fields
        when (item.type) {
            ItemDetailType.AMMO -> {
                appendValueIfDefined(sb, dataService.getTranslation("Caliber:"), item.caliber)
                appendValueIfDefined(sb, dataService.getTranslation("Damage:"), item.damage)
                appendValueIfDefined(sb, dataService.getTranslation("Armor Damage:"), item.armorDamage)
                appendValueIfDefined(sb, dataService.getTranslation("Penetration Power:"), item.penetrationPower)
            }
            ItemDetailType.CUSTOMIZATION -> {
                appendValueIfDefined(sb, dataService.getTranslation("Description:"), item.description)
                appendValueIfDefined(sb, dataService.getTranslation("Body Part:"), item.bodyPart)
                appendValueIfDefined(sb, dataService.getTranslation("Sides:"), item.sides)
                appendValueIfDefined(sb, dataService.getTranslation("Integrated Armor:"), item.integratedArmorVest)
                appendValueIfDefined(sb, dataService.getTranslation("Available By Default:"), item.availableAsDefault)
                appendValueIfDefined(sb, dataService.getTranslation("Prefab Path:"), item.prefabPath)
            }
            ItemDetailType.LOCATION -> {
                appendValueIfDefined(sb, dataService.getTranslation("Map ID:"), item.id)
                appendValueIfDefined(sb, dataService.getTranslation("Airdrop Chance:"), item.airdropChance)
                appendValueIfDefined(sb, dataService.getTranslation("Time Limit:"), item.escapeTimeLimit)
                appendValueIfDefined(sb, dataService.getTranslation("Insurance:"), item.insurance)
                appendValueIfDefined(sb, dataService.getTranslation("Boss Spawns:"), item.bossSpawns)
            }
            ItemDetailType.QUEST -> {
                if (item.traderId != null) {
                    val traderName = resolveTraderName(item.trader, item.traderId, dataService)
                    sb.append("${dataService.getTranslation("Trader:")} $traderName - ")

                    if (item.traderLink != null) {
                        sb.append("<a href=\"${item.traderLink}\">${item.traderId}</a>")
                    } else {
                        sb.append(item.traderId)
                    }
                    sb.append("<br>")
                }
                appendValueIfDefined(sb, dataService.getTranslation("Quest Type:"), item.questType)
            }
            else -> {}
        }

        appendValueIfDefined(sb, dataService.getTranslation("Weight:"), item.weight)
        appendValueIfDefined(sb, dataService.getTranslation("Flea Blacklisted:"), item.fleaBlacklisted)
        appendValueIfDefined(sb, dataService.getTranslation("Unlocked By Default:"), item.unlockedByDefault)

        sb.append("</div>")

        sb.append("</div>") // Close main wrapper

        return sb.toString()
    }

    private fun getDefaultBackgroundColor(): String {
        // Try to detect if we're in dark mode or light mode
        // For now, return transparent to let IDE theme handle it
        return "transparent"
    }

    private fun getDefaultForegroundColor(): String {
        // Return inherit to use IDE's default text color
        return "inherit"
    }

    /**
     * Extracts the appropriate color for the current theme (light or dark).
     * Handles both single color and "lightColor|darkColor" formats.
     */
    private fun extractColorForCurrentTheme(colorString: String): String {
        if (!colorString.contains("|")) {
            return colorString // Single color format
        }

        // Dual color format: "lightColor|darkColor"
        val parts = colorString.split("|")
        if (parts.size != 2) return colorString

        // Detect if we're in dark mode by checking if IDE background is dark
        val isDarkMode = JBColor.isBright()
        return if (isDarkMode) parts[1] else parts[0]
    }

    private fun addAlphaToColor(hexColor: String, alpha: Double): String {
        if (hexColor == "transparent") return "transparent"
        if (hexColor == "inherit") return "inherit"

        return try {
            val color = java.awt.Color.decode(hexColor)
            val r = color.red
            val g = color.green
            val b = color.blue
            "rgba($r, $g, $b, $alpha)"
        } catch (e: Exception) {
            hexColor
        }
    }

    private fun appendValueIfDefined(sb: StringBuilder, key: String, value: Any?) {
        if (value != null) {
            val valueStr = value.toString()
            if (valueStr.isNotBlank()) {
                sb.append("$key $valueStr<br>")
            }
        }
    }

    /**
     * Resolves a trader name from trader and traderId fields.
     * First checks if trader field contains a name or ID, then falls back to traderId lookup.
     */
    private fun resolveTraderName(trader: String?, traderId: String?, dataService: SptDataService): String {
        // If trader field exists, check if it's an ID or a name
        trader?.let {
            return if (it.matches(Regex("[0-9a-f]{24}", RegexOption.IGNORE_CASE))) {
                // It's an ID, look it up
                dataService.getItemDetails(it)?.name ?: "Unknown Trader"
            } else {
                // It's already a name
                it
            }
        }

        // Fall back to traderId lookup
        return traderId?.let { dataService.getItemDetails(it)?.name } ?: "Unknown Trader"
    }
}
