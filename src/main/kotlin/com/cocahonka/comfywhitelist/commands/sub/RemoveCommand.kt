package com.cocahonka.comfywhitelist.commands.sub

import com.cocahonka.comfywhitelist.LegacyUtils.sendMessage
import com.cocahonka.comfywhitelist.commands.SubCommand
import com.cocahonka.comfywhitelist.config.message.Messages
import com.cocahonka.comfywhitelist.config.message.MessageFormat
import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.command.CommandSender

/**
 * Represents the "remove" command, which removes a player from the whitelist.
 *
 * @property storage The [YamlStorage] instance to interact with whitelist data.
 */
class RemoveCommand(private val storage: YamlStorage) : SubCommand {

    override val identifier = "remove"
    override val permission = "comfywhitelist.remove"
    override val usage = "/comfywl remove <name>"

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if(isInvalidUsage(sender) { args.size == 1 }) return false

        val playerName = args[0]
        if (!playerName.matches(SubCommand.playerNameRegex)){
            sender.sendMessage(Messages.invalidPlayerName)
            return false
        }

        if(!storage.isPlayerWhitelisted(playerName)) {
            val replacementConfig = MessageFormat.ConfigBuilders.nameReplacementConfigBuilder(playerName)
            val message = Messages.nonExistentPlayerName.replaceText(replacementConfig)
            sender.sendMessage(message)
            return false
        }

        val replacementConfig = MessageFormat.ConfigBuilders.nameReplacementConfigBuilder(playerName)
        val message = Messages.playerRemoved.replaceText(replacementConfig)
        sender.sendMessage(message)
        return storage.removePlayer(playerName)
    }

}