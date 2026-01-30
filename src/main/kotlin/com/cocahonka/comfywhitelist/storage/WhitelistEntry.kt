package com.cocahonka.comfywhitelist.storage

/**
 * Represents a whitelist entry with optional expiry.
 *
 * @property playerName The player's Minecraft username (case-preserved)
 * @property expiryTimestamp Epoch milliseconds when entry expires, null for permanent
 */
data class WhitelistEntry(
    val playerName: String,
    val expiryTimestamp: Long?
) {
    /**
     * Returns true if entry is valid (permanent or not yet expired).
     */
    fun isValid(): Boolean = expiryTimestamp == null || System.currentTimeMillis() < expiryTimestamp

    /**
     * Returns true if this is a permanent (non-expiring) entry.
     */
    fun isPermanent(): Boolean = expiryTimestamp == null

    /**
     * Returns remaining time in milliseconds, or null if permanent.
     * Returns negative value if already expired.
     */
    fun remainingTimeMillis(): Long? = expiryTimestamp?.let { it - System.currentTimeMillis() }
}
