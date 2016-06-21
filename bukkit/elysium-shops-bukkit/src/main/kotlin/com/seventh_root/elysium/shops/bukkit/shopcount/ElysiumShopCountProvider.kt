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

package com.seventh_root.elysium.shops.bukkit.shopcount

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import com.seventh_root.elysium.shops.bukkit.database.table.ElysiumShopCountTable


class ElysiumShopCountProvider(val plugin: ElysiumShopsBukkit): ServiceProvider {

    fun getShopCount(character: ElysiumCharacter): Int {
        return (plugin.core.database.getTable(ElysiumShopCount::class.java) as? ElysiumShopCountTable)?.get(character)?.count?:0
    }

    fun setShopCount(character: ElysiumCharacter, amount: Int) {
        val shopCount = (plugin.core.database.getTable(ElysiumShopCount::class.java) as? ElysiumShopCountTable)?.get(character)?:
                ElysiumShopCount(0, character, 0)
        shopCount.count = amount
        plugin.core.database.getTable(ElysiumShopCount::class.java)?.update(shopCount)
    }

}