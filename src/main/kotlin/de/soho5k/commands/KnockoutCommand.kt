package de.soho5k.commands

import de.soho5k.utils.extensions.tc
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player

class KnockoutCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        sender.sendMessage("&bKnock&fOut &7by [rgb:255,72,79]soho5k&8, [rgb:138,5,101]Hotkeyyy".tc())
        return true
    }
}