package de.soho5k.commands

import de.soho5k.game.GameManager
import de.soho5k.knockout.Knockout
import de.soho5k.utils.extensions.tc
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class GenerateConfig : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player || !sender.isOp || GameManager.gameStarted) return true
        if (args.size < 2) {
            sender.sendMessage("&cMissing Arguments: /spawns <game> <radius> [material (default=EMERALD_BLOCK)]".tc())
            return true
        }

        val path = "${Knockout.INSTANCE.dataFolder}/games/${args[0]}.yml"
        val radius = args[1].toInt()
        val material = if (args.size == 3) Material.valueOf(args[2].uppercase()) else Material.EMERALD_BLOCK

        val playerPos = sender.location
        val world = playerPos.world ?: throw Exception("World Not Existing")
        val spawns = hashSetOf<Location>()
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = world.getBlockAt(
                        playerPos.blockX + x,
                        playerPos.blockY + y,
                        playerPos.blockZ + z
                    )
                    if (block.type == material) {
                        spawns.add(block.location)
                    }
                }
            }
        }
        sender.sendMessage("&eTotal Spawns: &c${spawns.size}&e, Set for Minigame: &c${args[0]}".tc())

        val file = File(path)
        if (!file.exists()) {
            sender.sendMessage("&cThis Path/Game doesn't exist".tc())
            return true
        }
        val yaml = YamlConfiguration.loadConfiguration(file)
        yaml.set("spawns", spawns.toArray())
        yaml.save(file)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {

        if (GameManager.gameStarted) return mutableListOf()

        if (args.size == 1) {
            return mutableListOf(
                "anvilrain",
                "firefly",
                "hotpotato",
                "kbffa",
                "koth",
                "minefield",
                "molecraft",
                "oitc",
                "rainbowrun",
                "redlights",
                "riptide",
                "snowfight",
                "spleef",
                "sg",
                "tntrun"
            )
        }
        if (args.size == 2) {
            return mutableListOf(
                "10",
                "20",
                "30",
                "40",
                "50",
                "100"
            )
        }
        if (args.size == 3) {
            return Material.values()
                .filter { it.toString().lowercase().startsWith(args[2].lowercase()) && it.isBlock }
                .map { it.toString() }.toMutableList()
        }

        return mutableListOf()
    }

}