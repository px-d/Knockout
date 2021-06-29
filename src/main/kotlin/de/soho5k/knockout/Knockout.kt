package de.soho5k.knockout

import com.onarandombox.MultiverseCore.MultiverseCore
import de.hglabor.velocity.queue.constants.QChannels
import de.hglabor.velocity.queue.jedis.JedisManager
import de.soho5k.commands.*
import de.soho5k.game.GameManager
import de.soho5k.game.events.GeneralEvents
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.knockout.managers.RankingManager
import de.soho5k.knockout.queue.GameInfoProvider
import de.soho5k.knockout.queue.QueueChannel
import de.soho5k.player.PlayerManager
import de.soho5k.utils.extensions.getOrSet
import de.soho5k.utils.extensions.tc
import de.soho5k.utils.soholo.HoloManager
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.kick
import net.axay.kspigot.extensions.bukkit.register
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.main.KSpigot
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team


class Knockout : KSpigot() {

    companion object {
        lateinit var INSTANCE: Knockout
        lateinit var multiverse: MultiverseCore
        lateinit var board: Scoreboard
        lateinit var team: Team
        lateinit var holoManager: HoloManager
        lateinit var jedis_serverName: String
        lateinit var jedis_lobbyServerName: String
        lateinit var jedis_password: String
        var restartTime = 30
        var useQueue: Boolean = false
    }

    override fun load() {
        onlinePlayers.forEach { it.kick("&cServer is reloading".tc()) }
    }

    override fun startup() {
        INSTANCE = this
        multiverse = Bukkit.getServer().pluginManager.getPlugin("Multiverse-Core") as MultiverseCore

        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")

        holoManager = HoloManager(this)
        holoManager.start()

        jedis_serverName = config.getOrSet("jedis.servername", "knockout") as String
        jedis_lobbyServerName = config.getOrSet("jedis.lobby_name", "lobby") as String
        jedis_password = config.getOrSet("jedis.password", "") as String
        useQueue = config.getOrSet("jedis.useQueue", false) as Boolean
        restartTime = config.getOrSet("lobby.restart_time", 30) as Int

        ConfigManager.enable()
        PlayerManager.enable()
        GeneralEvents.enable()
        GameManager.enable()
        /**
         * Server Queue
         */
        if (useQueue) initJedis()

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
        GamesCommand().register("games")

        RankingManager.enable()
    }

    override fun shutdown() {
        holoManager.end()
    }

    private fun initJedis() {
        JedisManager.init(jedis_password)
        JedisManager.subscribe(QueueChannel(jedis_serverName), QChannels.QUEUE_JOIN.get(), QChannels.QUEUE_LEAVE.get())
        GameInfoProvider.enable(jedis_serverName)
    }

    init {
        listen<ProjectileHitEvent> {
            if (it.entity is Snowball ){
                (it.hitEntity as? LivingEntity)?.damage(0.0, (it.entity.shooter as? LivingEntity))
            }
        }
    }
}