package com.cocahonka.comfywhitelist.commands.sub

import be.seeseemelk.mockbukkit.command.MessageTarget
import com.cocahonka.comfywhitelist.commands.CommandTestBase
import com.cocahonka.comfywhitelist.config.message.MessageFormat
import com.cocahonka.comfywhitelist.config.message.Messages
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ListCommandTest : CommandTestBase() {

    private lateinit var listCommand: ListCommand
    private lateinit var label: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        listCommand = ListCommand(storage)
        label = listCommand.identifier

        playerWithPermission.addAttachment(plugin, listCommand.permission, true)
    }

    private fun assertOnlyWhitelistedPlayersListMessage(sender: MessageTarget) {
        // ListCommand now formats entries as "PlayerName (permanent remaining)" or "PlayerName (X days remaining)"
        val entries = storage.getAllValidEntries()
        val playerDisplayList = entries.map { entry ->
            val daysRemaining = if (entry.isPermanent()) "permanent" else {
                val remaining = entry.remainingTimeMillis() ?: 0L
                if (remaining <= 0) "0 days" else {
                    val days = remaining / (24L * 60L * 60L * 1000L)
                    if (days == 1L) "1 day" else "$days days"
                }
            }
            "${entry.playerName} ($daysRemaining remaining)"
        }.toSet()
        val replacementConfig = MessageFormat.ConfigBuilders.playersReplacementConfigBuilder(playerDisplayList)
        val message = Messages.whitelistedPlayersList.replaceText(replacementConfig)
        assertEquals(
            sender.nextMessage(),
            legacySection.serialize(message)
        )
        sender.assertNoMoreSaid()
    }

    private fun assertOnlyEmptyWhitelistedPlayersListMessage(sender: MessageTarget) {
        assertEquals(
            sender.nextMessage(),
            legacySection.serialize(Messages.emptyWhitelistedPlayersList)
        )
        sender.assertNoMoreSaid()
    }


    @Test
    fun `when console is sender and storage not empty`() {
        val addedPlayer = server.addPlayer()
        val addedPlayerSecond = server.addPlayer()

        storage.addPlayer(addedPlayer.name)
        storage.addPlayer(addedPlayerSecond.name)

        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(listCommand.identifier),
        )

        assertTrue(result)
        assertOnlyWhitelistedPlayersListMessage(console)
    }

    @Test
    fun `when console is sender and storage empty`() {
        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(listCommand.identifier),
        )

        assertTrue(result)
        assertOnlyEmptyWhitelistedPlayersListMessage(console)
    }

    @Test
    fun `when player is sender without permission`() {
        val result = handler.onCommand(
            sender = playerWithoutPermission,
            command = command,
            label = label,
            args = arrayOf(listCommand.identifier),
        )

        assertFalse(result)
        assertOnlyNoPermissionMessage(playerWithoutPermission)
    }

    @Test
    fun `when player is sender with permission`() {
        val result = handler.onCommand(
            sender = playerWithPermission,
            command = command,
            label = label,
            args = arrayOf(listCommand.identifier),
        )

        assertTrue(result)
        assertOnlyEmptyWhitelistedPlayersListMessage(playerWithPermission)
    }

    @Test
    fun `when to many arguments`() {
        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(listCommand.identifier, listCommand.identifier),
        )

        assertFalse(result)
        assertOnlyInvalidUsageMessage(console, listCommand.usage)
    }

}
