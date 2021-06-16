package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.amountOf
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.extensions.sendLocalizedMessage
import de.soho5k.utils.spleefEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.isFeetInWater
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class Spleef :
    GameSchematic(
        "Spleef",
        "spleef",
        Location(Bukkit.getWorld("spleef"), 0.5, 65.0, 0.5),
        240,
        10,
        broadcast = 20
    ) {

    private val blocks = arrayListOf<Location>()
    private val pointDistributor = PointDistributor()


    override fun equip() {
        onlinePlayers.filter { it.mode == PlayerMode.PLAY }.forEach {equipPlayer(it) }
    }

    override fun equipPlayer(player: Player) {
        PlayerManager.setToSpectator(player, PlayerMode.LEFT)
        player.teleport(spawn.clone().add(0.0,15.0,0.0))
    }

    override fun onStart() {
        addEvent(listen<PlayerInteractEvent> {
            if ((it.action == Action.RIGHT_CLICK_AIR || it.action == Action.RIGHT_CLICK_BLOCK)) {

                if (it.player.inventory.itemInMainHand.type == Material.GOLDEN_SHOVEL) {
                    if (it.player.inventory.containsAtLeast(ItemStack(Material.SNOWBALL), 1)) {
                        val vector = it.player.location.direction

                        val projectile = it.player.launchProjectile(Snowball::class.java, vector)
                        projectile.customName = "spleefball"
                        it.player.inventory.removeItem(ItemStack(Material.SNOWBALL, 1))
                    }
                }
                if (it.player.inventory.itemInMainHand.type == Material.SNOWBALL) {
                    it.isCancelled = true
                    it.player.sendLocalizedMessage(
                        "MESSAGES.spleef.useshovel",
                        prefix = false,
                        mainColor = ChatColor.RED
                    )
                }
            }
        })
        addEvent(listen<ProjectileLaunchEvent> {
            if (it.entity is Snowball) {
                it.entity.customName = "spleefball"
            }
        })
        addEvent(listen<ProjectileHitEvent> {
            if (it.hitBlock != null) {
                if (it.entity.customName == "spleefball" && it.hitBlock!!.type == Material.SNOW_BLOCK) {
                    it.hitBlock?.type = Material.AIR
                    blocks.add(it.hitBlock!!.location)
                }
            }
        })
        addEvent(listen<EntityDamageEvent> {
            if (it.entity is Player) {
                it.isCancelled = true
            }
        })
        addEvent(listen<PlayerMoveEvent> {
            if (it.player.mode == PlayerMode.PLAY && it.player.isFeetInWater) {
                killPlayer(it.player)
            }
        })
        addEvent(listen<BlockBreakEvent> {
            if (it.block.type == Material.SNOW_BLOCK) {
                if (it.player.mode == PlayerMode.PLAY) {
                    blocks.add(it.block.location)
                    if (it.player.amountOf(Material.SNOWBALL) < 32) {
                        if (Random.nextInt(100) < 50) {
                            it.player.inventory.addItem(ItemStack(Material.SNOWBALL, 1))
                        }
                    }
                } else {
                    it.isCancelled = true
                }
                it.isDropItems = false
            } else {
                it.isCancelled = true
            }
        })
        addEvent(listen<CraftItemEvent> { it.isCancelled })
        addEvent(listen<BlockPlaceEvent> { it.isCancelled = it.player.gameMode != GameMode.CREATIVE })
        addEvent(listen<FoodLevelChangeEvent> { it.isCancelled = true })
        addEvent(listen<PlayerDropItemEvent> { it.isCancelled = true })
        for (i in Bukkit.getOnlinePlayers()) {
            i.isCollidable = false
        }
    }

    fun killPlayer(player: Player) {
        PlayerManager.setToSpectator(player)
        pointDistributor.addPlayer(player)
        PlayerManager.checkForAlive()
    }

    override fun onStop() {
        pointDistributor.giveRewards(RewardMode.PER_POSITION)
        for (i in blocks) {
            i.block.type = Material.SNOW_BLOCK
        }
    }
}