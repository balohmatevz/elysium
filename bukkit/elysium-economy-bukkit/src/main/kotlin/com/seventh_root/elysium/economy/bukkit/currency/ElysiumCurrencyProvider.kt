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

package com.seventh_root.elysium.economy.bukkit.currency

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.database.table.ElysiumCurrencyTable

class ElysiumCurrencyProvider(private val plugin: ElysiumEconomyBukkit): ServiceProvider {

    fun getCurrency(id: Int): ElysiumCurrency? {
        return plugin.core.database.getTable(ElysiumCurrency::class.java)?.get(id)
    }

    fun getCurrency(name: String): ElysiumCurrency? {
        return (plugin.core.database.getTable(ElysiumCurrency::class.java) as ElysiumCurrencyTable).get(name)
    }

    val currencies: Collection<ElysiumCurrency>
        get() = (plugin.core.database.getTable(ElysiumCurrency::class.java) as ElysiumCurrencyTable).getAll()

    fun addCurrency(currency: ElysiumCurrency) {
        plugin.core.database.getTable(ElysiumCurrency::class.java)?.insert(currency)
    }

    fun removeCurrency(currency: ElysiumCurrency) {
        plugin.core.database.getTable(ElysiumCurrency::class.java)?.delete(currency)
    }

    val defaultCurrency: ElysiumCurrency?
        get() {
            val currencyName = plugin.config.getString("currency.default")
            if (currencyName != null)
                return getCurrency(currencyName)
            else
                return null
        }


}