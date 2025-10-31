package com.madmanbeavis.sptidHighlighter.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.madmanbeavis.sptidHighlighter.models.ItemDetails
import com.madmanbeavis.sptidHighlighter.services.utils.ResourceLoader
import com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState

class SptDataService {
    private val logger = Logger.getInstance(SptDataService::class.java)

    @Volatile
    private var itemsCache: MutableMap<String, ItemDetails> = mutableMapOf()

    @Volatile
    private var translationsCache: Map<String, String> = emptyMap()

    @Volatile
    private var customItems: Map<String, ItemDetails> = emptyMap()

    companion object {
        fun getInstance(): SptDataService {
            return ApplicationManager.getApplication().getService(SptDataService::class.java)
        }
    }

    init {
        loadData()
    }

    fun loadData() {
        val settings = SptIdSettingsState.getInstance()
        val language = settings.language

        logger.info("Loading SPT data for language: $language")

        // Load base data
        itemsCache = loadItemsData(language)
        logger.info("Loaded ${itemsCache.size} base items")

        // Load translations
        translationsCache = loadTranslations(language)
        logger.info("Loaded ${translationsCache.size} translations")

        logger.info("SPT Data Service initialized with language: $language")
    }

    private fun loadItemsData(language: String): MutableMap<String, ItemDetails> {
        val resourcePath = "/database/$language.json"

        return try {
            val data = ResourceLoader.loadJsonResource<Map<String, ItemDetails>>(resourcePath)

            if (data != null) {
                data.toMutableMap()
            } else {
                throw IllegalArgumentException("Failed to parse items data")
            }
        } catch (e: Exception) {
            logger.error("Failed to load items data for language: $language", e)

            // Fallback to English if not already trying English
            if (language != "en") {
                logger.info("Falling back to English for items data")
                loadItemsData("en")
            } else {
                logger.warn("Failed to load English items data, using empty map")
                mutableMapOf()
            }
        }
    }

    private fun loadTranslations(language: String): Map<String, String> {
        val resourcePath = "/translations/$language.json"

        return try {
            val data = ResourceLoader.loadJsonResource<Map<String, String>>(resourcePath)

            data ?: throw IllegalArgumentException("Failed to parse translations")
        } catch (e: Exception) {
            logger.error("Failed to load translations for language: $language", e)

            // Fallback to English if not already trying English
            if (language != "en") {
                logger.info("Falling back to English for translations")
                loadTranslations("en")
            } else {
                logger.warn("Failed to load English translations, using empty map")
                emptyMap()
            }
        }
    }

    fun getItemDetails(id: String): ItemDetails? {
        // Check custom items first
        customItems[id]?.let { return it }
        // Then check base items
        return itemsCache[id]
    }

    fun getAllItemIds(): Set<String> {
        return (itemsCache.keys + customItems.keys).toSet()
    }

    fun getTranslation(key: String): String {
        return translationsCache[key] ?: key
    }

    @Synchronized
    fun setCustomItems(items: Map<String, ItemDetails>) {
        customItems = items
        logger.info("Loaded ${items.size} custom items from .sptids file")
    }

    @Synchronized
    fun clearCustomItems() {
        customItems = emptyMap()
        logger.info("Cleared custom items")
    }

    /**
     * Gets the current language setting.
     */
    fun getCurrentLanguage(): String {
        return SptIdSettingsState.getInstance().language
    }

    /**
     * Reloads all data from resources. Useful when language changes.
     */
    @Synchronized
    fun reloadData() {
        loadData()
    }
}
