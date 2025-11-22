package com.madmanbeavis.sptidHighlighter.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.madmanbeavis.sptidHighlighter.services.SptDataService
import com.madmanbeavis.sptidHighlighter.utils.HighlightStyleHelper

class SptIdAnnotator : Annotator {

    // Pre-compiled regex for 24-character hexadecimal strings
    private val sptIdPattern = Regex("[0-9a-f]{24}", RegexOption.IGNORE_CASE)

    // Limit annotations per element to prevent performance issues
    private val MAX_ANNOTATIONS_PER_ELEMENT = 50

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Check for cancellation to prevent UI freezing
        ProgressManager.checkCanceled()

        val text = element.text ?: return

        // Early exit: text must be at least 24 chars to contain an SPT ID
        if (text.length < 24) return

        // Early exit: Skip very large text blocks to avoid performance issues
        if (text.length > 10000) return

        val dataService = SptDataService.getInstance()

        // Find all potential SPT IDs in the text
        val matches = sptIdPattern.findAll(text)

        var annotationCount = 0
        for (match in matches) {
            // Check for cancellation in loop
            ProgressManager.checkCanceled()

            // Limit annotations to prevent performance degradation
            if (annotationCount >= MAX_ANNOTATIONS_PER_ELEMENT) {
                break
            }

            val potentialId = match.value

            val textRange = TextRange(
                element.textRange.startOffset + match.range.first,
                element.textRange.startOffset + match.range.last + 1
            )

            val itemDetails = dataService.getItemDetails(potentialId)
            if (itemDetails != null) {
                annotationCount++

                // Get custom text attributes based on user settings and item type
                val textAttributes = HighlightStyleHelper.getTextAttributesForType(itemDetails.type)

                // Add annotation with custom styling
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(textRange)
                    .enforcedTextAttributes(textAttributes)
                    .create()
            }
        }
    }
}
