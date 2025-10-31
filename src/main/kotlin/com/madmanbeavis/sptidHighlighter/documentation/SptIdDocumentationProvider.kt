package com.madmanbeavis.sptidHighlighter.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
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

        // Header with name
        sb.append("<div style='padding: 8px;'>")
        sb.append("<div style='font-size: 14px; font-weight: bold; margin-bottom: 8px;'>")
        sb.append(item.name)
        sb.append("</div>")

        // Separator line
        sb.append("<div style='border-bottom: 1px solid #3C3F41; margin-bottom: 8px;'></div>")

        // Content in monospace
        sb.append("<div style='font-family: monospace;'>")

        appendValueIfDefined(sb, dataService.getTranslation("Type:"), item.type?.name)

        if (item.parent != null && item.parentID != null) {
            sb.append("${dataService.getTranslation("Parent:")} ${item.parent} - ")
            if (item.parentDetailLink != null) {
                sb.append("<a href=\"${item.parentDetailLink}\">${item.parentID}</a>")
            } else {
                sb.append(item.parentID)
            }
            sb.append("<br>")
        }

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
                    // Determine trader name: check if Trader field is an ID or actual name
                    val traderName = if (item.trader != null) {
                        // Check if the trader field is actually an ID (24 hex chars)
                        if (item.trader.matches(Regex("[0-9a-f]{24}", RegexOption.IGNORE_CASE))) {
                            // It's an ID, look it up
                            dataService.getItemDetails(item.trader)?.name ?: "Unknown Trader"
                        } else {
                            // It's already a name
                            item.trader
                        }
                    } else {
                        // No trader field, try looking up by traderId
                        dataService.getItemDetails(item.traderId)?.name ?: "Unknown Trader"
                    }
                    
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

        // Detail link
        if (item.detailLink != null) {
            sb.append("<div style='border-top: 1px solid #3C3F41; margin-top: 8px; padding-top: 8px;'>")
            sb.append("<strong>${dataService.getTranslation("Full Details:")}</strong><br>")
            sb.append("<a href=\"${item.detailLink}\">${item.detailLink}</a>")
            sb.append("</div>")
        }

        sb.append("</div>")

        return sb.toString()
    }

    private fun appendValueIfDefined(sb: StringBuilder, key: String, value: Any?) {
        if (value != null) {
            val valueStr = value.toString()
            if (valueStr.isNotBlank()) {
                sb.append("$key $valueStr<br>")
            }
        }
    }
}
