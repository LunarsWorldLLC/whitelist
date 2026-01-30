package com.cocahonka.comfywhitelist.commands

import com.cocahonka.comfywhitelist.LegacyUtils.sendMessage
import com.cocahonka.comfywhitelist.commands.sub.AddCommand
import com.cocahonka.comfywhitelist.commands.sub.CheckCommand
import com.cocahonka.comfywhitelist.commands.sub.ListCommand
import com.cocahonka.comfywhitelist.commands.sub.RemoveCommand
import com.cocahonka.comfywhitelist.config.message.Messages
import com.cocahonka.comfywhitelist.config.message.MessageFormat
import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

/**
 * Handles subcommands and their execution for the ComfyWhitelist plugin.
 *
 * @param storage The [YamlStorage] instance to interact with whitelist data.
 */
class CommandHandler(
    storage: YamlStorage,
) : CommandExecutor {

    companion object {
        const val identifier = "comfywhitelist"
        const val usage = "/comfywl <command>"
        val aliases = listOf("comfywl")
    }

    val subCommands: List<SubCommand>

    init {
        subCommands = listOf(
            AddCommand(storage),
            RemoveCommand(storage),
            ListCommand(storage),
            CheckCommand(storage),
        )
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args.isNullOrEmpty()) {
            val replacementConfig = MessageFormat.ConfigBuilders.usageReplacementConfigBuilder(usage)
            val message = Messages.invalidUsage.replaceText(replacementConfig)
            sender.sendMessage(message)
            return false
        }

        val subCommandName = args[0]
        val subCommand = subCommands.find { it.identifier.equals(subCommandName, true) }
        if (subCommand == null) {
            sender.sendMessage(Messages.unknownSubcommand)
            return false
        }

        if (sender !is ConsoleCommandSender && !sender.hasPermission(subCommand.permission)) {
            sender.sendMessage(Messages.noPermission)
            return false
        }

        return subCommand.execute(sender, args.drop(1).toTypedArray())
    }
}