package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.extensions.*
import de.soho5k.utils.molecraftEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPreLoginEvent
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


    private val top = setConfig("container.top", Location(spawn.world, 30.0, 69.0, 30.0)) as Location
    private val bot = setConfig("container.bot", Location(spawn.world, -30.0, 40.0, -30.0)) as Location

    override fun onStart() {

        val loc = barrier(bot, top, null, Material.DIRT)
        broadcast("Size: ${loc.size}")

        println(top.formatted())
        println(bot.formatted())

        addEvent(listen<EntityDamageEvent> { it.isCancelled = true })
        addEvent(listen<BlockBreakEvent> {
            it.isDropItems = false
            it.expToDrop = 0
            val player = it.player
            when (it.block.type) {
                Material.DIRT -> {
                    it.isDropItems = false
                }
                Material.DIAMOND_ORE -> {
                    player.addPoints(1)
                    playBlockSound(it.player)
                }
                Material.DIAMOND_BLOCK -> {
                    player.addPoints(2)
                    playBlockSound(it.player)
                }
                Material.EMERALD_ORE -> {
                    player.addPoints(3)
                    playBlockSound(it.player)
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

    private fun playBlockSound(player: Player) {
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 2f)
    }

}
