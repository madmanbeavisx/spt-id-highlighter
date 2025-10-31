package com.madmanbeavis.sptidHighlighter.models

import com.google.gson.annotations.SerializedName

data class ItemDetails(
    @SerializedName("Name") val name: String,
    @SerializedName("ShortName") val shortName: String,
    @SerializedName("Type") val type: ItemDetailType? = null,
    val detailLink: String? = null,
    val parent: String? = null,
    val parentID: String? = null,
    val parentDetailLink: String? = null,
    @SerializedName("FleaBlacklisted") val fleaBlacklisted: Boolean? = null,
    @SerializedName("QuestItem") val questItem: Boolean? = null,
    @SerializedName("Weight") val weight: Double? = null,

    // Ammo
    @SerializedName("Caliber") val caliber: String? = null,
    @SerializedName("Damage") val damage: Int? = null,
    @SerializedName("ArmorDamage") val armorDamage: Int? = null,
    @SerializedName("PenetrationPower") val penetrationPower: Int? = null,

    // Traders
    val currency: String? = null,
    val unlockedByDefault: Boolean? = null,

    // Customization Items
    @SerializedName("Description") val description: String? = null,
    @SerializedName("BodyPart") val bodyPart: String? = null,
    @SerializedName("Sides") val sides: String? = null,
    @SerializedName("IntegratedArmorVest") val integratedArmorVest: Boolean? = null,
    @SerializedName("AvailableAsDefault") val availableAsDefault: Boolean? = null,
    val prefabPath: String? = null,

    // Locations
    val id: String? = null,
    val airdropChance: Double? = null,
    val escapeTimeLimit: Int? = null,
    val insurance: Boolean? = null,
    val bossSpawns: String? = null,

    // Quests
    val trader: String? = null,
    val traderId: String? = null,
    val traderLink: String? = null,
    val questType: String? = null
)

enum class ItemDetailType {
    ITEM,
    AMMO,
    WEAPON,
    ARMOR,
    HEADWEAR,
    KEY,
    MEDIKIT,
    DRUG,
    STIMULANT,
    FOOD,
    DRINK,
    CURRENCY,
    TRADER,
    CUSTOMIZATION,
    LOCATION,
    QUEST
}
