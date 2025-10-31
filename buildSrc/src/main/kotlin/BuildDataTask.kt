import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class BuildDataTask : DefaultTask() {

    private val gson = Gson()

    @TaskAction
    fun buildData() {
        println("Building SPT data...")

        val projectDir = project.layout.projectDirectory.asFile
        val localesDir = File(projectDir, "src/main/resources/assets/database/locales/global")
        val itemsFile = File(projectDir, "src/main/resources/assets/database/templates/items.json")
        val outputDir = File(projectDir, "src/main/resources/database")

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        // Load items data first
        val itemsData = loadItemsData(itemsFile)

        optimizeLocales(localesDir, outputDir, itemsData)

        println("SPT data built successfully.")
    }

    private fun loadItemsData(itemsFile: File): Map<String, JsonObject> {
        println("Loading items data...")

        if (!itemsFile.exists()) {
            println("Items file not found: ${itemsFile.absolutePath}")
            return emptyMap()
        }

        val itemsJson = gson.fromJson<Map<String, JsonObject>>(
            itemsFile.readText(),
            object : TypeToken<Map<String, JsonObject>>() {}.type
        )

        println("Loaded ${itemsJson.size} items")
        return itemsJson
    }

    private fun optimizeLocales(localesDir: File, outputDir: File, itemsData: Map<String, JsonObject>) {
        println("Optimizing locale data...")

        if (!localesDir.exists()) {
            println("Locales directory not found: ${localesDir.absolutePath}")
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

            // Enrich with item data
            transformedData.forEach { (id, itemData) ->
                itemsData[id]?.let { itemJson ->
                    enrichItemData(itemData, itemJson, itemsData)
                }
            }

            // Remove entries where neither Name nor ShortName is set
            val filtered = transformedData.filterValues { item ->
                (item["Name"] as? String)?.isNotEmpty() == true ||
                (item["ShortName"] as? String)?.isNotEmpty() == true
            }

            if (filtered.isNotEmpty()) {
                outputFile.writeText(gson.toJson(filtered))
                println("Optimized ${outputFile.name}")
            } else {
                println("No valid entries found for ${outputFile.name}, no file written.")
            }
        }

        println("Locale data optimized.")
    }

    private fun enrichItemData(itemData: MutableMap<String, Any>, itemJson: JsonObject, itemsData: Map<String, JsonObject>) {
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

    private fun determineItemType(parentId: String): String {
        return when (parentId) {
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
