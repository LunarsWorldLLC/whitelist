package com.cocahonka.comfywhitelist.config.general

import com.cocahonka.comfywhitelist.api.WhitelistManager
import com.cocahonka.comfywhitelist.config.base.ConfigManager
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.properties.Delegates

/**
 * A class responsible for managing the general configuration of the plugin,
 * including enabling/disabling the plugin.
 *
 * @param plugin The plugin instance.
 */
class GeneralConfig(private val plugin: Plugin) : WhitelistManager, ConfigManager() {
    companion object {
        var whitelistEnabled: Boolean by Delegates.notNull()
            private set
        var clearCommandEnabled: Boolean by Delegates.notNull()
            private set
        var cleanupIntervalHours: Int by Delegates.notNull()
            private set

        private const val filePath = "config.yml"
        private const val enabledKey = "enabled"
        private const val clearCommandKey = "clear-command"
        private const val cleanupIntervalKey = "cleanup-interval-hours"
    }

    override fun createConfig() {
        configFile = File(plugin.dataFolder, filePath)
        if (!configFile.exists()) {
            plugin.saveResource(filePath, false)
        }
    }

    override fun updateProperties() {
        whitelistEnabled = config.getBoolean(enabledKey, true)
        clearCommandEnabled = config.getBoolean(clearCommandKey, false)
        cleanupIntervalHours = config.getInt(cleanupIntervalKey, 1).coerceAtLeast(1)
    }

    override fun isWhitelistEnabled(): Boolean = whitelistEnabled

    override fun enableWhitelist() {
        whitelistEnabled = true
        config.set(enabledKey, true)
        config.save(configFile)
    }

    override fun disableWhitelist() {
        whitelistEnabled = false
        config.set(enabledKey, false)
        config.save(configFile)
    }
}
