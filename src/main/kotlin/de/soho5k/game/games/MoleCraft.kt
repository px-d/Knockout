package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.extensions.addPoints
import de.soho5k.utils.extensions.getOrSet
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.molecraftEquip
import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerDropItemEvent
import kotlin.random.Random

class MoleCraft :
    GameSchematic(
        "Molecraft",
        "molecraft",
        Location(Bukkit.getWorld("molecraft"), 0.5, 70.0, 0.5),
        120,
        10,
        broadcast = 20
    ) {


    private val top = config.getOrSet("container.top", Location(spawn.world, 30.0, 59.0, 30.0)) as Location
    private val bot = config.getOrSet("container.top", Location(spawn.world, -30.0, 44.0, -30.0)) as Location

    override fun onStart() {

        val loc = barrier(bot, top, null, Material.DIRT)

        addEvent(listen<EntityDamageEvent> { it.isCancelled = true })
        addEvent(listen<BlockBreakEvent> {
            it.isDropItems = false
            it.expToDrop = 0
            val player = it.player
            when (it.block.type) {
                Material.DIAMOND_ORE -> {
                    player.addPoints(1)
                }
                Material.DIAMOND_BLOCK -> {
                    player.addPoints(2)
                }
                Material.EMERALD_ORE -> {
                    player.addPoints(3)
                }
                else -> it.isCancelled = true
            }
        })

        addEvent(listen<PlayerDropItemEvent> { it.isCancelled = true })

        for (i in loc) {
            val random = Random.nextDouble()
            if (i.block.getRelative(BlockFace.UP).type != Material.AIR) {
                if (random >= 0.0 && random < .01) {
                    i.block.type = Material.EMERALD_ORE
                } else if (random >= .01 && random < .03) {
                    i.block.type = Material.DIAMOND_BLOCK
                } else if (random >= .03 && random < .06) {
                    i.block.type = Material.DIAMOND_ORE
                }
            }
        }
    }

    override fun equip() {
        for (i in Bukkit.getOnlinePlayers().filter { it.player?.mode == PlayerMode.PLAY }) {
            i.molecraftEquip()
        }
    }

    override fun onStop() {
        barrier(bot, top, null, Material.DIRT)
    }

}
