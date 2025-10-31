package com.madmanbeavis.sptidHighlighter.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.madmanbeavis.sptidHighlighter.services.SptIdsFileWatcher

class SptProjectListener : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Initialize the file watcher service for this project
        project.getService(SptIdsFileWatcher::class.java)
    }
}
