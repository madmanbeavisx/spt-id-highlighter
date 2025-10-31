package com.madmanbeavis.sptidHighlighter.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.madmanbeavis.sptidHighlighter.utils.MongoIdGenerator

/**
 * Action to generate and insert a valid MongoDB ObjectId at the current cursor position.
 * Default keybind: Ctrl+Shift+Alt+W
 */
class GenerateMongoIdAction : AnAction("Generate MongoDB ObjectId") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        
        // Check if there's a selection
        val selectionModel = editor.selectionModel
        val hasSelection = selectionModel.hasSelection()
        
        if (hasSelection) {
            // Multiple IDs if text is selected
            generateMultipleIds(editor, project)
        } else {
            // Single ID at cursor
            generateSingleId(editor, project)
        }
    }
    
    private fun generateSingleId(editor: Editor, project: Project) {
        val mongoId = MongoIdGenerator.generate()
        
        WriteCommandAction.runWriteCommandAction(project) {
            val caretModel = editor.caretModel
            val offset = caretModel.offset
            
            editor.document.insertString(offset, mongoId)
            caretModel.moveToOffset(offset + mongoId.length)
        }
    }
    
    private fun generateMultipleIds(editor: Editor, project: Project) {
        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText ?: return
        
        // Count lines in selection
        val lines = selectedText.lines()
        val count = lines.size
        
        // Ask for confirmation if more than 10 lines
        if (count > 10) {
            val result = Messages.showYesNoDialog(
                project,
                "Generate $count MongoDB ObjectIds?",
                "Generate Multiple IDs",
                Messages.getQuestionIcon()
            )
            if (result != Messages.YES) return
        }
        
        // Generate IDs
        val ids = MongoIdGenerator.generate(count)
        val replacement = ids.joinToString("\n")
        
        WriteCommandAction.runWriteCommandAction(project) {
            val start = selectionModel.selectionStart
            val end = selectionModel.selectionEnd
            
            editor.document.replaceString(start, end, replacement)
            selectionModel.removeSelection()
            editor.caretModel.moveToOffset(start + replacement.length)
        }
    }
    
    override fun update(e: AnActionEvent) {
        // Only enable if there's an active editor
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null
    }
}
