package com.madmanbeavis.sptidHighlighter.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.madmanbeavis.sptidHighlighter.models.ItemDetails
import com.madmanbeavis.sptidHighlighter.services.utils.ResourceLoader
import com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState
import java.util.concurrent.ConcurrentHashMap

class SptDataService {
    private val logger = Logger.getInstance(SptDataService::class.java)

    // Use ConcurrentHashMap for thread-safe, lock-free reads
    @Volatile
    private var itemsCache: ConcurrentHashMap<String, ItemDetails> = ConcurrentHashMap()

    @Volatile
    private var translationsCache: Map<String, String> = emptyMap()

    @Volatile
    private var customItems: ConcurrentHashMap<String, ItemDetails> = ConcurrentHashMap()

    companion object {
        private const val DATABASE_PATH_PREFIX = "/database/"
        private const val TRANSLATIONS_PATH_PREFIX = "/translations/"
        private const val JSON_EXTENSION = ".json"
        private const val DEFAULT_LANGUAGE = "en"

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

    private fun loadItemsData(language: String): ConcurrentHashMap<String, ItemDetails> {
        val resourcePath = "$DATABASE_PATH_PREFIX$language$JSON_EXTENSION"

        return try {
            val data = ResourceLoader.loadJsonResource<Map<String, ItemDetails>>(resourcePath)

            ConcurrentHashMap(data ?: throw IllegalArgumentException("Failed to parse items data"))
        } catch (e: Exception) {
            logger.error("Failed to load items data for language: $language", e)

            // Fallback to English if not already trying English
            if (language != DEFAULT_LANGUAGE) {
                logger.info("Falling back to English for items data")
                loadItemsData(DEFAULT_LANGUAGE)
            } else {
                logger.warn("Failed to load English items data, using empty map")
                ConcurrentHashMap()
            }
        }
    }

    private fun loadTranslations(language: String): Map<String, String> {
        val resourcePath = "$TRANSLATIONS_PATH_PREFIX$language$JSON_EXTENSION"

        return try {
            val data = ResourceLoader.loadJsonResource<Map<String, String>>(resourcePath)

            data ?: throw IllegalArgumentException("Failed to parse translations")
        } catch (e: Exception) {
            logger.error("Failed to load translations for language: $language", e)

            // Fallback to English if not already trying English
            if (language != DEFAULT_LANGUAGE) {
                logger.info("Falling back to English for translations")
                loadTranslations(DEFAULT_LANGUAGE)
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
        customItems = ConcurrentHashMap(items)
        logger.info("Loaded ${items.size} custom items from .sptids file")
    }

    @Synchronized
    fun clearCustomItems() {
        customItems = ConcurrentHashMap()
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
