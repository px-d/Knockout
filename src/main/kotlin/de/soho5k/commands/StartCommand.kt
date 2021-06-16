package de.soho5k.commands

import de.soho5k.game.GameManager
import de.soho5k.utils.extensions.sendLocalizedMessage
import de.soho5k.utils.extensions.toTime
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StartCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (sender.hasPermission("knockout.start")) {
            if (!GameManager.gameStarted) {
                if (GameManager.lobbyTime > 10) {
                    GameManager.lobbyTime = 10
                    sender.sendLocalizedMessage(
                        "MESSAGES.lobby.shortened",
                        false,
                        ChatColor.RED,
                        replacements = arrayOf(10.toTime())
                    )
                }
            } else {
                sender.sendLocalizedMessage("MESSAGES.lobby.already_started", false, ChatColor.RED)
            }
        }
        return true
    }
}
