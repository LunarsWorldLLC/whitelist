package com.cocahonka.comfywhitelist.commands.sub

import com.cocahonka.comfywhitelist.LegacyUtils.sendMessage
import com.cocahonka.comfywhitelist.commands.SubCommand
import com.cocahonka.comfywhitelist.config.message.Messages
import com.cocahonka.comfywhitelist.config.message.MessageFormat
import com.cocahonka.comfywhitelist.storage.WhitelistEntry
import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.command.CommandSender

/**
 * Represents the "check" command, which checks a player's whitelist status and remaining time.
 *
 * @property storage The [YamlStorage] instance to interact with whitelist data.
 */
class CheckCommand(private val storage: YamlStorage) : SubCommand {

    companion object {
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }

    override val identifier = "check"
    override val permission = "comfywhitelist.check"
    override val usage = "/comfywl check <name>"

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if(isInvalidUsage(sender) { args.size == 1 }) return false

        val playerName = args[0]
        if (!playerName.matches(SubCommand.playerNameRegex)){
            sender.sendMessage(Messages.invalidPlayerName)
            return false
        }

        val entry = storage.getEntry(playerName)
        val nameReplacementConfig = MessageFormat.ConfigBuilders.nameReplacementConfigBuilder(playerName)

        if (entry == null || !entry.isValid()) {
            val message = Messages.nonExistentPlayerName.replaceText(nameReplacementConfig)
            sender.sendMessage(message)
            return false
        }

        val daysRemaining = formatDaysRemaining(entry)
        val daysReplacementConfig = MessageFormat.ConfigBuilders.daysReplacementConfigBuilder(daysRemaining)
        val message = Messages.playerCheckResult
            .replaceText(nameReplacementConfig)
            .replaceText(daysReplacementConfig)
        sender.sendMessage(message)
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
