package com.cocahonka.comfywhitelist.commands.sub

import com.cocahonka.comfywhitelist.LegacyUtils.sendMessage
import com.cocahonka.comfywhitelist.commands.SubCommand
import com.cocahonka.comfywhitelist.config.general.GeneralConfig
import com.cocahonka.comfywhitelist.config.message.Messages
import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.command.CommandSender

/**
 * Represents the "clear" command, which clears the whitelist.
 *
 * @property storage The [YamlStorage] instance to interact with whitelist data.
 */
class ClearCommand(private val storage: YamlStorage) : SubCommand {

    override val identifier = "clear"
    override val permission = "comfywhitelist.clear"
    override val usage = "/comfywl clear"

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if(isInvalidUsage(sender) { args.isEmpty() }) return false

        if(!GeneralConfig.clearCommandEnabled) {
            sender.sendMessage(Messages.inactiveCommand)
            return false
        }

        sender.sendMessage(Messages.whitelistCleared)

        return storage.clear()
    }

}