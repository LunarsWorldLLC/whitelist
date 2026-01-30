package com.cocahonka.comfywhitelist.tasks

import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class ExpiredEntriesCleanupTask(
    private val storage: YamlStorage,
    private val plugin: JavaPlugin
) : BukkitRunnable() {

    override fun run() {
        val removedCount = storage.removeExpiredEntries()
        if (removedCount > 0) {
            plugin.logger.info("Removed $removedCount expired whitelist entries")
        }
    }
}
