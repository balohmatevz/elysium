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

package com.seventh_root.elysium.chat.bukkit.command.snoop

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.snooper.ElysiumSnooperProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class SnoopCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val snooperProvider = plugin.core.serviceManager.getServiceProvider(ElysiumSnooperProvider::class)
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        if (sender is Player) {
            val player = playerProvider.getPlayer(sender)
            if (args.size > 0) {
                if (args[0].equals("on", ignoreCase = true)) {
                    if (sender.hasPermission("elysium.chat.command.snoop.on")) {
                        if (!snooperProvider.snoopers.contains(player)) {
                            snooperProvider.addSnooper(player)
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.snoop-enabled")))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.snoop-already-enabled")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-snoop-on")))
                    }
                } else if (args[0].equals("off", ignoreCase = true)) {
                    if (sender.hasPermission("elysium.chat.command.snoop.off")) {
                        if (snooperProvider.snoopers.contains(player)) {
                            snooperProvider.removeSnooper(player)
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.snoop-disabled")))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.snoop-already-disabled")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("no-permission-snoop-off")))
                    }
                } else if (args[0].equals("check", ignoreCase = true)) {
                    if (sender.hasPermission("elysium.chat.command.snoop.check")) {
                        if (snooperProvider.snoopers.contains(player)) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.snoop-check-on")))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.snoop-check-off")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-snoop-check")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.snoop-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages-snoop-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }
}