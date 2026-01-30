package com.cocahonka.comfywhitelist.config.message

import com.cocahonka.comfywhitelist.ComfyWhitelist
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Object that manages the formatting of plugin messages.
 */
object MessageFormat {

    /**
     * A [MiniMessage] instance for parsing mini messages.
     */
    private val miniMessage = MiniMessage.miniMessage()

    /**
     * A prefix component for all plugin messages.
     */
    val prefixComponent: Component = Component.text(ComfyWhitelist.DISPLAY_NAME + " > ").color(Colors.prefix)

    fun applyStyles(rawMessage: String) =
        miniMessage.deserialize(
            rawMessage,
            Resolvers.off,
            Resolvers.success,
            Resolvers.warning,
            Resolvers.remove,
            Placeholders.prefix
        )

    /**
     * Contains builders for various text replacement configurations.
     */
    object ConfigBuilders {

        /**
         * Returns a [TextReplacementConfig] for replacing the "name" tag with the specified player name.
         *
         * @param playerName The name of the player.
         * @return The [TextReplacementConfig].
         */
        val nameReplacementConfigBuilder = { playerName: String ->
            TextReplacementConfig.builder()
                .match("<name>")
                .replacement(playerName)
                .build()
        }

        /**
         * Returns a [TextReplacementConfig] for replacing the "usage" tag with the specified usage string.
         *
         * @param usage The usage string.
         * @return The [TextReplacementConfig].
         */
        val usageReplacementConfigBuilder = { usage: String ->
            TextReplacementConfig.builder()
                .match("<usage>")
                .replacement(usage)
                .build()
        }

        /**
         * Returns a [TextReplacementConfig] for replacing the "players" tag with the specified set of player names.
         *
         * @param players The set of player names.
         * @return The [TextReplacementConfig].
         */
        val playersReplacementConfigBuilder = { players: Set<String> ->
            TextReplacementConfig.builder()
                .match("<players>")
                .replacement(players.joinToString())
                .build()
        }

        /**
         * Returns a [TextReplacementConfig] for replacing the "days" tag with the specified days string.
         *
         * @param days The days remaining string.
         * @return The [TextReplacementConfig].
         */
        val daysReplacementConfigBuilder = { days: String ->
            TextReplacementConfig.builder()
                .match("<days>")
                .replacement(days)
                .build()
        }
    }

    /**
     * Contains color constants used in message formatting.
     */
    object Colors {
        val prefix = TextColor.fromHexString("#e681a4")!!
        val warning = TextColor.fromHexString("#cc2e23")!!
        val success = TextColor.fromHexString("#5ab317")!!
        val remove = TextColor.fromHexString("#6a66d1")!!
        val off = TextColor.fromHexString("#999c97")!!
    }

    /**
     * Contains tag resolvers for applying color styling to specific tags in a message.
     */
    object Resolvers {
        val warning = TagResolver.resolver("warning", Tag.styling(Colors.warning))
        val success = TagResolver.resolver("success", Tag.styling(Colors.success))
        val remove = TagResolver.resolver("remove", Tag.styling(Colors.remove))
        val off = TagResolver.resolver("off", Tag.styling(Colors.off))
    }

    object Placeholders {
        val prefix = Placeholder.component("comfy", prefixComponent)
    }

}