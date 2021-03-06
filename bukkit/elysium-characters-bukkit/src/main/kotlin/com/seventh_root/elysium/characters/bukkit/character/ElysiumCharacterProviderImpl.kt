/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seventh_root.elysium.characters.bukkit.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.database.table.ElysiumCharacterTable
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.SQLException
import java.util.*

class ElysiumCharacterProviderImpl: ElysiumCharacterProvider {

    private val plugin: ElysiumCharactersBukkit
    private val activeCharacterCacheManager: CacheManager
    private val activeCharacterCache: Cache<Int, Int>

    constructor(plugin: ElysiumCharactersBukkit) {
        this.plugin = plugin
        this.activeCharacterCacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        activeCharacterCache = activeCharacterCacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun getCharacter(id: Int): ElysiumCharacter? {
        return plugin.core.database.getTable(ElysiumCharacterTable::class)[id]
    }

    override fun getActiveCharacter(player: ElysiumPlayer): ElysiumCharacter? {
        val playerId = player.id
        if (activeCharacterCache.containsKey(playerId)) {
            return getCharacter(activeCharacterCache.get(playerId) as Int)
        } else {
            try {
                var character: ElysiumCharacter? = null
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT character_id FROM player_character WHERE player_id = ?").use { statement ->
                        statement.setInt(1, player.id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val characterId = resultSet.getInt("character_id")
                            character = getCharacter(characterId)
                            activeCharacterCache.put(playerId, characterId)
                        }
                    }
                }
                return character
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    override fun setActiveCharacter(player: ElysiumPlayer, character: ElysiumCharacter?) {
        val oldCharacter = getActiveCharacter(player)
        if (oldCharacter != null) {
            if (player is ElysiumPlayer) {
                val offlineBukkitPlayer = player.bukkitPlayer
                if (offlineBukkitPlayer != null) {
                    if (offlineBukkitPlayer.isOnline) {
                        val bukkitPlayer = offlineBukkitPlayer.player
                        oldCharacter.inventoryContents = bukkitPlayer.inventory.contents
                        oldCharacter.helmet = bukkitPlayer.inventory.helmet
                        oldCharacter.chestplate = bukkitPlayer.inventory.chestplate
                        oldCharacter.leggings = bukkitPlayer.inventory.leggings
                        oldCharacter.boots = bukkitPlayer.inventory.boots
                        oldCharacter.location = bukkitPlayer.location
                        oldCharacter.health = bukkitPlayer.health
                        oldCharacter.foodLevel = bukkitPlayer.foodLevel
                        updateCharacter(oldCharacter)
                    }
                }
            }
        }
        if (character != null) {
            try {
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "INSERT INTO player_character(player_id, character_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE character_id = VALUES(character_id)").use { statement ->
                        statement.setInt(1, player.id)
                        statement.setInt(2, character.id)
                        statement.executeUpdate()
                    }
                }
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }

            if (player is ElysiumPlayer) {
                val offlineBukkitPlayer = player.bukkitPlayer
                if (offlineBukkitPlayer != null) {
                    if (offlineBukkitPlayer.isOnline) {
                        val bukkitPlayer = offlineBukkitPlayer.player
                        bukkitPlayer.inventory.contents = character.inventoryContents
                        bukkitPlayer.inventory.helmet = character.helmet
                        bukkitPlayer.inventory.chestplate = character.chestplate
                        bukkitPlayer.inventory.leggings = character.leggings
                        bukkitPlayer.inventory.boots = character.boots
                        bukkitPlayer.teleport(character.location)
                        bukkitPlayer.maxHealth = character.maxHealth
                        bukkitPlayer.health = character.health
                        bukkitPlayer.foodLevel = character.foodLevel
                        if (plugin.config.getBoolean("characters.set-player-display-name")) {
                            bukkitPlayer.displayName = character.name
                        }
                    }
                }
            }
            activeCharacterCache.put(player.id, character.id)
        } else if (oldCharacter != null) {
            try {
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "DELETE FROM player_character WHERE player_id = ? AND character_id = ?").use { statement ->
                        statement.setInt(1, player.id)
                        statement.setInt(2, oldCharacter.id)
                        statement.executeUpdate()
                    }
                }
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
            activeCharacterCache.remove(player.id)
        }
    }

    override fun getCharacters(player: ElysiumPlayer): Collection<ElysiumCharacter> {
        try {
            val characters: MutableList<ElysiumCharacter> = ArrayList()
            plugin.core.database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id FROM elysium_character WHERE player_id = ? ORDER BY id").use { statement ->
                    statement.setInt(1, player.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        characters.add(getCharacter(resultSet.getInt("id"))!!)
                    }

                }
            }
            return characters
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return emptyList()
    }

    override fun addCharacter(character: ElysiumCharacter): Int {
        return plugin.core.database.getTable(ElysiumCharacterTable::class).insert(character)
    }

    override fun removeCharacter(character: ElysiumCharacter) {
        val player = character.player
        if (player != null)
            setActiveCharacter(player, null)
        plugin.core.database.getTable(ElysiumCharacterTable::class).delete(character)
    }

    override fun updateCharacter(character: ElysiumCharacter) {
        if (plugin.config.getBoolean("characters.delete-character-on-death") && character.isDead) {
            removeCharacter(character)
        } else {
            plugin.core.database.getTable(ElysiumCharacterTable::class).update(character)
        }
    }

}
