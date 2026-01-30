package com.cocahonka.comfywhitelist.config.message

import net.kyori.adventure.text.Component

/**
 * All plugin messages in English.
 * Uses MessageFormat.applyStyles() for MiniMessage custom tag formatting.
 */
object Messages {
    private fun styled(raw: String): Component = MessageFormat.applyStyles(raw)

    // General messages
    val noPermission: Component = styled("<comfy><warning>You do not have permission to use this command.</warning>")
    val inactiveCommand: Component = styled("<comfy>This command is <off>disabled</off> via config.")
    val invalidUsage: Component = styled("<comfy><warning>Invalid command usage.</warning>\nUse: <usage>")
    val unknownSubcommand: Component = styled("<comfy><warning>Unknown subcommand.</warning> Type /comfywl help for a list of commands.")
    val invalidPlayerName: Component = styled("<comfy><warning>Invalid player name.</warning>")
    val pluginReloaded: Component = styled("<comfy>ComfyWhitelist <success>has been successfully reloaded.</success>")

    // Whitelist status messages
    val whitelistEnabled: Component = styled("<comfy>ComfyWhitelist <success>enabled.</success>")
    val whitelistDisabled: Component = styled("<comfy>ComfyWhitelist <off>disabled.</off>")
    val whitelistAlreadyEnabled: Component = styled("<comfy>ComfyWhitelist <success>already enabled.</success>")
    val whitelistAlreadyDisabled: Component = styled("<comfy>ComfyWhitelist <off>already disabled.</off>")

    // Player management messages
    val notWhitelisted: Component = styled("<warning>You are not whitelisted.</warning>")
    val playerAdded: Component = styled("<comfy>Player <success><name></success> has been <success>added</success> to the whitelist.")
    val playerRemoved: Component = styled("<comfy>Player <remove><name></remove> has been <remove>removed</remove> from the whitelist.")
    val nonExistentPlayerName: Component = styled("<comfy>There is <warning>no</warning> player named <warning><name></warning> in the whitelist.")

    // Whitelist display messages
    val whitelistedPlayersList: Component = styled("<comfy>Whitelisted players: <success><players></success>")
    val emptyWhitelistedPlayersList: Component = styled("<comfy>Whitelist is <off>empty.</off>")
    val whitelistCleared: Component = styled("<comfy>All players have been <remove>removed</remove> from the whitelist.")

    // Expiry-related messages
    val playerExpiryExtended: Component = styled("<comfy>Player <success><name></success> whitelist <success>extended</success> by 31 days.")
    val playerCheckResult: Component = styled("<comfy>Player <success><name></success> has <success><days></success> remaining.")
}
