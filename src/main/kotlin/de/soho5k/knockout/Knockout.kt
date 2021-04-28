package de.soho5k.knockout

import de.soho5k.utils.soholo.HoloManager
import com.onarandombox.MultiverseCore.MultiverseCore
import de.soho5k.commands.*
import de.soho5k.game.GameManager
import de.soho5k.game.events.GeneralEvents
import de.soho5k.knockout.queue.GameInfoProvider
import de.soho5k.knockout.queue.QueueChannel
import de.soho5k.player.PlayerManager
import de.soho5k.utils.extensions.getOrSet
import de.soho5k.utils.extensions.tc
import net.axay.kspigot.extensions.bukkit.kick
import net.axay.kspigot.extensions.bukkit.register
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.main.KSpigot
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

class Knockout : KSpigot() {

    companion object {
        lateinit var INSTANCE: Knockout
        lateinit var multiverse: MultiverseCore
        lateinit var board: Scoreboard
        lateinit var team: Team
        lateinit var holoManager: HoloManager
        lateinit var serverName: String
    }

    override fun load() {
        onlinePlayers.forEach { it.kick("&cServer is reloading".tc()) }
    }

    override fun startup() {
        INSTANCE = this
        multiverse = Bukkit.getServer().pluginManager.getPlugin("Multiverse-Core") as MultiverseCore

        holoManager = HoloManager(this)
        holoManager.start()

        serverName = config.getOrSet("servername","knockout") as String

        ConfigManager.enable()
        PlayerManager.enable()
        GameManager.enable()
        QueueChannel.enable()
        GameInfoProvider.enable(serverName)

        GeneralEvents.enable()

        for (i in Bukkit.getOnlinePlayers()) {
            PlayerManager.registerPlayer(i)
        }

        board = Bukkit.getScoreboardManager()!!.mainScoreboard
        team = if (board.getTeam("knockout_game") == null) {
            board.registerNewTeam("knockout_game")
        } else {
            board.getTeam("knockout_game") ?: throw Exception("No Such Team")
        }

        StartCommand().register("start")
        KnockoutCommand().register("knockout")
        StaffmodeCommand().register("staffmode")
        SkipCommand().register("skip")
        GameCommand().register("game")
        GenerateConfig().register("spawns")

        RankingManager.enable()
    }

    override fun shutdown() {
        onlinePlayers.forEach { it.kick("&cServer is reloading".tc()) }
        holoManager.end()
    }


}