package com.madmanbeavis.sptidHighlighter.services

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.madmanbeavis.sptidHighlighter.models.ItemDetailType
import com.madmanbeavis.sptidHighlighter.services.deserializers.FlexibleBooleanDeserializer
import com.madmanbeavis.sptidHighlighter.services.deserializers.FlexibleDoubleDeserializer
import com.madmanbeavis.sptidHighlighter.services.deserializers.FlexibleIntDeserializer
import com.madmanbeavis.sptidHighlighter.services.deserializers.ItemDetailTypeDeserializer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeserializersTest {
    
    @Test
    fun `ItemDetailTypeDeserializer handles valid uppercase type`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(ItemDetailType::class.java, ItemDetailTypeDeserializer())
            .create()
        
        val json = """"WEAPON""""
        val result = gson.fromJson(json, ItemDetailType::class.java)
        assertEquals(ItemDetailType.WEAPON, result)
    }
    
    @Test
    fun `ItemDetailTypeDeserializer handles lowercase type`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(ItemDetailType::class.java, ItemDetailTypeDeserializer())
            .create()
        
        val json = """"ammo""""
        val result = gson.fromJson(json, ItemDetailType::class.java)
        assertEquals(ItemDetailType.AMMO, result)
    }
    
    @Test
    fun `ItemDetailTypeDeserializer defaults to ITEM for invalid type`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(ItemDetailType::class.java, ItemDetailTypeDeserializer())
            .create()
        
        val json = """"invalid_type""""
        val result = gson.fromJson(json, ItemDetailType::class.java)
        assertEquals(ItemDetailType.ITEM, result)
    }
    
    @Test
    fun `FlexibleBooleanDeserializer handles true values`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Boolean::class.javaObjectType, FlexibleBooleanDeserializer())
            .create()
        
        assertTrue(gson.fromJson("true", Boolean::class.javaObjectType) == true)
        assertTrue(gson.fromJson(""""true"""", Boolean::class.javaObjectType) == true)
        assertTrue(gson.fromJson(""""yes"""", Boolean::class.javaObjectType) == true)
        assertTrue(gson.fromJson(""""1"""", Boolean::class.javaObjectType) == true)
    }
    
    @Test
    fun `FlexibleBooleanDeserializer handles false values`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Boolean::class.javaObjectType, FlexibleBooleanDeserializer())
            .create()
        
        assertTrue(gson.fromJson("false", Boolean::class.javaObjectType) == false)
        assertTrue(gson.fromJson(""""false"""", Boolean::class.javaObjectType) == false)
        assertTrue(gson.fromJson(""""no"""", Boolean::class.javaObjectType) == false)
        assertTrue(gson.fromJson(""""0"""", Boolean::class.javaObjectType) == false)
    }
    
    @Test
    fun `FlexibleBooleanDeserializer handles unknown values`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Boolean::class.javaObjectType, FlexibleBooleanDeserializer())
            .create()
        
        assertNull(gson.fromJson(""""unknown"""", Boolean::class.javaObjectType))
        assertNull(gson.fromJson(""""Unknown"""", Boolean::class.javaObjectType))
        assertNull(gson.fromJson("""""", Boolean::class.javaObjectType))
        assertNull(gson.fromJson("null", Boolean::class.javaObjectType))
    }
    
    @Test
    fun `FlexibleIntDeserializer handles numeric values`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Int::class.javaObjectType, FlexibleIntDeserializer())
            .create()
        
        assertEquals(42, gson.fromJson("42", Int::class.javaObjectType))
        assertEquals(42, gson.fromJson(""""42"""", Int::class.javaObjectType))
    }
    
    @Test
    fun `FlexibleIntDeserializer handles unknown values`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Int::class.javaObjectType, FlexibleIntDeserializer())
            .create()
        
        assertNull(gson.fromJson(""""unknown"""", Int::class.javaObjectType))
        assertNull(gson.fromJson("""""", Int::class.javaObjectType))
        assertNull(gson.fromJson("null", Int::class.javaObjectType))
    }
    
    @Test
    fun `FlexibleIntDeserializer handles invalid strings`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Int::class.javaObjectType, FlexibleIntDeserializer())
            .create()
        
        assertNull(gson.fromJson(""""not_a_number"""", Int::class.javaObjectType))
    }
    
    @Test
    fun `FlexibleDoubleDeserializer handles numeric values`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Double::class.javaObjectType, FlexibleDoubleDeserializer())
            .create()
        
        assertEquals(3.14, gson.fromJson("3.14", Double::class.javaObjectType))
        assertEquals(3.14, gson.fromJson(""""3.14"""", Double::class.javaObjectType))
    }
    
    @Test
    fun `FlexibleDoubleDeserializer handles unknown values`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Double::class.javaObjectType, FlexibleDoubleDeserializer())
            .create()
        
        assertNull(gson.fromJson(""""unknown"""", Double::class.javaObjectType))
        assertNull(gson.fromJson("""""", Double::class.javaObjectType))
        assertNull(gson.fromJson("null", Double::class.javaObjectType))
    }
}
