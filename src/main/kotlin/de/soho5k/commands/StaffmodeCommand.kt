package de.soho5k.commands

import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.extensions.sendLocalizedMessage
import de.soho5k.utils.extensions.tc
import net.axay.kspigot.annotations.UnsafeImplementation
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Haven't totally tested this feature yet, could be buggy or maybe even gambreaking.
 */
@UnsafeImplementation
class StaffmodeCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        sender.mode = PlayerMode.STAFF
        sender.sendLocalizedMessage(
            "MESSAGES.mode",
            replacements = arrayOf(sender.mode.toString().toLowerCase().capitalize())
        )
        sender.sendMessage("&4This is very unstabled. I haven't implemented it correctly as of right now. Please only use sparely.".tc())
        PlayerManager.refreshAllBoards()
        for (i in Bukkit.getOnlinePlayers()) {
            i.hidePlayer(Knockout.INSTANCE, sender)
        }
        return true
    }

}