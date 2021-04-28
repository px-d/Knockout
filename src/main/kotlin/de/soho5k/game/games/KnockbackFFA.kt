package de.soho5k.game.games

import de.soho5k.game.events.CommonEvents
import de.soho5k.game.GameSchematic
import de.soho5k.knockout.ConfigManager
import de.soho5k.utils.extensions.addPoints
import de.soho5k.utils.extensions.killSound
import de.soho5k.utils.knockbackFFAEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class KnockbackFFA :
    GameSchematic("Knockback FFA", "kbffa", Location(Bukkit.getWorld("kbffa"), 0.0, 70.0, 0.0), 180, 10) {

    private val spawns = ConfigManager.getSpawns("kbffa")

    /**
     * Key: Player who got hit
     * Value: Who hit the Player
     */
    private val latestHits = hashMapOf<Player, Player>()

    override fun onStart() {
        addEvent(listen<PlayerMoveEvent> {
            if (it.player.location.y < 60) {
                respawnPlayer(it.player)
            }
        })

        addEvent(listen<EntityDamageByEntityEvent> {
            if (it.entity is Player && it.damager is Player) {
                latestHits[it.entity as Player] = it.damager as Player
            }
        })

        addEvent(listen<EntityDamageEvent> {
            if (it.entity is Player && it.cause == EntityDamageEvent.DamageCause.FALL) {
                it.isCancelled = true
            }
        })
        addEvent(CommonEvents().allEvents())
        addEvent(listen<EntityDamageEvent> { if (it.entity is Player) it.damage = 0.0 })
        addEvent(listen<PlayerToggleSneakEvent> { it.isCancelled = true })
    }

    override fun equip() {
        onlinePlayers.forEach {
            it.knockbackFFAEquip()
            it.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = 100.0
        }
    }

    private fun respawnPlayer(player: Player) {
        player.teleport(spawns.random())
        if (latestHits[player] != null) {
            latestHits[player]!!.addPoints(2)
            player.addPoints(-1)
            latestHits[player]!!.killSound()
            latestHits.remove(player)
        } else {
            player.addPoints(-4)
        }
    }
}