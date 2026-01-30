package com.cocahonka.comfywhitelist.commands.sub

import com.cocahonka.comfywhitelist.LegacyUtils.sendMessage
import com.cocahonka.comfywhitelist.commands.SubCommand
import com.cocahonka.comfywhitelist.config.message.Messages
import com.cocahonka.comfywhitelist.config.message.MessageFormat
import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.command.CommandSender

/**
 * Represents the "add" command, which adds a player to the whitelist with 31-day expiry.
 * If the player already exists and is valid, extends their expiry by 31 days from now.
 *
 * @property storage The [YamlStorage] instance to interact with whitelist data.
 */
class AddCommand(private val storage: YamlStorage) : SubCommand {

    companion object {
        private const val EXPIRY_DAYS = 31L
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }

    override val identifier = "add"
    override val permission = "comfywhitelist.add"
    override val usage = "/comfywl add <name>"

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if(isInvalidUsage(sender) { args.size == 1 }) return false

        val playerName = args[0]
        if (!playerName.matches(SubCommand.playerNameRegex)){
            sender.sendMessage(Messages.invalidPlayerName)
            return false
        }

        val newExpiry = System.currentTimeMillis() + (EXPIRY_DAYS * MILLIS_PER_DAY)
        val existingEntry = storage.getEntry(playerName)
        val isExtending = existingEntry != null && existingEntry.isValid()

        val replacementConfig = MessageFormat.ConfigBuilders.nameReplacementConfigBuilder(playerName)
        val message = if (isExtending) {
            Messages.playerExpiryExtended.replaceText(replacementConfig)
        } else {
            Messages.playerAdded.replaceText(replacementConfig)
        }
        sender.sendMessage(message)
        return storage.addPlayerWithExpiry(playerName, newExpiry)
    }

}
