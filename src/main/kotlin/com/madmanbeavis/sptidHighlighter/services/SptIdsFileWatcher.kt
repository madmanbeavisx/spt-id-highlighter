package com.madmanbeavis.sptidHighlighter.services

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.madmanbeavis.sptidHighlighter.models.ItemDetailType
import com.madmanbeavis.sptidHighlighter.models.ItemDetails
import com.madmanbeavis.sptidHighlighter.services.deserializers.FlexibleBooleanDeserializer
import com.madmanbeavis.sptidHighlighter.services.deserializers.FlexibleDoubleDeserializer
import com.madmanbeavis.sptidHighlighter.services.deserializers.FlexibleIntDeserializer
import com.madmanbeavis.sptidHighlighter.services.deserializers.ItemDetailTypeDeserializer
import com.madmanbeavis.sptidHighlighter.services.utils.FileSearchUtils
import com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState

class SptIdsFileWatcher(private val project: Project) {
    private val logger = Logger.getInstance(SptIdsFileWatcher::class.java)
    private val gson = GsonBuilder()
        .registerTypeAdapter(ItemDetailType::class.java, ItemDetailTypeDeserializer())
        .registerTypeAdapter(Boolean::class.java, FlexibleBooleanDeserializer())
        .registerTypeAdapter(Boolean::class.javaObjectType, FlexibleBooleanDeserializer())
        .registerTypeAdapter(Int::class.java, FlexibleIntDeserializer())
        .registerTypeAdapter(Int::class.javaObjectType, FlexibleIntDeserializer())
        .registerTypeAdapter(Double::class.java, FlexibleDoubleDeserializer())
        .registerTypeAdapter(Double::class.javaObjectType, FlexibleDoubleDeserializer())
        .setLenient() // Allow lenient parsing for malformed JSON
        .create()

    init {
        setupFileWatcher()
        loadInitialCustomIds()
    }

    private fun setupFileWatcher() {
        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    val customFilenames = SptIdSettingsState.getInstance().customIdFilenames
                    for (event in events) {
                        val file = event.file ?: continue
                        if (customFilenames.contains(file.name)) {
                            when (event) {
                                is VFileCreateEvent, is VFileContentChangeEvent -> {
                                    logger.info("Detected ${file.name} change at ${file.path}, reloading all...")
                                    loadAllCustomIds()
                                }
                                is VFileDeleteEvent -> {
                                    logger.info("Detected ${file.name} deletion at ${file.path}, reloading all...")
                                    loadAllCustomIds()
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    private fun loadInitialCustomIds() {
        loadAllCustomIds()
    }

    private fun loadAllCustomIds() {
        val allCustomItems = mutableMapOf<String, ItemDetails>()
        val settings = SptIdSettingsState.getInstance()
        val currentLanguage = settings.language
        val customFilenames = settings.customIdFilenames

        logger.info("Starting to load custom ID files. Current language: $currentLanguage, Filenames: $customFilenames")

        project.baseDir?.let { baseDir ->
            val sptIdsFiles = findAllCustomIdFiles(baseDir, customFilenames)
            logger.info("Found ${sptIdsFiles.size} custom ID files")

            sptIdsFiles.forEach { file ->
                try {
                    logger.info("Loading custom IDs from: ${file.path}")
                    val content = String(file.contentsToByteArray())

                    // Parse JSON with error tolerance
                    val jsonElement = JsonParser.parseString(content)
                    if (jsonElement.isJsonObject) {
                        val jsonObject = jsonElement.asJsonObject
                        logger.info("Parsing ${jsonObject.size()} entries from ${file.name}")

                        // Process each ID entry individually to handle partial failures
                        var loadedCount = 0
                        for ((id, value) in jsonObject.entrySet()) {
                            try {
                                logger.info("Processing ID: $id")
                                if (value.isJsonObject) {
                                    val langData = value.asJsonObject
                                    logger.info("ID $id has language data keys: ${langData.keySet()}")

                                    // Try current language first, then fallback to English, then any available language
                                    val langElement = langData.get(currentLanguage)
                                        ?: run {
                                            logger.info("No '$currentLanguage' data for ID $id, trying fallback language")
                                            langData.get(SptIdSettingsState.DEFAULT_FALLBACK_LANGUAGE)
                                        }
                                        ?: run {
                                            logger.info("No fallback language data for ID $id, using first available")
                                            langData.entrySet().firstOrNull()?.value
                                        }

                                    langElement?.let { element ->
                                        logger.info("Found language data for ID $id: $element")
                                        val details = gson.fromJson<ItemDetails>(element, ItemDetails::class.java)
                                        if (details != null && details.name.isNotEmpty()) {
                                            allCustomItems[id] = details
                                            loadedCount++
                                            logger.info("Successfully loaded ID $id: ${details.name}")
                                        } else {
                                            logger.warn("Parsed ID $id but details are null or have empty name")
                                        }
                                    } ?: logger.warn("No language data found for ID $id in any language")
                                } else {
                                    logger.warn("ID $id value is not a JSON object: ${value.javaClass.simpleName}")
                                }
                            } catch (e: Exception) {
                                logger.warn("Failed to parse entry for ID $id in ${file.path}: ${e.message}", e)
                            }
                        }
                        logger.info("Successfully loaded $loadedCount items from ${file.name}")
                    }
                } catch (e: Exception) {
                    logger.error("Failed to load custom ID file: ${file.path}", e)
                }
            }
        } ?: logger.warn("Project baseDir is null, cannot search for custom ID files")

        SptDataService.getInstance().setCustomItems(allCustomItems)
        logger.info("Loaded total of ${allCustomItems.size} custom items")
    }

    private fun findAllCustomIdFiles(directory: VirtualFile?, filenames: List<String>): List<VirtualFile> {
        val allFiles = mutableListOf<VirtualFile>()
        for (filename in filenames) {
            allFiles.addAll(FileSearchUtils.findAllFilesRecursively(directory, filename))
        }
        return allFiles
    }

}
