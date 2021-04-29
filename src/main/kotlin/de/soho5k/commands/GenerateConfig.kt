package de.soho5k.commands

import de.soho5k.knockout.Knockout
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.lang.Exception

class GenerateConfig : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player || !sender.isOp) return true

        val radius = if (args.size == 1) args[0].toInt() else 30
        val path =
            if (args.size == 2) "${Knockout.INSTANCE.dataFolder}/games/${args[1]}.yml" else "${Knockout.INSTANCE.dataFolder}/generated/generatedconfig.yml"
        val playerPos = sender.location

        val world = playerPos.world ?: throw Exception("World Not Existing")

        val spawns = arrayListOf<Location>()

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = world.getBlockAt(
                        playerPos.blockX + x,
                        playerPos.blockY + y,
                        playerPos.blockZ + z
                    )
                    if (block.type == Material.EMERALD_BLOCK) {
                        spawns.add(block.location)
                    }
                }
            }
        }
        println("Total Spawns: ${spawns.size}")


        val file = File(path)

        val yaml =
            YamlConfiguration.loadConfiguration(file)

        yaml.set("spawns", spawns.toArray())
        yaml.save(file)

        return true
    }

}