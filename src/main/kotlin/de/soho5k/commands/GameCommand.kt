package de.soho5k.commands

import de.soho5k.game.GameManager
import de.soho5k.utils.extensions.tc
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GameCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        return true
        if (args.size == 1) {
            if (args[0].equals("list", true)) {
                sender.sendMessage("&9Aktive Spiele:".tc())
                for (i in GameManager.activeGames) {
                    sender.sendMessage("&7 ${i.name}".tc())
                }
            }
            if (args[0].equals("all", true)) {
                sender.sendMessage("&9Game-Pool:".tc())
                for (i in GameManager.gamePool) {
                    sender.sendMessage("&7 ${i.name}".tc())
                }
            }
        }
        if (args.size == 2) {
            if (args[0].equals("enable", true)) {
                GameManager.enableGame(args[1])
            }
            if (args[0].equals("disable", true)) {
                GameManager.disableGame(args[1])
            }
        }

        return true
    }
}