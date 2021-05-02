package de.soho5k.commands

import de.soho5k.game.GameManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player

class StartCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (sender.hasPermission("knockout.start")) {
            if (!GameManager.gameStarted) {
                if (GameManager.lobbytime > 10)
                    GameManager.lobbytime = 10
            } else {
                sender.sendMessage("Game Already Started")
            }
        }

        return true
    }

}