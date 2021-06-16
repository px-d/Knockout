package de.soho5k.commands

import de.soho5k.game.GameManager
import de.soho5k.utils.extensions.sendLocalizedMessage
import de.soho5k.utils.extensions.tc
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
import util.ItemBuilder


data class Games(
    val name: String,
    val identifier: String,
    val icon: ItemStack,
    val description: ArrayList<String> = arrayListOf(""),
    val length: String = "Short"
)

var gameCount = 0

val gui = kSpigotGUI(GUIType.SIX_BY_NINE) {

    title = "Choose Games:"

    page(1) {
        placeholder(Slots.Border, itemStack(Material.WHITE_STAINED_GLASS_PANE) { meta { name = null } })
        createRectCompound<ItemStack>(
            Slots.RowSixSlotOne,
            Slots.RowSixSlotOne,
            iconGenerator = {
                createMaxGamesChooser()
            },
            onClick = { clickEvent, _ ->
                clickEvent.bukkitEvent.isCancelled = true
                var maxGames = GameManager.maxGames
                val click = clickEvent.bukkitEvent.click
                if (click == ClickType.RIGHT) {
                    maxGames -= 1
                }
                if (click == ClickType.LEFT) {
                    if (GameManager.gamePool.size > maxGames) {
                        maxGames += 1
                    }
                }
                if (maxGames < 1 || maxGames > gameCount) return@createRectCompound
                GameManager.maxGames = maxGames
                clickEvent.bukkitEvent.currentItem = createMaxGamesChooser()
            }
        ).addContent(createMaxGamesChooser())

        val gamesCompound = createRectCompound<Games>(
            Slots.RowTwoSlotTwo,
            Slots.RowFiveSlotEight,

            iconGenerator = {
                updateIcon(it.icon, it.identifier, it.name, it.description, it.length)
                it.icon
            },
            onClick = { clickEvent, element ->
                clickEvent.bukkitEvent.isCancelled = true
                val clickedIcon = clickEvent.bukkitEvent.currentItem!!
                val enabled = GameManager.isGameEnabled(element.identifier)
                if (enabled && GameManager.gamePool.size > 1) {
                    GameManager.disableGame(element.identifier)
                } else GameManager.enableGame(element.identifier)
                if (GameManager.maxGames < GameManager.gamePool.size || GameManager.maxGames > GameManager.gamePool.size) {
                    GameManager.maxGames = GameManager.gamePool.size
                }

                updateIcon(clickedIcon, element.identifier, element.name, element.description, element.length)
                clickEvent.guiInstance[Slots.RowSixSlotOne] = createMaxGamesChooser()
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
    }
}

fun updateIcon(
    icon: ItemStack,
    identifier: String,
    name: String,
    description: ArrayList<String>,
    length: String
): ItemStack {
    val item = ItemBuilder(icon).name("&6${name}".tc())
        .lore(
            "&7Length: &9${length}",
            *description.toTypedArray(),
            "",
            if (GameManager.isGameEnabled(identifier)) "${ChatColor.GREEN}${ChatColor.BOLD}ENABLED" else "${ChatColor.RED}${ChatColor.BOLD}DISABLED"
        )
    return item.build()
}

fun createMaxGamesChooser(): ItemStack {
    return itemStack(Material.PAPER) {
        meta {
            setDisplayName("${ChatColor.GOLD}Choose Max-Games")
            setLore {
                +" "
                +"${ChatColor.GRAY}Selected Games: ${ChatColor.GREEN}${GameManager.gamePool.size}"
                +"§8Total Games in the Game-Pool"
                +"§8that COULD be drawn"
                +" "
                +"${ChatColor.GRAY}Max games: ${ChatColor.GREEN}${GameManager.maxGames}"
                +"§8Amount of games to be played"
                +"§8(drawn from the Game-Pool)"
                +" "
                +"${ChatColor.AQUA}Rightclick ${ChatColor.GRAY}lower"
                +"${ChatColor.YELLOW}Leftclick ${ChatColor.GRAY}higher"
                +" "
                +"${ChatColor.DARK_RED}${ChatColor.BOLD}IMPORTANT:${ChatColor.GRAY}"
                +" §7The number of§a max games"
                +" §7must§c not§7 be higher than the"
                +" §7number of§a selected games§7."
            }
        }
    }
}


class GameCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (sender.hasPermission("knockout.game")) {
            sender.openGUI(gui)
        } else {
            sender.sendLocalizedMessage("TEXT.no_permission", false, ChatColor.RED)
        }
        return true
    }
}
