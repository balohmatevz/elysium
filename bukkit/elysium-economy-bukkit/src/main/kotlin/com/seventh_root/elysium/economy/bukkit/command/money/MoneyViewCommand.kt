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

package com.seventh_root.elysium.economy.bukkit.command.money

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.Conversable
import org.bukkit.entity.Player


class MoneyViewCommand(private val plugin: ElysiumEconomyBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Conversable) {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
            val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
            val bukkitPlayer = if (args.size > 0) plugin.server.getPlayer(args[0]) else if (sender is Player) sender else null
            if (bukkitPlayer != null) {
                val player = playerProvider.getPlayer(bukkitPlayer)
                val character: ElysiumCharacter?
                if (args.size > 1) {
                    val nameBuilder = StringBuilder()
                    for (i in 1..args.size - 2) {
                        nameBuilder.append(args[i]).append(' ')
                    }
                    nameBuilder.append(args[args.size - 1])
                    val name = nameBuilder.toString()
                    character = characterProvider.getCharacters(player)
                            .filter { character -> character.name.equals(name) }
                            .firstOrNull()
                } else {
                    character = characterProvider.getActiveCharacter(player)
                }
                val finalCharacter = character
                if (finalCharacter != null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-view-valid")))
                    sender.sendMessage(currencyProvider.currencies
                            .map { currency ->
                                ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-view-valid-list-item"))
                                        .replace("\$currency", currency.name)
                                        .replace("\$balance", economyProvider.getBalance(finalCharacter, currency).toString())
                            }
                            .toTypedArray()
                    )
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        }
        return true
    }

}
