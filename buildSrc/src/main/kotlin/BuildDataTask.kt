import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.logging.Logger

abstract class BuildDataTask : DefaultTask() {

    @get:InputDirectory
    abstract val localesDirectory: DirectoryProperty

    @get:InputFile
    abstract val itemsFile: RegularFileProperty

    @get:InputFile
    abstract val handbookFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private val gson = Gson()
    private val logger = Logger.getLogger(BuildDataTask::class.java.name)

    @TaskAction
    fun buildData() {
        logger.info("Building SPT data...")

        localesDirectory.asFile.get().parentFile.parentFile.parentFile.parentFile.parentFile
        val localesDir = localesDirectory.asFile.get()
        val itemsFile = itemsFile.asFile.get()
        val handbookFile = handbookFile.asFile.get()
        val outputDir = outputDirectory.asFile.get()

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        // Load items data first
        val itemsData = loadItemsData(itemsFile)

        // Load handbook categories
        val categoriesData = loadCategoriesData(handbookFile)

        optimizeLocales(localesDir, outputDir, itemsData, categoriesData)

        logger.info("SPT data built successfully.")
    }

    private fun loadItemsData(itemsFile: File): Map<String, JsonObject> {
        logger.info("Loading items data...")

        if (!itemsFile.exists()) {
            logger.warning("Items file not found: ${itemsFile.absolutePath}")
            return emptyMap()
        }

        val itemsJson = gson.fromJson<Map<String, JsonObject>>(
            itemsFile.readText(),
            object : TypeToken<Map<String, JsonObject>>() {}.type
        )

        logger.info("Loaded ${itemsJson.size} items")
        return itemsJson
    }

    private fun loadCategoriesData(handbookFile: File): Map<String, JsonObject> {
        logger.info("Loading categories data...")

        if (!handbookFile.exists()) {
            logger.warning("Handbook file not found: ${handbookFile.absolutePath}")
            return emptyMap()
        }

        val handbookJson = gson.fromJson(
            handbookFile.readText(),
            JsonObject::class.java
        )

        val categoriesArray = handbookJson.getAsJsonArray("Categories")
        val categoriesMap = mutableMapOf<String, JsonObject>()

        categoriesArray?.forEach { element ->
            val category = element.asJsonObject
            val id = category.get("Id")?.asString
            if (id != null) {
                val categoryData = JsonObject()
                categoryData.addProperty("_parent", "CATEGORY")

                val parentId = category.get("ParentId")
                if (parentId != null && !parentId.isJsonNull) {
                    categoryData.addProperty("ParentId", parentId.asString)
                }

                val icon = category.get("Icon")
                if (icon != null && !icon.isJsonNull) {
                    categoryData.addProperty("Icon", icon.asString)
                }

                categoriesMap[id] = categoryData
            }
        }

        logger.info("Loaded ${categoriesMap.size} categories")
        return categoriesMap
    }

