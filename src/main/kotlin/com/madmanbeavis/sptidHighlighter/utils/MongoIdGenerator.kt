package com.madmanbeavis.sptidHighlighter.utils

import java.security.SecureRandom
import java.time.Instant

/**
 * Utility for generating valid MongoDB ObjectIds.
 * 
 * MongoDB ObjectId format (24 hex characters):
 * - 4 bytes: Unix timestamp (seconds since epoch)
 * - 5 bytes: Random value
 * - 3 bytes: Counter (incrementing)
 */
object MongoIdGenerator {
    
    private val random = SecureRandom()
    private var counter = random.nextInt(0xFFFFFF)
    
    /**
     * Generates a valid MongoDB ObjectId as a 24-character hexadecimal string.
     */
    @Synchronized
    fun generate(): String {
        // Get current timestamp (4 bytes)
        val timestamp = Instant.now().epochSecond.toInt()
        
        // Generate random value (5 bytes)
        val randomBytes = ByteArray(5)
        random.nextBytes(randomBytes)
        
        // Increment counter and get 3 bytes
        counter = (counter + 1) and 0xFFFFFF
        
        // Build ObjectId
        val builder = StringBuilder(24)
        
        // Timestamp (4 bytes = 8 hex chars)
        builder.append(String.format("%08x", timestamp))
        
        // Random value (5 bytes = 10 hex chars)
        for (b in randomBytes) {
            builder.append(String.format("%02x", b.toInt() and 0xFF))
        }
        
        // Counter (3 bytes = 6 hex chars)
        builder.append(String.format("%06x", counter))
        
        return builder.toString()
    }
    
    /**
     * Generates multiple MongoDB ObjectIds.
     */
    fun generate(count: Int): List<String> {
        return (1..count).map { generate() }
    }
    
    /**
     * Validates if a string is a valid MongoDB ObjectId format.
     */
    fun isValid(id: String): Boolean {
        if (id.length != 24) return false
        return id.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }
}
