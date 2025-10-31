package com.madmanbeavis.sptidHighlighter.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.madmanbeavis.sptidHighlighter.services.SptDataService
import com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState

/**
 * Diagnostic action to check plugin status and loaded items.
 * Accessible via Tools menu or Find Action (Ctrl+Shift+A / Cmd+Shift+A)
 */
class DiagnosticAction : AnAction("SPT ID Highlighter: Show Diagnostics") {
    private val logger = Logger.getInstance(DiagnosticAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dataService = SptDataService.getInstance()
        val settings = SptIdSettingsState.getInstance()

        // Gather diagnostic information
        val allIds = dataService.getAllItemIds()
        val language = settings.language

        // Count custom vs base items (approximate)
        val totalItems = allIds.size

        val message = buildString {
            appendLine("SPT ID Highlighter Diagnostics")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Language: $language")
            appendLine("Total loaded items: $totalItems")
            appendLine()
            appendLine("Check idea.log for detailed loading info")
            appendLine("(Help → Diagnostic Tools → Show Log in Explorer)")
        }

        logger.info(message)

        // Show notification
        NotificationGroupManager.getInstance()
            .getNotificationGroup("SPT ID Highlighter")
            .createNotification(
                "SPT ID Highlighter Diagnostics",
                message,
                NotificationType.INFORMATION
            )
            .notify(project)
    }
}
