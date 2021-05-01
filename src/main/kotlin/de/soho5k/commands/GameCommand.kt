package de.soho5k.commands

import de.soho5k.game.GameManager
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.items.setLore
import net.axay.kspigot.runnables.firstAsync
import net.axay.kspigot.runnables.thenSync
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


data class Games(val name: String, val identifier: String, val icon: ItemStack)

var gameCount = 0

val gui = kSpigotGUI(GUIType.SIX_BY_NINE) {
    title = "Choose games"

    page(1) {
        placeholder(Slots.Border, itemStack(Material.WHITE_STAINED_GLASS_PANE) { meta { name = null } })
        val maxGamesCompound = createRectCompound<ItemStack>(

            Slots.RowSixSlotOne,
            Slots.RowSixSlotOne,

            iconGenerator = {
                createMaxGamesChooser()
            }, onClick = { it, _ ->
                it.bukkitEvent.isCancelled = true
                var maxGames = GameManager.maxGames
                val click = it.bukkitEvent.click
                if (click == ClickType.RIGHT) maxGames -= 1
                if (click == ClickType.LEFT) maxGames += 1
                if (maxGames < 1 || maxGames > gameCount) return@createRectCompound
                GameManager.maxGames = maxGames
                it.bukkitEvent.currentItem = createMaxGamesChooser()
            })
        val gamesCompound = createRectCompound<Games>(

            Slots.RowTwoSlotTwo,
            Slots.RowFiveSlotEight,

            iconGenerator = {
                updateIcon(it.icon, it.identifier)
                it.icon
            },
            onClick = { clickEvent, element ->
                clickEvent.bukkitEvent.isCancelled = true
                val clickedIcon = clickEvent.bukkitEvent.currentItem!!
                val enabled = GameManager.isGameEnabled(element.identifier)
                if (enabled) {
                    GameManager.disableGame(element.identifier)
                } else GameManager.enableGame(element.identifier)
                updateIcon(clickedIcon, element.identifier)
            }
        )
        firstAsync {
            GameManager.gameIcons.forEach { _ ->
                gameCount += 1
            }
            GameManager.gameIcons
        }.thenSync {
            gamesCompound.addContent(it)
        }.execute()
        maxGamesCompound.addContent(createMaxGamesChooser())
    }
}

fun updateIcon(icon: ItemStack, identifier: String): ItemStack {
    val meta = icon.itemMeta
    meta?.setLore {
        +" "
        +"${ChatColor.GRAY}This game is"
        if (GameManager.isGameEnabled(identifier)) +"${ChatColor.GREEN}${ChatColor.BOLD}ENABLED" else +"${ChatColor.RED}${ChatColor.BOLD}DISABLED"
    }
    icon.itemMeta = meta
    return icon
}

fun createMaxGamesChooser(): ItemStack {
    println(GameManager.maxGames)

    return itemStack(Material.PAPER) {
        meta {
            setDisplayName("${ChatColor.LIGHT_PURPLE}Choose max games")
            setLore {
                +" "
                +"${ChatColor.GRAY}Max games: ${ChatColor.GREEN}${ChatColor.BOLD}${GameManager.maxGames}"
                +" "
                +"${ChatColor.GRAY}Leftclick ${ChatColor.GREEN}+${ChatColor.GRAY} | Rightclick ${ChatColor.RED}-"
                +" "
                +"${ChatColor.DARK_RED}${ChatColor.BOLD}IMPORTANT${ChatColor.GRAY}:"
                +"${ChatColor.GRAY}The maximum games should be higher than the selected games"
            }
        }
    }
}


class GameCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        sender.openGUI(gui)
        return true
    }
}