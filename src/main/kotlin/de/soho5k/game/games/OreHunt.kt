package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.*
import de.soho5k.utils.molecraftEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPreLoginEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import javax.print.attribute.IntegerSyntax
import kotlin.random.Random

class OreHunt :
    GameSchematic(
        "Ore Hunt",
        "orehunt",
        Location(Bukkit.createWorld(WorldCreator("orehunt")), 0.5, 70.0, 0.5),
        120,
        10,
        broadcast = 20
    ) {

    private val top = setConfig("container.top", Location(spawn.world, 30.5, 69.0, 30.5)) as Location
    private val bot = setConfig("container.bot", Location(spawn.world, -29.5, 40.0, -29.5)) as Location

    private val points = hashMapOf<Player, Int>()

    override fun onStart() {
        val loc = barrier(bot, top, null, Material.DIRT)
        onlinePlayers.forEach {
            it.addPotionEffect(
                PotionEffect(
                    PotionEffectType.NIGHT_VISION,
                    Integer.MAX_VALUE,
                    10,
                    true,
                    false,
                    false
                )
            )
        }
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
                    points[player]?.plus(1)
                    playBlockSound(it.player)
                }
                Material.EMERALD_ORE -> {
                    points[player]?.plus(2)
                    playBlockSound(it.player)
                }
                Material.EMERALD_BLOCK -> {
                    points[player]?.plus(3)
                    playBlockSound(it.player)
                }
                else -> it.isCancelled = true
            }
        })

        addEvent(listen<PlayerDropItemEvent> { it.isCancelled = true })
        addEvent(listen<FoodLevelChangeEvent> { it.isCancelled = true })

        for (i in loc) {
            val random = Random.nextDouble()
            if (i.block.getRelative(BlockFace.UP).type != Material.AIR) {
                when (random) {
                    in 0.0..0.01 -> {
                        i.block.type = Material.EMERALD_BLOCK
                    }
                    in 0.02..0.04 -> {
                        i.block.type = Material.EMERALD_ORE
                    }
                    in 0.05..0.1 -> {
                        i.block.type = Material.DIAMOND_ORE
                    }
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
        val pointDistributor = PointDistributor()
        for (i in points.toList().sortedBy { it.second }.toMap().keys) pointDistributor.addPlayer(i)
        pointDistributor.giveRewards(RewardMode.FIRST, listOf(10, 10, 10, 9, 9, 9, 8, 8, 8, 7, 7, 7, 6, 6, 6, 5, 5, 5))
        onlinePlayers.forEach { it.sendLocalizedMessage("MESSAGES.orehunt.endpoints", false, ChatColor.RED) }
    }

    private fun playBlockSound(player: Player) {
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 2f)
    }

}
