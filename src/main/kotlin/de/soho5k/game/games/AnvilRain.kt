package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.mode
import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class AnvilRain : GameSchematic(
    "Anvil Rain",
    "anvilrain",
    Location(Bukkit.getWorld("anvilrain"), 0.5, 70.0, 0.5),
    120,
    10
) {

    var rate = 100

    private val spawnLoc = spawn
    private val pointDistributor = PointDistributor()





    override fun tick() {
        if (time % 2 == 0) rate += 2
        task(period = 1L, howOften = rate.toLong()) {
            if (!running) it.cancel()
            val x = Random.nextInt(40) - 20
            val z = Random.nextInt(40) - 20

            val f = spawnLoc.world!!.spawnFallingBlock(
                spawnLoc.clone().add(x.toDouble(), 100.0, z.toDouble()),
                Material.DAMAGED_ANVIL.createBlockData()
            )

            f.setHurtEntities(true)
            f.dropItem = false
        }
    }

    override fun onStart() {
        addEvent(listen<PlayerDeathEvent> {
            killPlayer(it.entity)
        })
        addEvent(listen<EntityDamageEvent> {
            if (it.entity is Player) {
                it.damage = 0.0
                if (it.cause == EntityDamageEvent.DamageCause.FALLING_BLOCK && (it.entity as Player).mode == PlayerMode.PLAY) {
                    killPlayer(it.entity as Player)
                }
            }
        })
        addEvent(listen<EntityDamageByEntityEvent> {
            if (it.entity is Player && it.damager is Player) {
                it.isCancelled = true
            }
        })
        addEvent(listen<BlockBreakEvent> {
            it.isCancelled = it.player.gameMode != GameMode.CREATIVE
        })
        addEvent(listen<EntityChangeBlockEvent> {
            if (it.entity is FallingBlock) {
                val f = it.entity as FallingBlock
                if (f.blockData.material == Material.DAMAGED_ANVIL) {
                    it.isCancelled = true
                    if (f.isOnGround) {
                        f.getNearbyEntities(.5, .5, .5).forEach { entity ->
                            if (entity is Player) {
                                killPlayer(entity)
                            }
                        }
                        f.remove()
                        it.block.type = Material.AIR
                    }
                }
            }
        })

        for (i in Bukkit.getOnlinePlayers()) {
            i.addPotionEffect(
                PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    true,
                    false,
                    false
                )
            )
        }
    }

    private fun killPlayer(player: Player) {
        player.mode = PlayerMode.SPECTATE
        player.inventory.clear()
        for (i in Bukkit.getOnlinePlayers()) i.hidePlayer(Knockout.INSTANCE, player)
        player.addPotionEffect(
            PotionEffect(
                PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                0,
                true,
                false,
                false
            )
        )
        player.allowFlight = true
        pointDistributor.addPlayer(player)
        task(delay = 21L) {
            player.spigot().respawn()
            player.teleport(spawn.clone().add(0.5, 10.0, 0.5))
            PlayerManager.checkForAlive()
        }
    }

    override fun onStop() {
        pointDistributor.giveRewards(RewardMode.FIRST, listOf(10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
    }
}
