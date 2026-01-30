package com.cocahonka.comfywhitelist.tasks

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExpiredEntriesCleanupTaskTest {

    private lateinit var server: ServerMock
    private lateinit var plugin: Plugin
    private lateinit var storage: YamlStorage

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
        storage = YamlStorage(plugin.dataFolder)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `removeExpiredEntries removes expired but keeps valid entries`() {
        val validPlayer = "validPlayer"
        val expiredPlayer = "expiredPlayer"
        val permanentPlayer = "permanentPlayer"

        storage.addPlayerWithExpiry(validPlayer, System.currentTimeMillis() + 86400000L)
        storage.addPlayerWithExpiry(expiredPlayer, System.currentTimeMillis() - 1000L)
        storage.addPlayerWithExpiry(permanentPlayer, null)

        storage.removeExpiredEntries()

        assertTrue(storage.isPlayerWhitelisted(validPlayer))
        assertFalse(storage.isPlayerWhitelisted(expiredPlayer))
        assertTrue(storage.isPlayerWhitelisted(permanentPlayer))
        assertNull(storage.getEntry(expiredPlayer))
    }

    @Test
    fun `removeExpiredEntries returns correct count`() {
        storage.addPlayerWithExpiry("expired1", System.currentTimeMillis() - 1000L)
        storage.addPlayerWithExpiry("expired2", System.currentTimeMillis() - 2000L)
        storage.addPlayerWithExpiry("expired3", System.currentTimeMillis() - 3000L)
        storage.addPlayerWithExpiry("valid", System.currentTimeMillis() + 86400000L)

        val removedCount = storage.removeExpiredEntries()

        assertEquals(3, removedCount)
    }

    @Test
    fun `removeExpiredEntries saves to file`() {
        storage.addPlayerWithExpiry("validPlayer", System.currentTimeMillis() + 86400000L)
        storage.addPlayerWithExpiry("expiredPlayer", System.currentTimeMillis() - 1000L)

        storage.removeExpiredEntries()

        // Create new storage instance to verify persistence
        val storage2 = YamlStorage(plugin.dataFolder)
        storage2.load()

        assertTrue(storage2.isPlayerWhitelisted("validPlayer"))
        assertNull(storage2.getEntry("expiredPlayer"))
    }

    @Test
    fun `removeExpiredEntries with no expired entries returns 0`() {
        storage.addPlayerWithExpiry("validPlayer", System.currentTimeMillis() + 86400000L)
        storage.addPlayerWithExpiry("permanentPlayer", null)

        val removedCount = storage.removeExpiredEntries()

        assertEquals(0, removedCount)
        assertTrue(storage.isPlayerWhitelisted("validPlayer"))
        assertTrue(storage.isPlayerWhitelisted("permanentPlayer"))
    }

    @Test
    fun `removeExpiredEntries on empty storage returns 0`() {
        val removedCount = storage.removeExpiredEntries()

        assertEquals(0, removedCount)
    }
}
