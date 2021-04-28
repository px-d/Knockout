package de.soho5k.commands

import de.soho5k.game.GameManager
import de.soho5k.utils.extensions.msg
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player

class SkipCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {

        if (sender.hasPermission("knockout.skip")) {
            if (args.size == 1) {
                if (args[0].equals("confirm", true)) {
                    GameManager.activeGame.stop()
                    if(sender is Player){
                        sender.msg("MESSAGES.game.skipped")
                    }else{
                        sender.sendMessage("Skipped the Game")
                    }
                }
            }else{
                if(sender is Player){
                    sender.msg("MESSAGES.game.skipconfirmation")
                }else{
                    sender.sendMessage("Skip with /skip confirm")
                }
            }

        }

        return true
    }

}