package de.soho5k.knockout.managers

import de.soho5k.knockout.Knockout
import de.soho5k.utils.extensions.getOrSet
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object ConfigManager {

    private lateinit var config: FileConfiguration

    val spawns = hashMapOf<String, Location>()

    var totalGames: Int = 10

    var minPlayers: Int = 2
    var maxPlayers: Int = 60

    var lobbytime: Int = 60

    lateinit var lobbyLoc: Location
    lateinit var world_lobby: World

    fun enable() {
        println(">> Starting ConfigManager")
        config = Knockout.INSTANCE.config

        totalGames = config.getOrSet("threshold.total_games", 10) as Int

        minPlayers = config.getOrSet("threshold.min_players", 5) as Int
        maxPlayers = config.getOrSet("threshold.max_players", 60) as Int

        lobbytime = config.getOrSet("timings.lobbytime", 60) as Int

        world_lobby = Bukkit.createWorld(WorldCreator("world")) ?: throw Exception("No World: 'world'")
        lobbyLoc = config.getOrSet("locations.lobby", Location(world_lobby, 0.5, 70.0, 0.5)) as Location
    }

    fun getSpawns(id: String): List<Location> {
        val configFile = File("${Knockout.INSTANCE.dataFolder}/games/${id}.yml")
        if (!configFile.exists()) throw Exception("No Such File")
        val config = YamlConfiguration.loadConfiguration(configFile)
        val spawns = config.getList("spawns") ?: throw Exception("No Spawns set")
        return if (spawns.isEmpty()) {
            listOf(Location(Bukkit.getWorld(id), 0.0, 100.0, 0.0))
        } else {
            spawns.filterIsInstance<Location>().toList()
        }
    }
}