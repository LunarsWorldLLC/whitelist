package com.cocahonka.comfywhitelist.commands.sub

import be.seeseemelk.mockbukkit.command.MessageTarget
import com.cocahonka.comfywhitelist.commands.CommandTestBase
import com.cocahonka.comfywhitelist.config.message.Messages
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StatusCommandTest : CommandTestBase() {

    private lateinit var statusCommand: StatusCommand
    private lateinit var label: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        statusCommand = StatusCommand()
        label = statusCommand.identifier

        playerWithPermission.addAttachment(plugin, statusCommand.permission, true)
    }

    private fun assertOnlyEnabledMessage(sender: MessageTarget) {
        assertEquals(
            sender.nextMessage(),
            legacySection.serialize(Messages.whitelistEnabled)
        )
        sender.assertNoMoreSaid()
    }

    private fun assertOnlyDisabledMessage(sender: MessageTarget) {
        assertEquals(
            sender.nextMessage(),
            legacySection.serialize(Messages.whitelistDisabled)
        )
        sender.assertNoMoreSaid()
    }

    @Test
    fun `when console is sender and whitelist enabled`() {
        generalConfig.enableWhitelist()

        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(statusCommand.identifier)
        )

        assertTrue(result)
        assertOnlyEnabledMessage(console)
    }

    @Test
    fun `when console is sender and whitelist disabled`() {
        generalConfig.disableWhitelist()

        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(statusCommand.identifier)
        )

        assertTrue(result)
        assertOnlyDisabledMessage(console)
    }

    @Test
    fun `when player is sender without permission`() {
        val result = handler.onCommand(
            sender = playerWithoutPermission,
            command = command,
            label = label,
            args = arrayOf(statusCommand.identifier)
        )

        assertFalse(result)
        assertOnlyNoPermissionMessage(playerWithoutPermission)
    }

    @Test
    fun `when player is sender with permission`() {
        generalConfig.enableWhitelist()

        val result = handler.onCommand(
            sender = playerWithPermission,
            command = command,
            label = label,
            args = arrayOf(statusCommand.identifier)
        )

        assertTrue(result)
        assertOnlyEnabledMessage(playerWithPermission)
    }

    @Test
    fun `when to many arguments`() {
        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(statusCommand.identifier, statusCommand.identifier)
        )

        assertFalse(result)
        assertOnlyInvalidUsageMessage(console, statusCommand.usage)
    }

}
