package com.madmanbeavis.sptidHighlighter.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.madmanbeavis.sptidHighlighter.services.SptDataService

class SptIdAnnotator : Annotator {

    // SPT IDs are 24-character hexadecimal strings
    private val sptIdPattern = Regex("[0-9a-f]{24}", RegexOption.IGNORE_CASE)

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text ?: return
        val dataService = SptDataService.getInstance()

        // Find all potential SPT IDs in the text
        val matches = sptIdPattern.findAll(text)

        for (match in matches) {
            val potentialId = match.value

            val textRange = TextRange(
                element.textRange.startOffset + match.range.first,
                element.textRange.startOffset + match.range.last + 1
            )

            if (dataService.getItemDetails(potentialId) != null) {
                // Add annotation with italic and underline styling for known IDs
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
                    .create()
            }
        }
    }
}
