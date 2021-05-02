package de.soho5k.game.games

import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.getOrSet
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.extensions.xpSound
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import kotlin.random.Random

/**
 * @author Hotkeyyy
 * @since 01.05.2021
 */

class Redlights : GameSchematic(
    "Red Lights",
    "redlights",
    Location(Bukkit.getWorld("redlights"), 0.0, 70.0, 0.0),
    90,
    10
) {
    private var inGameMode = false
    private var isRed = false
    private val goalX = config.getOrSet("settings.goalX", 126) as Int
    private val randomTimeYellowToRed = config.getOrSet("settings.yellowToRed", 6) as Int
    private val playersInGoal = hashSetOf<Player>()
    private val pointDistributor = PointDistributor()

    override fun onStart() {
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
                if (playersInGoal.contains(it.player)) return@listen
                if (it.to?.x?.toInt()!! > goalX) {
                    playersInGoal.add(it.player)
                    pointDistributor.addPlayer(it.player)
                    return@listen
                }
                if ((it.player.location.x != it.to?.x ||
                            it.player.location.z != it.to?.z) && isRed
                ) {
                    val pl = it.player.location.clone()
                    pl.x = 0.0
                    it.player.teleport(pl)
                }
            }
        )
        setWool(Material.GREEN_WOOL)
    }

    var minTime = 4
    override fun tick() {
        if (playersInGoal.size >= onlinePlayers.filter { it.mode == PlayerMode.PLAY }.size / 2) {
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
        pointDistributor.giveRewards(RewardMode.POSITION_THEN_X)
    }

    fun game() {
        inGameMode = true
        var time = 0
        minTime = 4
        var redTime = Random.nextInt(randomTimeYellowToRed)
        if(redTime == 0) redTime = 2
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
    }
}