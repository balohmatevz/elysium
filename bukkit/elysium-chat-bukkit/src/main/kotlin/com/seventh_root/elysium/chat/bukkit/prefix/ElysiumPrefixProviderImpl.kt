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

package com.seventh_root.elysium.chat.bukkit.prefix

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumPrefixTable
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer


class ElysiumPrefixProviderImpl(private val plugin: ElysiumChatBukkit): ElysiumPrefixProvider {

    override val prefixes: List<ElysiumPrefix>
        get() = plugin.core.database.getTable(ElysiumPrefixTable::class).getAll()

    override fun addPrefix(prefix: ElysiumPrefix) {
        plugin.core.database.getTable(ElysiumPrefixTable::class).insert(prefix)
    }

    override fun updatePrefix(prefix: ElysiumPrefix) {
        plugin.core.database.getTable(ElysiumPrefixTable::class).update(prefix)
    }

    override fun removePrefix(prefix: ElysiumPrefix) {
        plugin.core.database.getTable(ElysiumPrefixTable::class).delete(prefix)
    }

    override fun getPrefix(name: String): ElysiumPrefix? {
        return plugin.core.database.getTable(ElysiumPrefixTable::class).get(name)
    }

    override fun getPrefix(player: ElysiumPlayer): String {
        val prefixBuilder = StringBuilder()
        for (prefix in plugin.core.database.getTable(ElysiumPrefixTable::class).getAll()) {
            if (player.bukkitPlayer?.isOnline?:false) {
                if (player.bukkitPlayer?.player?.hasPermission("elysium.chat.prefix.${prefix.name}")?:false) {
                    prefixBuilder.append(prefix.prefix).append(' ')
                }
            }
        }
        return prefixBuilder.toString()
    }

}