package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.fuseSound
import de.soho5k.utils.extensions.mode
import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scoreboard.Team
import java.util.*
import kotlin.math.floor


class TNTRun :
    GameSchematic(
        "TNT Run",
        "tntrun",
        Location(Bukkit.getWorld("tntrun"), 0.5, 70.2, 0.5),
        300,
        10
    ) {

    private val blocks = hashSetOf<Location>()
    private val recentMovement = hashMapOf<UUID, Long>()
    private val pointDistributor = PointDistributor()

    override fun onRejoin(player: Player) {
        PlayerManager.setToSpectator(player, PlayerMode.LEFT)
        player.isFlying = true
        player.teleport(spawn.clone().add(0.0, 20.0, 0.0))
    }

    override fun onStart() {
        addEvent(listen<PlayerMoveEvent> { e ->
            if (e.player.mode != PlayerMode.PLAY) return@listen
            if (e.player.location.y < 50) {
                if (e.player.mode == PlayerMode.PLAY) {
                    killPlayer(e.player)
                } else {
                    e.player.teleport(spawn)
                }
            }

            val block1 = e.player.location.block.getRelative(BlockFace.DOWN)
            val block2 = e.player.location.block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN)

            task(delay = 10L, howOften = 1L) {
                if (block1.type == Material.TNT) {
                    block1.type = Material.AIR
                    blocks.add(block1.location)
                    e.player.fuseSound()
                }
                if (block2.type == Material.TNT) {
                    block2.type = Material.AIR
                    blocks.add(block2.location)
                    e.player.fuseSound()
                }
            }

            val from: Location = e.from
            val to: Location = e.to ?: return@listen
            val x = floor(from.x)
            val z = floor(from.z)
            if (floor(to.x) != x || floor(to.z) != z) {
                //player just moved a block
                recentMovement[e.player.uniqueId] = Date().time
                val lastMove = recentMovement[e.player.uniqueId] ?: return@listen
                task(delay = 20L, howOften = 1L) {
                    if (recentMovement[e.player.uniqueId] == lastMove) {
                        removeNearby(e.player)
                    }
                }
            }
        })
        addEvent(listen<PlayerInteractEvent> {
            it.isCancelled = it.player.gameMode != GameMode.CREATIVE
        })
        addEvent(listen<EntityDamageEvent> {
            it.isCancelled = it.entity is Player
        })
        addEvent(listen<FoodLevelChangeEvent> { it.isCancelled = true })
        Knockout.team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        blocks.forEach { it.block.type = Material.TNT }
    }

    override fun onStop() {
        blocks.forEach { it.block.type = Material.TNT }
        recentMovement.clear()
        pointDistributor.giveRewards(RewardMode.FIRST_TO_LAST)
    }

    private fun killPlayer(player: Player) {
        player.teleport(spawn.clone().add(0.5, 15.0, 0.5))
        PlayerManager.setToSpectator(player)
        pointDistributor.addPlayer(player)
        PlayerManager.checkForAlive()
    }

    private fun removeNearby(p: Player) {
        for (block in getBlocksBelow(p)) {
            if (block.type == Material.TNT) {
                block.type = Material.AIR
                blocks.add(block.location)
                p.fuseSound()
            }
        }
    }

    private fun getBlocksBelow(player: Player): HashSet<Block> {
        val blocksBelow = hashSetOf<Block>()
        val location = player.location
        val x = location.x
        val z = location.z
        val world = player.world
        val yBelow = player.location.y - 0.0001
        val northEast = Location(world, x + 0.3, yBelow, z - 0.3).block
        val northWest = Location(world, x - 0.3, yBelow, z - 0.3).block
        val southEast = Location(world, x + 0.3, yBelow, z + 0.3).block
        val southWest = Location(world, x - 0.3, yBelow, z + 0.3).block
        val directBlock = Location(world, x, yBelow, z).block

        val blocks = arrayOf(northEast, northWest, southEast, southWest, directBlock)
        for (block in blocks) {
            blocksBelow.add(block)
        }
        return blocksBelow
    }
}
