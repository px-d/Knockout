package de.soho5k.commands

import de.soho5k.game.GameManager
import de.soho5k.utils.extensions.tc
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GamesCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (GameManager.gameStarted) {
            sender.sendMessage("&9Activated Games".tc())
            for (i in GameManager.activeGames) {
                sender.sendMessage("&8- &7${i.name}".tc())
            }
        }
        return true
    }

}