package com.madmanbeavis.sptidHighlighter.services.utils

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile

/**
 * Utility functions for searching files in the project.
 */
object FileSearchUtils {
    
    private val logger = Logger.getInstance(FileSearchUtils::class.java)
    
    /**
     * Recursively finds all files with the given filename in the directory tree.
     * Skips common build/dependency directories.
     */
    fun findAllFilesRecursively(
        directory: VirtualFile?,
        filename: String,
        excludeDirs: Set<String> = setOf("node_modules", "build", "dist", "out")
    ): List<VirtualFile> {
        if (directory == null || !directory.isDirectory) return emptyList()
        
        val result = mutableListOf<VirtualFile>()
        
        fun searchRecursively(dir: VirtualFile) {
            try {
                for (child in dir.children) {
                    if (child.isDirectory) {
                        // Skip common directories that shouldn't be searched
                        if (!child.name.startsWith(".") && child.name !in excludeDirs) {
                            searchRecursively(child)
                        }
                    } else if (child.name == filename) {
                        result.add(child)
                    }
                }
            } catch (e: Exception) {
                logger.warn("Error searching directory: ${dir.path}", e)
            }
        }
        
        searchRecursively(directory)
        return result
    }
}
