package com.cocahonka.comfywhitelist.storage

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.cocahonka.comfywhitelist.api.Storage
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class YamlStorageTest : StorageTestBase() {
    private lateinit var storage: Storage
    private lateinit var server: ServerMock
    private lateinit var plugin: Plugin

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

    override fun createStorage(): Storage {
        return storage
    }

    @Test
    fun `add player with expiry`() {
        val storage = createStorage() as YamlStorage
        val playerName = "tempPlayer"
        val expiryTime = System.currentTimeMillis() + 86400000L // +1 day

        storage.addPlayerWithExpiry(playerName, expiryTime)
        assertTrue(storage.isPlayerWhitelisted(playerName))

        val entry = storage.getEntry(playerName)
        assertNotNull(entry)
        assertEquals(expiryTime, entry!!.expiryTimestamp)
        assertFalse(entry.isPermanent())
    }

    @Test
    fun `expired player is not whitelisted`() {
        val storage = createStorage() as YamlStorage
        val playerName = "expiredPlayer"
        val pastTime = System.currentTimeMillis() - 1000L // 1 second ago

        storage.addPlayerWithExpiry(playerName, pastTime)
        assertFalse(storage.isPlayerWhitelisted(playerName))
    }

    @Test
    fun `permanent player is always valid`() {
        val storage = createStorage() as YamlStorage
        val playerName = "permanentPlayer"

        storage.addPlayerWithExpiry(playerName, null)
        assertTrue(storage.isPlayerWhitelisted(playerName))

        val entry = storage.getEntry(playerName)
        assertNotNull(entry)
        assertTrue(entry!!.isPermanent())
        assertTrue(entry.isValid())
    }

    @Test
    fun `getAllWhitelistedPlayers excludes expired entries`() {
        val storage = createStorage() as YamlStorage
        val validPlayer = "validPlayer"
        val expiredPlayer = "expiredPlayer"

        storage.addPlayerWithExpiry(validPlayer, System.currentTimeMillis() + 86400000L)
        storage.addPlayerWithExpiry(expiredPlayer, System.currentTimeMillis() - 1000L)

        val players = storage.allWhitelistedPlayers
        assertTrue(players.contains(validPlayer))
        assertFalse(players.contains(expiredPlayer))
    }

    @Test
    fun `case insensitive player lookup`() {
        val storage = createStorage() as YamlStorage
        storage.addPlayer("TestPlayer")

        assertTrue(storage.isPlayerWhitelisted("testplayer"))
        assertTrue(storage.isPlayerWhitelisted("TESTPLAYER"))
        assertTrue(storage.isPlayerWhitelisted("TestPlayer"))
    }

    @Test
    fun `save and load preserves expiry timestamps`() {
        val storage = createStorage() as YamlStorage
        val playerName = "timedPlayer"
        val expiryTime = System.currentTimeMillis() + 86400000L

        storage.addPlayerWithExpiry(playerName, expiryTime)
        assertTrue(storage.save())

        val storage2 = createStorage() as YamlStorage
        assertTrue(storage2.load())

        val entry = storage2.getEntry(playerName)
        assertNotNull(entry)
        assertEquals(expiryTime, entry!!.expiryTimestamp)
    }

    @Test
    fun `getEntry returns null for non-existent player`() {
        val storage = createStorage() as YamlStorage
        assertNull(storage.getEntry("nonExistent"))
    }
}
