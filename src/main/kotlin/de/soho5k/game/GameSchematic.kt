package de.soho5k.game

import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerManager
import de.soho5k.utils.Localization.getLocalized
import de.soho5k.utils.extensions.*
import events.GameEndEvent
import events.GameStartEvent
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.io.File

abstract class GameSchematic(
    val name: String,
    private val identifier: String,
    var spawn: Location,
    private var duration: Int,
    var grace: Int = 0,
    private var running: Boolean = false,
    private val events: ArrayList<Listener> = arrayListOf(),
    var multiplier: Int = 1,
    var broadcast: Int = 10
) {
    /**
     *How much time is left in the game
     */
    val time: Int get() = duration
    val id: String get() = identifier

    val isRunning: Boolean
        get() = running

    var stopped = false

    private val configFile = File("${Knockout.INSTANCE.dataFolder}/games/${identifier}.yml")
    val config = YamlConfiguration.loadConfiguration(configFile)

    init {
        grace = config.setOrDefault("$identifier.grace", grace, configFile) as Int
        duration = config.setOrDefault("$identifier.time", duration, configFile) as Int
        spawn = config.setOrDefault("$identifier.spawn", spawn, configFile) as Location
        ConfigManager.spawns[identifier] = spawn
    }

    fun addEvent(event: Listener) = events.add(event)
    fun addEvent(events: List<Listener>) = events.forEach { addEvent(it) }


    /**
     * Start the Game
     */
    fun start() {
        broadcastIntroToAll(id)
        teleportAllToGame(spawn)

        if (grace > 0) {
            onlinePlayers.forEach {
                it.sendLocalizedMessage(
                    "MESSAGES.game.grace",
                    replacements = arrayOf(it.player!!.getLocalized("SCOREBOARD.title.$id"), grace.toTime())
                )
            }
        }

        var graceperiod = grace
        preGrace()
        task(howOften = grace.toLong(), period = 20L) {
            if (stopped) it.cancel()
            onlinePlayers.forEach { p ->
                p.title(
                    "&e${p.getLocalized("SCOREBOARD.title.$id")}".tc(),
                    if (grace > 0) "&7${".".repeat(graceperiod)}${graceperiod.toTime()}${".".repeat(graceperiod)}".tc() else "",
                    0,
                    21,
                    0
                )
            }
            graceperiod--
        }

        //START THE GAME AFTER GRACE PERIOD
        task(delay = (grace * 20L), howOften = 1L) {
            if (!stopped) {
                registerAll()
                onStart()
                equip()
                run()
            } else {
                it.cancel()
            }
        }
    }

    /**
     * End the Game and unregister all events (Performance)
     */
    fun stop() {
        onStop()
        GameManager.inLobby = true
        running = false
        stopped = true
        if (!GameManager.ended) {
            Bukkit.getPluginManager().callEvent(GameEndEvent(this))
        }
        for (i in events) {
            i.unregister()
        }
        Bukkit.getPluginManager().registerEvents(GameManager.events, Knockout.INSTANCE)

        task(delay = 5L) { PlayerManager.teleportToLobby() }
    }

    private fun run() {
        if (stopped) return
        running = true
        GameManager.inLobby = false
        task(period = 20) {
            if (time == 0) {
                stop()
                it.cancel()
            } else {
                if (time.shouldBroadcast(broadcast)) {
                    Bukkit.getOnlinePlayers().forEach { p ->
                        p.sendLocalizedMessage(
                            "MESSAGES.minigame.ending",
                            replacements = arrayOf(p.getLocalized("SCOREBOARD.title.$id"), time.toTime())
                        )
                        p.ping()
                    }
                }
                tick()
                PlayerManager.refreshAllBoards()
                duration--
            }

            if (!isRunning || stopped) {
                it.cancel()
            }
        }
    }

    /**
     * Function that is being called when the Game stops.
     * Called before any other stuff in GameSchematic class.
     */
    open fun onStop() {}

    /**
     * Function that is being called when the Game starts.
     * Called before any other stuff in GameSchematic class.
     */
    open fun onStart() {}

    /**
     * Function that is being called when the Grace-Period is over.
     * (If the Grace-Period is 0 this gets called immediately)
     */
    open fun equip() {}

    /**
     * Function that is being called every tick.
     */
    open fun tick() {}

    /**
     *Function that is being called
     */
    open fun preGrace() {}

    private fun registerAll() {
        if (stopped) return
        Bukkit.getPluginManager().callEvent(GameStartEvent(this, events))
        HandlerList.unregisterAll(GameManager.events)
        onlinePlayers.forEach {
            it.ping(1.2f)
            it.sendLocalizedMessage("MESSAGES.game.started")
            it.title("", it.getLocalized("TEXT_glhf").tc())
        }
    }

    fun barrier(borderB: Location, borderT: Location, target: Material, replaceWith: Material) {
        for (x in borderB.blockX..borderT.blockX) {
            for (y in borderB.blockY..borderT.blockY) {
                for (z in borderB.blockZ..borderT.blockZ) {
                    val loc = Location(spawn.world, x.toDouble(), y.toDouble(), z.toDouble())
                    if (loc.block.type == target) loc.block.type = replaceWith
                }
            }
        }
    }
}