package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.Localization.getLocalized
import de.soho5k.utils.extensions.addPoints
import de.soho5k.utils.extensions.amountOf
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.extensions.tc
import de.soho5k.utils.snowFightEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.task
import net.axay.kspigot.utils.hasMark
import net.axay.kspigot.utils.mark
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack

class Snowfight : GameSchematic(
    "Snowfight",
    "snowfight",
    Location(Bukkit.getWorld("snowfight"), 0.5, 65.0, 0.5),
    240,
    10,
    broadcast = 20
) {
    private val spawns = ConfigManager.getSpawns("snowfight")

    override fun equip() {
        onlinePlayers.filter { it.mode == PlayerMode.PLAY }.forEach { it.snowFightEquip() }
    }

    override fun onStart() {
        addEvent(
            listOf(
                listen<ProjectileHitEvent> {
                    if (it.hitEntity !is Player) return@listen
                    if ((it.hitEntity as Player).mode != PlayerMode.PLAY) return@listen
                    if (!it.entity.hasMark("snowball")) return@listen
                    if (it.entity.shooter is Player) respawnPlayer(it.hitEntity as Player, it.entity.shooter as Player)
                },
                listen<ProjectileLaunchEvent> {
                    if (it.entity is Snowball) it.entity.mark("snowball")
                },
                listen<CraftItemEvent> { it.isCancelled = true },
                listen<BlockBreakEvent> {
                    it.isCancelled = true
                    if (it.block.type == Material.SNOW_BLOCK || it.block.type == Material.SNOW) {
                        if (it.player.mode == PlayerMode.PLAY) {
                            if (it.player.amountOf(Material.SNOWBALL) < 16) it.player.inventory.addItem(
                                ItemStack(
                                    Material.SNOWBALL,
                                    1
                                )
                            )
                        }
                    }
                },

                listen<EntityDamageEvent> {
                    if (it.cause == EntityDamageEvent.DamageCause.FALL) it.isCancelled = true
                },
                listen<EntityDamageByEntityEvent> { if (it.entity is Player && it.damager is Player) it.damage = 0.0 }
            )
        )
        addEvent(CommonEvents(cancelBlockBreak = false).allEvents())
        addEvent(listen<EntityInteractEvent> {
            if (it.block.type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                it.entity.teleport(spawns.random())
            }
        })

        for (i in onlinePlayers.filter { it.mode == PlayerMode.PLAY }) {
            i.teleport(spawns.random())
        }
    }

    private fun respawnPlayer(player: Player, killer: Player) {
        if (player.uniqueId == killer.uniqueId) {
            player.addPoints(-2)
        } else {
            player.addPoints(-1)
            killer.addPoints(2)
        }
        player.gameMode = GameMode.SPECTATOR
        player.playSound(player.location, Sound.ENTITY_DONKEY_HURT, 1F, 1F)
        player.title("", "&e${player.getLocalized("TEXT.respawn_in").format("&c3&e")}".tc())
        task(delay = 60L) {
            player.gameMode = GameMode.SURVIVAL
            player.teleport(spawns.random())
        }
    }
}