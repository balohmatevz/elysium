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

package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer


interface ElysiumChatChannelProvider: ServiceProvider {
    val chatChannels: Collection<ElysiumChatChannel>
    fun getChatChannel(id: Int): ElysiumChatChannel?
    fun getChatChannel(name: String): ElysiumChatChannel?
    fun addChatChannel(chatChannel: ElysiumChatChannel)
    fun removeChatChannel(chatChannel: ElysiumChatChannel)
    fun updateChatChannel(chatChannel: ElysiumChatChannel)
    fun getPlayerChannel(player: ElysiumPlayer): ElysiumChatChannel?
    fun setPlayerChannel(player: ElysiumPlayer, channel: ElysiumChatChannel)
    fun getChatChannelFromIRCChannel(ircChannel: String): ElysiumChatChannel?
}