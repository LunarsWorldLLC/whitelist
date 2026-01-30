package com.cocahonka.comfywhitelist.commands.sub

import be.seeseemelk.mockbukkit.command.MessageTarget
import com.cocahonka.comfywhitelist.commands.CommandTestBase
import com.cocahonka.comfywhitelist.config.base.ConfigManager
import com.cocahonka.comfywhitelist.config.general.GeneralConfig
import com.cocahonka.comfywhitelist.config.message.Messages
import org.bukkit.configuration.file.FileConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.properties.Delegates

class ReloadCommandTest : CommandTestBase() {

    private lateinit var reloadCommand: ReloadCommand
    private lateinit var label: String

    private var newEnabled by Delegates.notNull<Boolean>()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        reloadCommand = ReloadCommand(plugin)
        label = reloadCommand.identifier

        newEnabled = !GeneralConfig.whitelistEnabled

        val generalFileConfiguration = ConfigManager::class.java
            .getDeclaredField("config")
            .apply { isAccessible = true }
            .get(generalConfig) as FileConfiguration

        val generalConfigFile = ConfigManager::class.java
            .getDeclaredField("configFile")
            .apply { isAccessible = true }
            .get(generalConfig) as File

        val enabledKey = GeneralConfig::class.java
            .getDeclaredField("enabledKey")
            .apply { isAccessible = true }
            .get(GeneralConfig.Companion::class.java) as String

        generalFileConfiguration.set(enabledKey, newEnabled)
        generalFileConfiguration.save(generalConfigFile)

        playerWithPermission.addAttachment(plugin, reloadCommand.permission, true)
    }

    private fun assertOnlyPluginReloadedMessage(sender: MessageTarget) {
        assertEquals(
            sender.nextMessage(),
            legacySection.serialize(Messages.pluginReloaded)
        )
        sender.assertNoMoreSaid()
    }

    private fun assertEnabledUpdatedTrue() =
        assertEquals(GeneralConfig.whitelistEnabled, newEnabled)

    private fun assertEnabledUpdatedFalse() =
        assertNotEquals(GeneralConfig.whitelistEnabled, newEnabled)

    @Test
    fun `when console is sender`() {
        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(reloadCommand.identifier)
        )

        assertTrue(result)
        assertEnabledUpdatedTrue()
        assertOnlyPluginReloadedMessage(console)
    }

    @Test
    fun `when player is sender without permission`() {
        val result = handler.onCommand(
            sender = playerWithoutPermission,
            command = command,
            label = label,
            args = arrayOf(reloadCommand.identifier)
        )

        assertFalse(result)
        assertEnabledUpdatedFalse()
        assertOnlyNoPermissionMessage(playerWithoutPermission)
    }

    @Test
    fun `when player is sender with permission`() {
        val result = handler.onCommand(
            sender = playerWithPermission,
            command = command,
            label = label,
            args = arrayOf(reloadCommand.identifier)
        )

        assertTrue(result)
        assertEnabledUpdatedTrue()
        assertOnlyPluginReloadedMessage(playerWithPermission)
    }

    @Test
    fun `when to many arguments`() {
        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(reloadCommand.identifier, reloadCommand.identifier)
        )

        assertFalse(result)
        assertEnabledUpdatedFalse()
        assertOnlyInvalidUsageMessage(console, reloadCommand.usage)
    }

}
