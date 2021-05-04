package de.soho5k.game.games

import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.getOrSet
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.extensions.sendLocalizedMessage
import de.soho5k.utils.extensions.xpSound
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.random.Random


class Redlights : GameSchematic(
    "Red Lights",
    "redlights",
    Location(Bukkit.getWorld("redlights"), 0.0, 60.0, 0.0, -90F, 5F),
    90,
    10
) {
    private var inGameMode = false
    private var isRed = false
    private val goalX = config.getOrSet("settings.goalX", 126) as Int
    private val randomTimeYellowToRed = config.getOrSet("settings.yellowToRed", 6) as Int
    private val pointDistributor = PointDistributor()

    private val borderT = config.getOrSet("border_top", Location(spawn.world, 3.0, 62.0, 19.0)) as Location
    private val borderB = config.getOrSet("border_bot", Location(spawn.world, 3.0, 60.0, -19.0)) as Location

    private val trafficLights = hashSetOf<Location>()

    private val traffic1T =
        config.getOrSet("trafficlight.first.top", Location(spawn.world, 18.0, 67.0, -22.0)) as Location
    private val traffic1B =
        config.getOrSet("trafficlight.first.bottom", Location(spawn.world, 18.0, 67.0, 22.0)) as Location

    private val traffic2T =
        config.getOrSet("trafficlight.second.top", Location(spawn.world, 87.0, 67.0, -22.0)) as Location
    private val traffic2B =
        config.getOrSet("trafficlight.second.bottom", Location(spawn.world, 87.0, 67.0, 22.0)) as Location

    init {
        for (z in traffic1T.blockZ..traffic1B.blockZ) {
            val loc = Location(traffic1T.world, traffic1T.x, traffic1T.y, z.toDouble())

            if (loc.block.type == Material.WHITE_CONCRETE) {
                trafficLights.add(loc)
            }
        }

        for (z in traffic2T.blockZ..traffic2B.blockZ) {
            val loc = Location(traffic2T.world, traffic2T.x, traffic2T.y, z.toDouble())

            if (loc.block.type == Material.WHITE_CONCRETE) {
                trafficLights.add(loc)
            }
        }
    }

    override fun onStart() {
        barrier(borderB, borderT, Material.WHITE_STAINED_GLASS, Material.AIR)
        setWool(Material.GREEN_WOOL)

        addEvent(CommonEvents(cancelPlayerDamage = true).allEvents())
        addEvent(
            listen<InventoryClickEvent> {
                if ((it.whoClicked as Player).mode != PlayerMode.STAFF) {
                    it.isCancelled = true
                }
            }
        )
        addEvent(
            listen<PlayerMoveEvent> {
                if (it.player.mode == PlayerMode.PLAY) {
                    if (it.to?.x?.toInt()!! > goalX) {
                        PlayerManager.setToSpectator(it.player)
                        pointDistributor.addPlayer(it.player)

                        onlinePlayers.forEach { p ->
                            p.sendLocalizedMessage(
                                "MESSAGES.game.player_finished",
                                false,
                                ChatColor.RED,
                                ChatColor.GOLD,
                                it.player.name
                            )
                        }
                    }

                    if ((it.player.location.x != it.to?.x || it.player.location.z != it.to?.z) && isRed) {
                        val loc = it.player.location.clone()
                        it.player.teleport(Location(loc.world, 0.0, loc.y, loc.z, loc.yaw, loc.pitch))
                    }
                }
            }
        )
    }

    var minTime = 4
    override fun tick() {
        if (onlinePlayers.filter { it.mode == PlayerMode.SPECTATE }.size >=
            onlinePlayers.filter { it.mode == PlayerMode.PLAY }.size / 2
        ) {
            stop()
            return
        }
        if (!inGameMode) {
            if (minTime == 0) {
                game()
                return
            }

            if (Random.nextInt(5) == 0) {
                game()
            }
            minTime--
        }
    }


    override fun onStop() {
        barrier(borderB, borderT, Material.AIR, Material.WHITE_STAINED_GLASS)
        pointDistributor.giveRewards(RewardMode.POSITION_THEN_X)
        trafficLights.forEach { it.block.type = Material.WHITE_CONCRETE }
    }

    fun game() {
        inGameMode = true
        var time = 0
        minTime = 4
        var redTime = Random.nextInt(randomTimeYellowToRed)
        if (redTime == 0) redTime = 2
        setWool(Material.YELLOW_WOOL)
        task(period = 20L) {
            if (GameManager.inLobby) {
                it.cancel()
                return@task
            }
            if (time == redTime) {
                setWool(Material.RED_WOOL)
                isRed = true
            }
            if (time == redTime.plus(2)) {
                setWool(Material.GREEN_WOOL)
                isRed = false
                inGameMode = false
                it.cancel()
            }
            time++
        }
    }

    private fun setWool(type: Material) {
        onlinePlayers.filter { it.mode == PlayerMode.PLAY }.forEach {
            for (i in 0 until 9) {
                it.inventory.setItem(i, itemStack(type) {})
            }
            it.xpSound()
        }

        trafficLights.forEach {
            it.block.type = type
        }
    }


}