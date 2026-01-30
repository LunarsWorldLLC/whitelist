package com.cocahonka.comfywhitelist.commands.sub

import com.cocahonka.comfywhitelist.LegacyUtils.sendMessage
import com.cocahonka.comfywhitelist.commands.SubCommand
import com.cocahonka.comfywhitelist.config.message.Messages
import com.cocahonka.comfywhitelist.config.message.MessageFormat
import com.cocahonka.comfywhitelist.storage.WhitelistEntry
import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.command.CommandSender

/**
 * Represents the "list" command, which lists all players in the whitelist with their remaining time.
 *
 * @property storage The [YamlStorage] instance to interact with whitelist data.
 */
class ListCommand(private val storage: YamlStorage) : SubCommand {

    companion object {
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }

    override val identifier = "list"
    override val permission = "comfywhitelist.list"
    override val usage = "/comfywl list"

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if(isInvalidUsage(sender) { args.isEmpty() }) return false

        val entries = storage.getAllValidEntries()

        val messageComponent = if (entries.isEmpty()) {
            Messages.emptyWhitelistedPlayersList
        } else {
            val playerDisplayList = entries.map { entry ->
                "${entry.playerName} (${formatDaysRemaining(entry)} remaining)"
            }.toSet()
            val replacementConfig = MessageFormat.ConfigBuilders.playersReplacementConfigBuilder(playerDisplayList)
            Messages.whitelistedPlayersList.replaceText(replacementConfig)
        }
        sender.sendMessage(messageComponent)
        return true
    }

    private fun formatDaysRemaining(entry: WhitelistEntry): String {
        if (entry.isPermanent()) return "permanent"
        val remainingMillis = entry.remainingTimeMillis() ?: return "0 days"
        if (remainingMillis <= 0) return "0 days"
        val days = remainingMillis / MILLIS_PER_DAY
        return if (days == 1L) "1 day" else "$days days"
    }

}