package com.madmanbeavis.sptidHighlighter.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.madmanbeavis.sptidHighlighter.services.SptIdsFileWatcher

class SptProjectListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        // Initialize the file watcher service for this project
        project.getService(SptIdsFileWatcher::class.java)
    }
}
