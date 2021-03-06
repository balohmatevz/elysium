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

package com.seventh_root.elysium.trade.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import com.seventh_root.elysium.trade.bukkit.ElysiumTradeBukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


class PlayerInteractListener(private val plugin: ElysiumTradeBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.clickedBlock != null) {
            if (event.clickedBlock.state != null) {
                if (event.clickedBlock.state is Sign) {
                    val sign = event.clickedBlock.state as Sign
                    if (sign.getLine(0).equals(GREEN.toString() + "[trader]")) {
                        val material = Material.matchMaterial(sign.getLine(1))?:Material.matchMaterial(sign.getLine(1).replace(Regex("\\d+\\s+"), ""))
                        val amount = if (sign.getLine(1).matches(Regex("\\d+\\s+.*"))) sign.getLine(1).split(Regex("\\s+"))[0].toInt() else 1
                        var buyPrice = sign.getLine(2).split(" | ")[0].toInt()
                        var sellPrice = sign.getLine(2).split(" | ")[1].toInt()
                        var actualPrice = arrayOf(buyPrice, sellPrice).average()
                        val currency = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class).getCurrency(sign.getLine(3))
                        if (currency != null) {
                            if (event.action === RIGHT_CLICK_BLOCK) {
                                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                                val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
                                val player = playerProvider.getPlayer(event.player)
                                val character = characterProvider.getActiveCharacter(player)
                                if (character != null) {
                                    if (event.player.hasPermission("elysium.trade.sign.trader.buy")) {
                                        if (economyProvider.getBalance(character, currency) >= buyPrice) {
                                            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - buyPrice)
                                            event.player.inventory.addItem(ItemStack(material, amount))
                                            event.player.sendMessage(
                                                    ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-buy"))
                                                            .replace("\$quantity", amount.toString())
                                                            .replace("\$material", material.toString().toLowerCase().replace('_', ' '))
                                                            .replace("\$price", buyPrice.toString() + if (sellPrice === 1) currency.nameSingular else currency.namePlural)
                                            )
                                            actualPrice += plugin.config.getDouble("traders.price-change")
                                            actualPrice = Math.max(Math.min(actualPrice, plugin.config.getDouble("traders.maximum-price")), plugin.config.getDouble("traders.minimum-price")).toDouble()
                                            buyPrice = (actualPrice + ((plugin.config.getDouble("traders.trade-fee-percentage") / 100.0) * actualPrice)).toInt()
                                            sellPrice = (actualPrice - ((plugin.config.getDouble("traders.trade-fee-percentage") / 100.0) * actualPrice)).toInt()
                                            sign.setLine(2, buyPrice.toString() + " | " + sellPrice.toString())
                                            sign.update()
                                        } else {
                                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-buy-insufficient-funds")))
                                        }
                                    } else {
                                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-trader-buy")))
                                    }
                                } else {
                                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                                }
                            } else if (event.action == LEFT_CLICK_BLOCK) {
                                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                                val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
                                val player = playerProvider.getPlayer(event.player)
                                val character = characterProvider.getActiveCharacter(player)
                                if (character != null) {
                                    if (event.player.hasPermission("elysium.trade.sign.trader.sell")) {
                                        if (event.player.inventory.containsAtLeast(ItemStack(material), amount)) {
                                            if (economyProvider.getBalance(character, currency) + sellPrice <= 1728) {
                                                economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) + sellPrice)
                                                event.player.inventory.removeItem(ItemStack(material, amount))
                                                event.player.sendMessage(
                                                        ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-sell"))
                                                                .replace("\$quantity", amount.toString())
                                                                .replace("\$material", material.toString().toLowerCase().replace('_', ' '))
                                                                .replace("\$price", sellPrice.toString() + if (sellPrice === 1) currency.nameSingular else currency.namePlural)
                                                )
                                                actualPrice -= plugin.config.getDouble("traders.price-change")
                                                actualPrice = Math.max(Math.min(actualPrice, plugin.config.getDouble("traders.maximum-price")), plugin.config.getDouble("traders.minimum-price")).toDouble()
                                                buyPrice = (actualPrice + ((plugin.config.getDouble("traders.trade-fee-percentage") / 100.0) * actualPrice)).toInt()
                                                sellPrice = (actualPrice - ((plugin.config.getDouble("traders.trade-fee-percentage") / 100.0) * actualPrice)).toInt()
                                                sign.setLine(2, buyPrice.toString() + " | " + sellPrice.toString())
                                                sign.update()
                                            } else {
                                                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-sell-insufficient-wallet-space")))
                                            }
                                        } else {
                                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-sell-insufficient-items")))
                                        }
                                    } else {
                                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-trader-sell")))
                                    }
                                } else {
                                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}