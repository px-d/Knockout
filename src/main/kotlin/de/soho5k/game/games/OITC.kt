package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerMode
import de.soho5k.utils.clearInv
import de.soho5k.utils.extensions.*
import de.soho5k.utils.oitcEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.heal
import net.axay.kspigot.runnables.task
import org.bukkit.*
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import org.bukkit.util.Vector

class OITC : GameSchematic(
    "OITC",
    "oitc",
    Location(Bukkit.getWorld("oitc"), 0.5, 70.0, 0.5),
    180,
    10,
    broadcast = 20
) {
    private val spawns = ConfigManager.getSpawns("oitc")

    override fun onStart() {
        spawn.world?.setGameRule(GameRule.KEEP_INVENTORY, true)
        addEvent(listen<PlayerDropItemEvent> {
            it.isCancelled = true
        })
        addEvent(listen<BlockBreakEvent> {
            if (it.player.gameMode != GameMode.CREATIVE) {
                it.isCancelled = true
            }
        })
        addEvent(listen<EntityShootBowEvent> {
            if (it.entity is Player) {
                if (it.projectile is Arrow) {
                    (it.projectile as Arrow).shooter = it.entity
                }
            }
        })
        addEvent(listen<ProjectileHitEvent> {
            if (it.entity is Arrow) {
                if (it.entity.shooter is Player && it.hitEntity is Player) {
                    (it.hitEntity as Player).inventory.clear()

                    killPlayer(it.hitEntity as Player, it.entity.shooter as Player)
                }
                it.entity.remove()
            }
        })
        addEvent(listen<FoodLevelChangeEvent> { it.isCancelled = true })
        addEvent(listen<EntityDamageEvent> {
            if (it.entity is Player) {
                if (it.cause == EntityDamageEvent.DamageCause.FALL || it.cause == EntityDamageEvent.DamageCause.PROJECTILE) {
                    it.damage = 0.0
                }
            }
        })

        addEvent(listen<PlayerDeathEvent> {
            it.entity.clearInv()
            it.keepInventory = true
            it.deathMessage = ""
            if (it.entity.killer != null) {
                if (it.entity.killer is Player) {
                    killPlayer(it.entity, it.entity.killer!!)
                }
            } else {
                respawnPlayer(it.entity)
            }
        })

        Knockout.team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
        for (i in Bukkit.getOnlinePlayers().filter { it.mode == PlayerMode.PLAY }) {
            respawnPlayer(i)
        }
    }

    private fun killPlayer(killed: Player, from: Player) {
        if (killed.uniqueId == from.uniqueId) {
            from.suicideSound()
        } else {
            from.killSound()
            from.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 100, 1, true, false, false))
            from.addPoints(3)
            from.inventory.addItem(ItemStack(Material.ARROW))
            from.heal()
        }
        task(delay = 2L) {
            if (killed.uniqueId == from.uniqueId) {
                from.addPoints(-3)
            } else {
                killed.sendLocalizedMessage("MESSAGE.oitc.killed", replacements = arrayOf(from.name))
            }
            respawnPlayer(killed)
        }
    }

    private fun respawnPlayer(player: Player) {
        player.velocity = Vector(0, 0, 0)
        player.clearInv()
        player.spigot().respawn()
        player.teleport(spawns.random().clone().add(0.0, 0.4, 0.0))
        player.heal()
        player.oitcEquip()
    }
}