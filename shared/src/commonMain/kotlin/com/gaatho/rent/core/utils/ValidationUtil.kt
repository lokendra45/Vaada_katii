package com.gaatho.rent.core.utils

/**
 * Common validation utility for forms and user inputs in Rent Manager Nepal.
 */
object ValidationUtil {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    
    // Nepal mobile numbers: 10 digits starting with 98, 97, or 96 (NTC, Ncell, SmartCell)
    private val NEPALI_PHONE_REGEX = Regex("^(98|97|96)\\d{8}$")

    /**
     * Checks if the given email string is a valid email format.
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    }

    /**
     * Checks if the given phone string is a valid 10-digit Nepalese mobile number (`98xxxx` / `97xxxx` / `96xxxx`).
     * Handles optional country code (`+977` or `977`).
     */
    fun isValidNepaliPhone(phone: String): Boolean {
        var cleaned = phone.filter { it.isDigit() }
        if (cleaned.length == 13 && cleaned.startsWith("977")) {
            cleaned = cleaned.substring(3)
        }
        return NEPALI_PHONE_REGEX.matches(cleaned)
    }

    /**
     * Checks if a property or tenant name is valid (`2..60` characters).
     */
    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.length in 2..60
    }

    /**
     * Checks if a rent or deposit amount is a valid positive financial figure.
     */
    fun isValidAmount(amount: Double?): Boolean {
        return amount != null && amount > 0 && !amount.isNaN() && !amount.isInfinite()
    }
}
