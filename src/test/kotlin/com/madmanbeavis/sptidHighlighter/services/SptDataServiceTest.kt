package com.madmanbeavis.sptidHighlighter.services

import com.madmanbeavis.sptidHighlighter.models.ItemDetails
import com.madmanbeavis.sptidHighlighter.models.ItemDetailType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Unit tests for SptDataService.
 * Note: These tests focus on data models and logic that don't require IntelliJ Platform mocking.
 * Full integration tests would require IntelliJ Platform test framework.
 */
class SptDataServiceTest {

    @Test
    fun `ItemDetails can be created with all fields`() {
        val item = ItemDetails(
            name = "Test Item",
            shortName = "TI",
            type = ItemDetailType.WEAPON,
            weight = 1.5,
            damage = 50,
            penetrationPower = 30
        )

        assertEquals("Test Item", item.name)
        assertEquals("TI", item.shortName)
        assertEquals(ItemDetailType.WEAPON, item.type)
        assertEquals(1.5, item.weight)
        assertEquals(50, item.damage)
        assertEquals(30, item.penetrationPower)
    }

    @Test
    fun `ItemDetails can be created with minimal fields`() {
        val item = ItemDetails(
            name = "Minimal Item",
            shortName = "MI"
        )

        assertEquals("Minimal Item", item.name)
        assertEquals("MI", item.shortName)
        assertEquals(null, item.type)
        assertEquals(null, item.weight)
    }

    @Test
    fun `ItemDetailType enum has all expected values`() {
        val types = ItemDetailType.values()

        assert(types.contains(ItemDetailType.ITEM))
        assert(types.contains(ItemDetailType.WEAPON))
        assert(types.contains(ItemDetailType.AMMO))
        assert(types.contains(ItemDetailType.ARMOR))
        assert(types.contains(ItemDetailType.QUEST))
    }
}
