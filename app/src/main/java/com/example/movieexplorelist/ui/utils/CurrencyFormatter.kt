package com.example.movieexplorelist.ui.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Utility object for formatting prices in Indian Rupees
 */
@Suppress("UNUSED_PARAMETER", "UNUSED", "UNUSED_VARIABLE")
object CurrencyFormatter {
    
    /**
     * Format price in Indian Rupees with proper symbol
     * @param price Price in USD (will be converted to INR)
     * @return Formatted string like "₹299.99"
     */
    fun formatPriceINR(price: Double): String {
        // Convert USD to INR (1 USD ≈ 83 INR - approximate)
        val priceInINR = price * 83.0
        val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(priceInINR)
    }
}

