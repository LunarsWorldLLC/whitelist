package com.cocahonka.comfywhitelist.storage

import com.cocahonka.comfywhitelist.api.Storage
import org.bukkit.Bukkit.getLogger
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * YamlStorage is an implementation of the Storage interface that uses a YAML file for storing
 * the whitelist data. It provides methods for adding, removing, and checking players in the whitelist,
 * as well as loading and saving the data to the YAML file.
 *
 * Entries are stored with optional expiry timestamps. The YAML format is section-based:
 * ```
 * players:
 *   PlayerName:
 *     expiry: <timestamp|null>
 * ```
 *
 * @param dataFolder The folder where the YAML storage file should be located. It must be a directory.
 * @throws IllegalArgumentException If the provided dataFolder is not a directory.
 */
class YamlStorage(dataFolder: File) : Storage {
    private val whitelistedPlayers: MutableMap<String, WhitelistEntry> = mutableMapOf()
    private val storageFile: File
    private val config: YamlConfiguration

    init {
        require(dataFolder.isDirectory) { "provided dataFolder ($dataFolder) is not directory!" }
        storageFile = File(dataFolder, FILE_NAME)
        if(!storageFile.exists()) {
            createFile()
        }
        config = YamlConfiguration.loadConfiguration(storageFile)
    }

    companion object {
        private const val PLAYERS_KEY = "players"
        private const val EXPIRY_KEY = "expiry"
        private const val FILE_NAME = "whitelist.yml"
    }

    override fun addPlayer(username: String): Boolean {
        val normalized = username.lowercase()
        if (whitelistedPlayers.containsKey(normalized)) {
            return false // Already exists
        }
        whitelistedPlayers[normalized] = WhitelistEntry(username, null)
        return save()
    }

    /**
     * Adds a player to the whitelist with an optional expiry timestamp.
     * If the player already exists, their entry is replaced with the new expiry.
     *
     * @param username The player's username (case preserved for display)
     * @param expiryTimestamp Epoch milliseconds when entry expires, null for permanent
     * @return true if saved successfully
     */
    fun addPlayerWithExpiry(username: String, expiryTimestamp: Long?): Boolean {
        val normalized = username.lowercase()
        whitelistedPlayers[normalized] = WhitelistEntry(username, expiryTimestamp)
        return save()
    }

    /**
     * Gets the whitelist entry for a player.
     *
     * @param username The player's username (case-insensitive)
     * @return The WhitelistEntry or null if player is not whitelisted
     */
    fun getEntry(username: String): WhitelistEntry? {
        return whitelistedPlayers[username.lowercase()]
    }

    override fun removePlayer(username: String): Boolean {
        return (whitelistedPlayers.remove(username.lowercase()) != null).also { if (it) save() }
    }

    override fun clear(): Boolean {
        whitelistedPlayers.clear()
        return save()
    }

    override fun isPlayerWhitelisted(username: String): Boolean {
        val entry = whitelistedPlayers[username.lowercase()]
        return entry?.isValid() == true
    }

    override fun getAllWhitelistedPlayers(): Set<String> {
        return whitelistedPlayers.values
            .filter { it.isValid() }
            .map { it.playerName }
            .toSet()
    }

    override fun load(): Boolean {
        return try {
            if (!storageFile.exists()) {
                storageFile.parentFile.mkdirs()
                storageFile.createNewFile()
            }
            config.load(storageFile)
            whitelistedPlayers.clear()

            // Try new format first (section-based)
            val section = config.getConfigurationSection(PLAYERS_KEY)
            if (section != null) {
                section.getKeys(false).forEach { playerName ->
                    val expiry = section.get("$playerName.$EXPIRY_KEY") as? Long
                    whitelistedPlayers[playerName.lowercase()] = WhitelistEntry(playerName, expiry)
                }
            } else {
                // Fallback: old format (list of strings) - migrate to permanent entries
                config.getStringList(PLAYERS_KEY).forEach { playerName ->
                    whitelistedPlayers[playerName.lowercase()] = WhitelistEntry(playerName, null)
                }
            }
            true
        } catch (e: Exception) {
            getLogger().warning(e.stackTraceToString())
            false
        }
    }

    override fun save(): Boolean {
        return try {
            config.set(PLAYERS_KEY, null) // Clear section first
            whitelistedPlayers.values.forEach { entry ->
                // Create the player section explicitly, then set expiry
                // This ensures the player key exists even when expiry is null
                config.createSection("$PLAYERS_KEY.${entry.playerName}")
                if (entry.expiryTimestamp != null) {
                    config.set("$PLAYERS_KEY.${entry.playerName}.$EXPIRY_KEY", entry.expiryTimestamp)
                }
            }
            config.save(storageFile)
            true
        } catch (e: Exception) {
            getLogger().warning(e.stackTraceToString())
            false
        }
    }

    private fun createFile() {
        storageFile.parentFile.mkdirs()
        storageFile.createNewFile()
    }

}