    private fun optimizeLocales(
        localesDir: File,
        outputDir: File,
        itemsData: Map<String, JsonObject>,
        categoriesData: Map<String, JsonObject>
    ) {
        logger.info("Optimizing locale data...")

        if (!localesDir.exists()) {
            logger.warning("Locales directory not found: ${localesDir.absolutePath}")
            return
        }

        localesDir.listFiles { file -> file.extension == "json" }?.forEach { file ->
            val outputFile = File(outputDir, file.name)

            val sourceData = gson.fromJson<Map<String, String>>(
                file.readText(),
                object : TypeToken<Map<String, String>>() {}.type
            )

            val transformedData = mutableMapOf<String, MutableMap<String, Any>>()

            sourceData.forEach { (key, value) ->
                val parts = key.split(" ")

                if (parts.size != 2) return@forEach

                val (id, property) = parts

                if (id.length != 24) return@forEach

                val normalizedProperty = property.replaceFirstChar { it.uppercase() }

                val itemData = transformedData.getOrPut(id) {
                    mutableMapOf<String, Any>("Name" to "", "ShortName" to "")
                }

                when (normalizedProperty) {
                    "Nickname" -> {
                        val trimmedValue = value.trim()
                        if (trimmedValue.isNotEmpty()) {
                            itemData["Name"] = trimmedValue
                            itemData["ShortName"] = trimmedValue
                        }
                    }
                    "Name", "ShortName" -> {
                        val trimmedValue = value.trim()
                        if (trimmedValue.isNotEmpty()) {
                            itemData[normalizedProperty] = trimmedValue

                            if ((itemData["Name"] as? String)?.isEmpty() == true) {
                                itemData["Name"] = trimmedValue
                            }
                            if ((itemData["ShortName"] as? String)?.isEmpty() == true) {
                                itemData["ShortName"] = trimmedValue
                            }
                        }
                    }
                }
            }

            // Add items from items.json that don't have locale entries
            itemsData.forEach { (id, itemJson) ->
                if (!transformedData.containsKey(id)) {
                    val itemName = itemJson.get("_name")?.asString
                    if (itemName != null) {
                        val itemData = mutableMapOf<String, Any>(
                            "Name" to itemName,
                            "ShortName" to itemName
                        )
                        transformedData[id] = itemData
                    }
                }
            }

            // Add categories from handbook that don't have locale entries
            categoriesData.forEach { (id, _) ->
                if (!transformedData.containsKey(id)) {
                    val itemData = mutableMapOf<String, Any>(
                        "Name" to id,  // Use ID as fallback name
                        "ShortName" to id
                    )
                    transformedData[id] = itemData
                }
            }

            // Enrich with item data and category data
            transformedData.forEach { (id, itemData) ->
                itemsData[id]?.let { itemJson ->
                    enrichItemData(itemData, itemJson, itemsData)
                }
                categoriesData[id]?.let { categoryJson ->
                    enrichCategoryData(itemData, categoryJson)
                }
            }

            // Remove entries where neither Name nor ShortName is set
            val filtered = transformedData.filterValues { item ->
                (item["Name"] as? String)?.isNotEmpty() == true ||
                (item["ShortName"] as? String)?.isNotEmpty() == true
            }

            if (filtered.isNotEmpty()) {
                outputFile.writeText(gson.toJson(filtered))
                logger.fine("Optimized ${outputFile.name}")
            } else {
                logger.fine("No valid entries found for ${outputFile.name}, no file written.")
            }
        }

        logger.info("Locale data optimized.")
    }

    private fun enrichItemData(itemData: MutableMap<String, Any>, itemJson: JsonObject, itemsData: Map<String, JsonObject>) {
        // Check for _type field (Node, Item, etc.)
        val itemType = itemJson.get("_type")?.asString
        if (itemType == "Node") {
            itemData["Type"] = "ITEM"  // Treat nodes as generic items

            // Check if _props exists and has IsEncoded
            itemJson.getAsJsonObject("_props")?.let { props ->
                props.get("IsEncoded")?.let { encoded ->
                    if (!encoded.isJsonNull) {
                        itemData["IsEncoded"] = encoded.asBoolean
                    }
                }
            }
            return
        }

        val props = itemJson.getAsJsonObject("_props") ?: return

        // Add Weight
        props.get("Weight")?.let { weight ->
            if (!weight.isJsonNull) {
                itemData["Weight"] = weight.asDouble
            }
        }

        // Add QuestItem
        props.get("QuestItem")?.let { questItem ->
            if (!questItem.isJsonNull) {
                itemData["QuestItem"] = questItem.asBoolean
            }
        }

        // Add CanSellOnRagfair (FleaBlacklisted is the inverse)
        props.get("CanSellOnRagfair")?.let { canSell ->
            if (!canSell.isJsonNull) {
                itemData["FleaBlacklisted"] = !canSell.asBoolean
            }
        }

        // Determine item type from parent
        val parent = itemJson.get("_parent")?.asString
        if (parent != null) {
            val itemType = determineItemType(parent)
            itemData["Type"] = itemType

            // Add type-specific data
            when (itemType) {
                "AMMO" -> {
                    // Check if this is an ammo pack with StackSlots
                    if (parent == "543be5cb4bdc2deb348b4568") {
                        enrichAmmoPackData(itemData, props, itemsData)
                    } else {
                        enrichAmmoData(itemData, props)
                    }
                }
                "CUSTOMIZATION" -> enrichCustomizationData(itemData, props)
            }
        }
    }

