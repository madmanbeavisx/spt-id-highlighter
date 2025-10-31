package com.madmanbeavis.sptidHighlighter.services

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.madmanbeavis.sptidHighlighter.models.ItemDetails
import com.madmanbeavis.sptidHighlighter.models.ItemDetailType
import com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState
import java.lang.reflect.Type

@Service(Service.Level.PROJECT)
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
    private val SPTIDS_FILENAME = ".sptids"

    // Custom deserializer for ItemDetailType that accepts strings
    private class ItemDetailTypeDeserializer : JsonDeserializer<ItemDetailType> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ItemDetailType? {
            return try {
                val value = json.asString.uppercase()
                ItemDetailType.valueOf(value)
            } catch (e: Exception) {
                // If it fails, default to ITEM
                ItemDetailType.ITEM
            }
        }
    }

    // Custom deserializer for Boolean that accepts "Unknown" as null
    private class FlexibleBooleanDeserializer : JsonDeserializer<Boolean> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Boolean? {
            return try {
                when {
                    json.isJsonPrimitive && json.asJsonPrimitive.isBoolean -> json.asBoolean
                    json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                        val str = json.asString.lowercase()
                        when (str) {
                            "true", "yes", "1" -> true
                            "false", "no", "0" -> false
                            "unknown", "" -> null
                            else -> null
                        }
                    }
                    json.isJsonNull -> null
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    // Custom deserializer for Int that accepts strings and handles null/unknown
    private class FlexibleIntDeserializer : JsonDeserializer<Int> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Int? {
            return try {
                when {
                    json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> json.asInt
                    json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                        val str = json.asString
                        if (str.lowercase() == "unknown" || str.isEmpty()) {
                            null
                        } else {
                            str.toIntOrNull()
                        }
                    }
                    json.isJsonNull -> null
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    // Custom deserializer for Double that accepts strings and handles null/unknown
    private class FlexibleDoubleDeserializer : JsonDeserializer<Double> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Double? {
            return try {
                when {
                    json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> json.asDouble
                    json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                        val str = json.asString
                        if (str.lowercase() == "unknown" || str.isEmpty()) {
                            null
                        } else {
                            str.toDoubleOrNull()
                        }
                    }
                    json.isJsonNull -> null
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    init {
        setupFileWatcher()
        loadInitialCustomIds()
    }

    private fun setupFileWatcher() {
        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    for (event in events) {
                        val file = event.file ?: continue
                        if (file.name == SPTIDS_FILENAME) {
                            when (event) {
                                is VFileCreateEvent, is VFileContentChangeEvent -> {
                                    logger.info("Detected $SPTIDS_FILENAME change at ${file.path}, reloading all...")
                                    loadAllCustomIds()
                                }
                                is VFileDeleteEvent -> {
                                    logger.info("Detected $SPTIDS_FILENAME deletion at ${file.path}, reloading all...")
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
        val currentLanguage = SptIdSettingsState.getInstance().language

        logger.info("Starting to load .sptids files. Current language: $currentLanguage")

        project.baseDir?.let { baseDir ->
            val sptIdsFiles = findAllSptIdsFiles(baseDir)
            logger.info("Found ${sptIdsFiles.size} .sptids files")

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
                                if (value.isJsonObject) {
                                    val langData = value.asJsonObject
                                    langData.get(currentLanguage)?.let { langElement ->
                                        val details = gson.fromJson<ItemDetails>(langElement, ItemDetails::class.java)
                                        if (details != null && details.name.isNotEmpty()) {
                                            allCustomItems[id] = details
                                            loadedCount++
                                        } else {
                                            logger.warn("Parsed ID $id but details are null or have empty name")
                                        }
                                    } ?: logger.warn("No '$currentLanguage' language data found for ID $id")
                                }
                            } catch (e: Exception) {
                                logger.warn("Failed to parse entry for ID $id in ${file.path}: ${e.message}", e)
                            }
                        }
                        logger.info("Successfully loaded $loadedCount items from ${file.name}")
                    }
                } catch (e: Exception) {
                    logger.error("Failed to load $SPTIDS_FILENAME file: ${file.path}", e)
                }
            }
        } ?: logger.warn("Project baseDir is null, cannot search for .sptids files")

        SptDataService.getInstance().setCustomItems(allCustomItems)
        logger.info("Loaded total of ${allCustomItems.size} custom items")
    }

    private fun findAllSptIdsFiles(directory: VirtualFile?): List<VirtualFile> {
        if (directory == null || !directory.isDirectory) return emptyList()

        val result = mutableListOf<VirtualFile>()

        fun searchRecursively(dir: VirtualFile) {
            try {
                for (child in dir.children) {
                    if (child.isDirectory) {
                        // Skip common directories that shouldn't contain .sptids files
                        if (!child.name.startsWith(".") &&
                            child.name != "node_modules" &&
                            child.name != "build" &&
                            child.name != "dist" &&
                            child.name != "out") {
                            searchRecursively(child)
                        }
                    } else if (child.name == SPTIDS_FILENAME) {
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
