package com.madmanbeavis.sptidHighlighter.hints

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.madmanbeavis.sptidHighlighter.services.SptDataService
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class SptIdInlayHintProvider : InlayHintsProvider<NoSettings> {

    // Pre-compiled regex for performance
    private val sptIdPattern = Regex("[0-9a-f]{24}", RegexOption.IGNORE_CASE)

    // Pre-compiled cleaning regex
    private val cleaningPattern = Regex("""["'\s]""")

    override val key: SettingsKey<NoSettings> = SettingsKey("spt.id.hints")
    override val name: String = "SPT ID hints"
    override val previewText: String? = null

    override fun createSettings(): NoSettings = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        return SptIdInlayCollector(editor)
    }

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent {
                return javax.swing.JPanel()
            }
        }
    }

    private inner class SptIdInlayCollector(editor: Editor) : FactoryInlayHintsCollector(editor) {

        // Use HashSet for reliable O(1) lookups without hash collision issues
        private val processedOffsets = HashSet<Int>()

        // Limit to prevent unbounded memory growth
        private val MAX_HINTS_PER_FILE = 500
        private var hintsAdded = 0

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            // Critical: Check for cancellation every iteration to prevent UI freezing
            ProgressManager.checkCanceled()

            // Early bailout: Stop processing if we've hit the limit
            if (hintsAdded >= MAX_HINTS_PER_FILE) {
                return false
            }

            // Skip non-leaf nodes immediately
            if (element.children.isNotEmpty()) {
                return true
            }

            val text = element.text ?: return true

            // Early length check - SPT IDs are 24 chars, allow some padding
            if (text.length < 24 || text.length > 30) {
                return true
            }

            // Clean the text to check if it's exactly an SPT ID
            val cleanText = text.replace(cleaningPattern, "")

            // Must be exactly 24 chars after cleaning
            if (cleanText.length != 24) {
                return true
            }

            // Fast pattern check before expensive service call
            if (!sptIdPattern.matches(cleanText)) {
                return true
            }

            // Calculate offset early for deduplication
            val offset = element.textRange.endOffset

            // O(1) lookup for deduplication
            if (processedOffsets.contains(offset)) {
                return true
            }

            // Expensive service call - only after all fast checks pass
            val dataService = SptDataService.getInstance()
            val itemDetails = dataService.getItemDetails(cleanText)

            if (itemDetails != null) {
                // Add to processed set
                processedOffsets.add(offset)
                hintsAdded++

                // Create an inlay hint with just the item name
                val hintText = " ${itemDetails.name}"

                // Create presentation with tooltip, but without interactive features
                val tooltipContent = buildDetailedTooltip(itemDetails, cleanText)
                val textPresentation = factory.text(hintText)
                val backgroundPresentation = factory.roundWithBackground(textPresentation)

                // Add tooltip without making it clickable/interactive
                val presentation = factory.withTooltip(tooltipContent, backgroundPresentation)

                // Use relatesToPrecedingText=true to prevent the hint from being interactive
                sink.addInlineElement(offset, true, presentation, false)
            }

            return true
        }

        private fun buildDetailedTooltip(
            item: com.madmanbeavis.sptidHighlighter.models.ItemDetails,
            itemId: String
        ): String {
            val parts = mutableListOf<String>()

            // Item name
            parts.add(item.name)

            // ID
            parts.add("ID: $itemId")

            // Type
            if (item.type != null) {
                parts.add("Type: ${item.type.name}")
            }

            // Type-specific stats
            when (item.type) {
                com.madmanbeavis.sptidHighlighter.models.ItemDetailType.AMMO -> {
                    if (item.caliber != null) parts.add("Caliber: ${item.caliber}")
                    if (item.damage != null) parts.add("Damage: ${item.damage}")
                    if (item.penetrationPower != null) parts.add("Penetration: ${item.penetrationPower}")
                    if (item.armorDamage != null) parts.add("Armor Damage: ${item.armorDamage}")
                }

                com.madmanbeavis.sptidHighlighter.models.ItemDetailType.WEAPON -> {
                    if (item.caliber != null) parts.add("Caliber: ${item.caliber}")
                }

                else -> {}
            }

            // Weight
            if (item.weight != null) {
                parts.add("Weight: ${item.weight} kg")
            }

            return parts.joinToString("\n")
        }
    }
}
