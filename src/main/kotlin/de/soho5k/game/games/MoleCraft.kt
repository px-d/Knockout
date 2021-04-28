package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.extensions.addPoints
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
import java.lang.Exception
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

    val blocks = arrayListOf<Location>()


    override fun onStart() {
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
            }
        })

        addEvent(listen<PlayerDropItemEvent> { it.isCancelled = true })

        val radius = 40
        val spawn = ConfigManager.spawns["molecraft"] ?: throw Exception("No Spawn saved for 'molecraft'")
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = spawn.world!!.getBlockAt(
                        spawn.blockX + x,
                        spawn.blockY + y,
                        spawn.blockZ + z
                    )
                    if (block.type == Material.DIRT) {
                        blocks.add(block.location.clone())
                    }
                }
            }
        }


        for (i in blocks) {
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
        for (i in blocks) {
            i.block.type = Material.DIRT
        }
    }

}
