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

        // Header
        sb.append("<h3>${item.name}")
        if (item.name != item.shortName) {
            sb.append(" [<em>${item.shortName}</em>]")
        }
        sb.append("</h3>")

        // Content
        sb.append("<pre>")

        appendValueIfDefined(sb, dataService.getTranslation("Type:"), item.type?.name)

        if (item.parent != null && item.parentID != null) {
            sb.append("${dataService.getTranslation("Parent:")} ${item.parent} - ")
            if (item.parentDetailLink != null) {
                sb.append("<a href=\"${item.parentDetailLink}\">${item.parentID}</a>")
            } else {
                sb.append(item.parentID)
            }
            sb.append("\n")
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
                if (item.trader != null && item.traderId != null) {
                    sb.append("${dataService.getTranslation("Trader:")} ${item.trader} - ")
                    if (item.traderLink != null) {
                        sb.append("<a href=\"${item.traderLink}\">${item.traderId}</a>")
                    } else {
                        sb.append(item.traderId)
                    }
                    sb.append("\n")
                } else {
                    appendValueIfDefined(sb, dataService.getTranslation("Trader ID:"), item.traderId)
                }
                appendValueIfDefined(sb, dataService.getTranslation("Quest Type:"), item.questType)
            }
            else -> {}
        }

        appendValueIfDefined(sb, dataService.getTranslation("Weight:"), item.weight)
        appendValueIfDefined(sb, dataService.getTranslation("Flea Blacklisted:"), item.fleaBlacklisted)
        appendValueIfDefined(sb, dataService.getTranslation("Unlocked By Default:"), item.unlockedByDefault)

        sb.append("</pre>")

        // Detail link
        if (item.detailLink != null) {
            sb.append("<hr>")
            sb.append("<p><strong>${dataService.getTranslation("Full Details:")}</strong><br>")
            sb.append("<a href=\"${item.detailLink}\">${item.detailLink}</a></p>")
        }

        return sb.toString()
    }

    private fun appendValueIfDefined(sb: StringBuilder, key: String, value: Any?) {
        if (value != null) {
            val valueStr = value.toString()
            if (valueStr.isNotBlank()) {
                sb.append("$key $valueStr\n")
            }
        }
    }
}
