package de.soho5k.game.games

import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.player.PlayerManager
import de.soho5k.utils.PointDistributor
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.isFeetInWater
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.random.Random

/**
 * Could still lead to all games being finished at the same time.
 * Wait for PlayerManger Checks
 */

class Firefly : GameSchematic(
    "Firefly",
    "firefly",
    Location(Bukkit.getWorld("firefly"), 0.5, 70.0, 0.5),
    120,
    10
) {
    private val blocks = arrayListOf<Location>()
    private val placements = PointDistributor()


    override fun onStart() {
        addEvent(CommonEvents().allEvents())
        addEvent(
            listen<PlayerMoveEvent> {
                if (it.player.isFeetInWater) {
                    killPlayer(it.player)
                }
            }
        )

        addEvent(listen<EntityDamageEvent> {
            it.isCancelled = it.entity is Player
        })


        val radius = 50
        val world = spawn.world ?: throw Exception("World Not Existing")
        val loc = spawn

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = world.getBlockAt(
                        loc.blockX + x,
                        loc.blockY + y,
                        loc.blockZ + z
                    )

                    if (block.type == Material.WHITE_WOOL) {
                        blocks.add(block.location.clone())
                    }
                }
            }
        }

        task(period = 2L) { runnable ->
            if (!isRunning) runnable.cancel()
            val amount = Random.nextInt(49) + 1
            for (i in 0..amount) {
                if (blocks.any { it.block.type != Material.AIR }) {
                    changeBlock(blocks.filter { it.block.type != Material.AIR }.random())
                } else {
                    runnable.cancel()
                    GameManager.activeGame.stop()
                    break
                }
            }
        }
    }

    override fun onStop() {
        placements.giveRewards(points = listOf(10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
        task(delay = 20L) {
            for (i in blocks) {
                i.block.type = Material.WHITE_WOOL
            }
        }
    }

    private fun changeBlock(location: Location) {
        when (location.block.type) {
            Material.WHITE_WOOL -> {
                location.block.type = Material.YELLOW_WOOL
            }
            Material.YELLOW_WOOL -> {
                location.block.type = Material.ORANGE_WOOL
            }
            Material.ORANGE_WOOL -> {
                location.block.type = Material.RED_WOOL
            }
            Material.RED_WOOL -> {
                location.block.type = Material.AIR
            }
            else -> {
            }
        }
    }

    private fun killPlayer(player: Player) {
        placements.addPlayer(player)
        PlayerManager.setToSpectator(player)
        player.teleport(spawn.clone().add(0.0, 5.0, 0.0))
        player.isFlying = true
        PlayerManager.checkForAlive()
    }
}