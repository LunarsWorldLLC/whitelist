package com.cocahonka.comfywhitelist.config.general

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File


class GeneralConfigTest {

    private lateinit var generalConfig: GeneralConfig
    private lateinit var server: ServerMock
    private lateinit var plugin: Plugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
        generalConfig = GeneralConfig(plugin)

    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `loadConfig sets whitelistEnabled from default config when no user config exists`() {
        // When no user config exists, the default from bundled config.yml (enabled: false) is used
        generalConfig.loadConfig()
        assertFalse(GeneralConfig.whitelistEnabled)
    }

    @Test
    fun `loadConfig sets correct whitelistEnabled when config file exists`() {
        val filePath = "config.yml"
        val enabledKey = "enabled"

        val configFile = File(plugin.dataFolder, filePath)
        configFile.parentFile.mkdirs()

        YamlConfiguration.loadConfiguration(configFile).apply {
            set(enabledKey, false)
            save(configFile)
        }

        generalConfig.loadConfig()
        assertFalse(GeneralConfig.whitelistEnabled)
    }
}
