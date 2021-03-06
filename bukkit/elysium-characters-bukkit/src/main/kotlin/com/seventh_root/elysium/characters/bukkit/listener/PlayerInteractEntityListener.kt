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

package com.seventh_root.elysium.characters.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.characters.bukkit.character.field.ElysiumCharacterCardFieldProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot.HAND

class PlayerInteractEntityListener(private val plugin: ElysiumCharactersBukkit): Listener {

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand == HAND) {
            if (event.player.isSneaking || !plugin.config.getBoolean("characters.view-card-requires-sneak")) {
                if (event.rightClicked is Player) {
                    if (event.player.hasPermission("elysium.characters.command.character.card.other")) {
                        val bukkitPlayer = event.rightClicked as Player
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                        val player = playerProvider.getPlayer(bukkitPlayer)
                        val character = characterProvider.getActiveCharacter(player)
                        if (character != null) {
                            for (line in plugin.config.getStringList("messages.character-card")) {
                                var filteredLine = ChatColor.translateAlternateColorCodes('&', line)
                                val characterCardFieldProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterCardFieldProvider::class)
                                characterCardFieldProvider.characterCardFields.forEach { field -> filteredLine = filteredLine.replace("\$${field.name}", field.get(character)) }
                                event.player.sendMessage(filteredLine)
                            }
                        } else {
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character-other")))
                        }
                    } else {
                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-card-other")))
                    }
                }
            }
        }
    }
}
