package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.fuseSound
import de.soho5k.utils.extensions.mode
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import java.util.*

/**
 * @author Hotkeyyy
 * @since 26.04.2021
 */
class Minefield :
    GameSchematic("Minefield", "minefield", Location(Bukkit.getWorld("minefield"), 0.0, 65.0, 0.0), 180, 20) {

    private val blocks = hashSetOf<Block>()
    private val borderBlocks = hashSetOf<Block>()
    private val winningPlayers = hashSetOf<Player>()
    private val pointDistributor = PointDistributor()

    override fun onStart() {
        borderBlocks.forEach { it.type = Material.AIR }
        Knockout.team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        Knockout.team.setCanSeeFriendlyInvisibles(true)
        onlinePlayers.forEach {
            it.isCollidable = false
            it.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false))
        }
        addEvent(CommonEvents().allEvents())
        addEvent(
            listOf(listen<PlayerInteractEvent> {
                if (it.player.mode != PlayerMode.PLAY) return@listen
                if (it.action != Action.PHYSICAL) return@listen
                if (it.clickedBlock?.type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                    it.player.fuseSound()
                    it.player.teleport(
                        Location(
                            spawn.world,
                            it.player.location.x,
                            it.player.location.y,
                            spawn.z,
                            it.player.location.yaw,
                            it.player.location.pitch
                        )
                    )
                }
            },
                listen<PlayerMoveEvent> {
                    if (it.player.location.z > 103 && !winningPlayers.contains(it.player)) {
                        PlayerManager.setToSpectator(it.player)
                        winningPlayers.add(it.player)
                        pointDistributor.addPlayer(it.player)
                    }
                })
        )
        addEvent(listen<EntityDamageByEntityEvent> {
            if (it.entity is Player && it.damager is Player) it.isCancelled = true
        })
    }

    override fun preGrace() {
        val world = spawn.world
        for (x in -29..29) {
            for (z in 4..101) {
                if (Random().nextInt(5) < 2) {
                    val loc = world?.getHighestBlockAt(x, z)?.location?.clone()?.add(0.0, 1.0, 0.0) ?: return
                    loc.block.type = Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                    blocks.add(loc.block)
                }
            }
        }
        for (x in -29..29) {
            for (y in 64..68) {
                world?.getBlockAt(x, y, 2)?.type = Material.WHITE_STAINED_GLASS
                borderBlocks.add(world?.getBlockAt(x, y, 2)!!)
            }
        }
    }

    override fun tick() {
        if (winningPlayers.size >= onlinePlayers.filter { it.mode == PlayerMode.PLAY }.size) {
            stop()
        }
    }

    override fun onStop() {
        blocks.forEach { it.type = Material.AIR }
        borderBlocks.forEach { it.type = Material.WHITE_STAINED_GLASS }
        pointDistributor.giveRewards(RewardMode.POSITION_THEN_Z)
    }

}