    private fun enrichAmmoPackData(itemData: MutableMap<String, Any>, props: JsonObject, itemsData: Map<String, JsonObject>) {
        // Try to get the actual ammo from StackSlots
        props.getAsJsonArray("StackSlots")?.let { stackSlots ->
            if (stackSlots.size() > 0) {
                val firstSlot = stackSlots[0].asJsonObject
                val filters = firstSlot.getAsJsonObject("_props")
                    ?.getAsJsonArray("filters")

                if (filters != null && filters.size() > 0) {
                    val filter = filters[0].asJsonObject.getAsJsonArray("Filter")
                    if (filter != null && filter.size() > 0) {
                        val ammoId = filter[0].asString
                        // Look up the actual ammo and get its data
                        itemsData[ammoId]?.let { ammoJson ->
                            val ammoProps = ammoJson.getAsJsonObject("_props")
                            if (ammoProps != null) {
                                enrichAmmoData(itemData, ammoProps)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun enrichCustomizationData(itemData: MutableMap<String, Any>, props: JsonObject) {
        // Add Description (from locale ID)
        props.get("Description")?.let { desc ->
            if (!desc.isJsonNull) {
                itemData["Description"] = desc.asString
            }
        }

        // Add Body Part
        props.get("BodyPart")?.let { bodyPart ->
            if (!bodyPart.isJsonNull) {
                itemData["BodyPart"] = bodyPart.asString
            }
        }

        // Add Sides
        props.get("Side")?.let { side ->
            if (!side.isJsonNull) {
                itemData["Sides"] = side.asString
            }
        }

        // Add IntegratedArmorVest
        props.get("IntegratedArmorVest")?.let { integrated ->
            if (!integrated.isJsonNull) {
                itemData["IntegratedArmorVest"] = integrated.asBoolean
            }
        }

        // Add AvailableAsDefault
        props.get("AvailableAsDefault")?.let { available ->
            if (!available.isJsonNull) {
                itemData["AvailableAsDefault"] = available.asBoolean
            }
        }
    }

    private fun enrichAmmoData(itemData: MutableMap<String, Any>, props: JsonObject) {
        // Add Caliber
        props.get("Caliber")?.let { caliber ->
            if (!caliber.isJsonNull) {
                itemData["Caliber"] = caliber.asString
            }
        }

        // Add Damage
        props.get("Damage")?.let { damage ->
            if (!damage.isJsonNull) {
                itemData["Damage"] = damage.asInt
            }
        }

        // Add ArmorDamage
        props.get("ArmorDamage")?.let { armorDamage ->
            if (!armorDamage.isJsonNull) {
                itemData["ArmorDamage"] = armorDamage.asInt
            }
        }

        // Add PenetrationPower
        props.get("PenetrationPower")?.let { penetrationPower ->
            if (!penetrationPower.isJsonNull) {
                itemData["PenetrationPower"] = penetrationPower.asInt
            }
        }
    }

    private fun enrichCategoryData(itemData: MutableMap<String, Any>, categoryJson: JsonObject) {
        // Set type to CATEGORY
        itemData["Type"] = "CATEGORY"

        // Add ParentId if present
        categoryJson.get("ParentId")?.let { parentId ->
            if (!parentId.isJsonNull) {
                itemData["ParentId"] = parentId.asString
            }
        }

        // Add Icon if present
        categoryJson.get("Icon")?.let { icon ->
            if (!icon.isJsonNull) {
                itemData["Icon"] = icon.asString
            }
        }
    }

    private fun determineItemType(parentId: String): String {
        return when (parentId) {
            "CATEGORY" -> "CATEGORY"
            "5485a8684bdc2da71d8b4567" -> "AMMO"
            "543be5cb4bdc2deb348b4568" -> "AMMO" // Ammo container/pack
            "5447b5cf4bdc2d65278b4567", "5447b5e04bdc2d62278b4567", "5447b5f14bdc2d61278b4567",
            "5447b5fc4bdc2d87278b4567", "5447b6094bdc2dc3278b4567", "5447b6194bdc2d67278b4567",
            "5447b6254bdc2dc3278b4568", "5447bed64bdc2d97278b4568", "5447bedf4bdc2d87278b4568",
            "5447bee84bdc2dc3278b4569" -> "WEAPON"
            "543be5dd4bdc2deb348b4569" -> "CURRENCY"
            "5448e54d4bdc2dcc718b4568" -> "ARMOR"
            "5a341c4086f77401f2541505", "5a341c4686f77469e155819e" -> "HEADWEAR"
            "543be5e94bdc2df1348b4568" -> "KEY"
            "5448f39d4bdc2d0a728b4568" -> "MEDIKIT"
            "5448f3a14bdc2d27728b4569" -> "DRUG"
            "5448f3a64bdc2d60728b456a" -> "STIMULANT"
            "5448e8d04bdc2ddf718b4569" -> "FOOD"
            "5448e8d64bdc2dce718b4568" -> "DRINK"
            "5cc084dd14c02e000b0550a3" -> "CUSTOMIZATION" // Customization item parent
            else -> "ITEM"
        }
    }
}
