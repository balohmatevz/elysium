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

package com.seventh_root.elysium.chat.bukkit.command.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible

class ChatChannelLeaveCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(ChatChannelPrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.chat.command.chatchannel.leave")) {
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                if (chatChannelProvider.chatChannels.size > 0) {
                    val player = playerProvider.getPlayer(sender)
                    if (args.size > 0) {
                        val chatChannelBuilder = StringBuilder()
                        for (i in 0..args.size - 1 - 1) {
                            chatChannelBuilder.append(args[i]).append(' ')
                        }
                        chatChannelBuilder.append(args[args.size - 1])
                        val chatChannel = chatChannelProvider.getChatChannel(chatChannelBuilder.toString())
                        if (chatChannel != null) {
                            if (sender.hasPermission("elysium.chat.command.chatchannel.leave." + chatChannel.name)) {
                                chatChannel.removeListener(player)
                                chatChannel.removeSpeaker(player)
                                chatChannelProvider.updateChatChannel(chatChannel)
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-leave-valid")))
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-leave-channel")
                                        .replace("\$channel", chatChannel.name)))
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-leave-invalid-chatchannel")))
                        }
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-chat-channels-available")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-leave")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class ChatChannelPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class).getChatChannel(input) != null && (context.forWhom as Permissible).hasPermission("elysium.")
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
                val player = playerProvider.getPlayer(conversable)
                val channel = chatChannelProvider.getChatChannel(input)!!
                channel.removeListener(player)
                channel.removeSpeaker(player)
                chatChannelProvider.updateChatChannel(channel)
            }
            return ChatChannelLeftPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            if (plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class).getChatChannel(invalidInput) == null) {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-leave-invalid-chatchannel"))
            } else if (!(context.forWhom as Permissible).hasPermission("elysium.chat.command.chatchannel.leave." + invalidInput)) {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-leave-channel")
                        .replace("\$channel", invalidInput))
            }
            return ""
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-leave-prompt"))
        }

    }

    private inner class ChatChannelLeftPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-leave-valid"))
        }

    }

}
