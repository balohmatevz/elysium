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

import org.bukkit.Material


class ElysiumCurrencyImpl(
        override var id: Int = 0,
        override var name: String,
        override var nameSingular: String,
        override var namePlural: String,
        override var rate: Double,
        override var defaultAmount: Int,
        override var material: Material
): ElysiumCurrency