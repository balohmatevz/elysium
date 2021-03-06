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

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


class MoneyWalletCommand(private val plugin: ElysiumEconomyBukkit): CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(CurrencyPrompt())
            .withEscapeSequence("cancel")
            .thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            .addConversationAbandonedListener { event ->
                if (!event.gracefulExit()) {
                    val conversable = event.context.forWhom
                    if (conversable is Player) {
                        conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                    }
                }
            }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("elysium.economy.command.money.wallet")) {
            if (sender is Player) {
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
                if (args.size > 0) {
                    val currency = currencyProvider.getCurrency(args[0])
                    if (currency != null) {
                        showWallet(sender, currency)
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-wallet-currency-invalid-currency")))
                    }
                } else {
                    val currency = currencyProvider.defaultCurrency
                    if (currency != null) {
                        showWallet(sender, currency)
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-money-wallet")))
        }
        return true
    }

    private fun showWallet(bukkitPlayer: Player, currency: ElysiumCurrency) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
        val player = playerProvider.getPlayer(bukkitPlayer)
        val character = characterProvider.getActiveCharacter(player)
        if (character != null) {
            val wallet = plugin.server.createInventory(null, 27, "Wallet [" + currency.name + "]")
            val coin = ItemStack(currency.material)
            val meta = coin.itemMeta
            meta.displayName = currency.nameSingular
            coin.itemMeta = meta
            val coinStack = ItemStack(currency.material, 64)
            val stackMeta = coinStack.itemMeta
            stackMeta.displayName = currency.nameSingular
            coinStack.itemMeta = stackMeta
            val remainder = (economyProvider.getBalance(character, currency) % 64).toInt()
            var i = 0
            while (i < economyProvider.getBalance(character, currency)) {
                val leftover = wallet.addItem(coinStack)
                if (!leftover.isEmpty()) {
                    bukkitPlayer.world.dropItem(bukkitPlayer.location, leftover.values.iterator().next())
                }
                i += 64
            }
            if (remainder != 0) {
                val remove = ItemStack(coin)
                remove.amount = 64 - remainder
                wallet.removeItem(remove)
            }
            bukkitPlayer.openInventory(wallet)
        } else {
            bukkitPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
        }
    }

    private inner class CurrencyPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class).getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("currency", plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class).getCurrency(input))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-prompt")) + "\n" +
                    plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class).currencies
                            .map { currency -> ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-prompt-list-item")).replace("\$currency", currency.name) }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-invalid-currency"))
        }

    }

    private inner class CurrencySetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            showWallet(context.forWhom as Player, context.getSessionData("currency") as ElysiumCurrency)
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-valid"))
        }

    }

}