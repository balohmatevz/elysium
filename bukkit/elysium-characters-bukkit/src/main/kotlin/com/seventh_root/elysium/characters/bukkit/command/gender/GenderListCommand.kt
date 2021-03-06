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

package com.seventh_root.elysium.characters.bukkit.command.gender

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGenderProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GenderListCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("elysium.characters.command.gender.list")) {
            val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-list-title")))
            for (gender in genderProvider.genders) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-list-item"))
                        .replace("\$gender", gender.name))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-gender-list")))
        }
        return true
    }

}
