package com.madmanbeavis.sptidHighlighter.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MongoIdGeneratorTest {
    
    @Test
    fun `generated ID has correct length`() {
        val id = MongoIdGenerator.generate()
        assertEquals(24, id.length)
    }
    
    @Test
    fun `generated ID contains only hex characters`() {
        val id = MongoIdGenerator.generate()
        assertTrue(id.all { it in '0'..'9' || it in 'a'..'f' })
    }
    
    @Test
    fun `generated IDs are unique`() {
        val ids = MongoIdGenerator.generate(100)
        val uniqueIds = ids.toSet()
        assertEquals(100, uniqueIds.size)
    }
    
    @Test
    fun `isValid returns true for valid ObjectId`() {
        val id = MongoIdGenerator.generate()
        assertTrue(MongoIdGenerator.isValid(id))
    }
    
    @Test
    fun `isValid returns false for invalid length`() {
        assertFalse(MongoIdGenerator.isValid("123"))
        assertFalse(MongoIdGenerator.isValid("12345678901234567890123456789"))
    }
    
    @Test
    fun `isValid returns false for non-hex characters`() {
        assertFalse(MongoIdGenerator.isValid("12345678901234567890123g"))
        assertFalse(MongoIdGenerator.isValid("xyz123456789012345678901"))
    }
    
    @Test
    fun `isValid returns true for uppercase hex`() {
        assertTrue(MongoIdGenerator.isValid("507F1F77BCFAA64AFCAA6C5D"))
    }
    
    @Test
    fun `isValid returns true for lowercase hex`() {
        assertTrue(MongoIdGenerator.isValid("507f1f77bcfaa64afcaa6c5d"))
    }
    
    @Test
    fun `generate multiple IDs returns correct count`() {
        val ids = MongoIdGenerator.generate(5)
        assertEquals(5, ids.size)
    }
    
    @Test
    fun `generated IDs start with valid timestamp`() {
        val id = MongoIdGenerator.generate()
        val timestampHex = id.substring(0, 8)
        val timestamp = timestampHex.toLong(16)
        
        // Timestamp should be reasonable (after 2020, before 2100)
        assertTrue(timestamp > 1577836800) // Jan 1, 2020
        assertTrue(timestamp < 4102444800) // Jan 1, 2100
    }
}
