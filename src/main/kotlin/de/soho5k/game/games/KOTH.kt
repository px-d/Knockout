package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.player.PlayerMode
import de.soho5k.utils.extensions.addPoints
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.kothEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.random.Random

/**
 * @author Hotkeyyy
 * @since 26.04.2021
 */
class KOTH : GameSchematic(
    "King of the Hill",
    "koth",
    Location(Bukkit.getWorld("koth"), 0.0, 70.0, 0.0),
    duration = 60,
    grace = 10,
    broadcast = 20
) {
    override fun onRejoin(player: Player) {
        player.mode = PlayerMode.PLAY
        player.kothEquip()
        player.teleport(spawn)
    }
    override fun onStart() {
        addEvent(CommonEvents().allEvents())
        addEvent(
            listen<PlayerInteractEvent> { if (it.action == Action.RIGHT_CLICK_BLOCK) it.isCancelled = true }
        )
        addEvent(
            listen<EntityDamageEvent> { it.damage = 0.0 }
        )

        onlinePlayers.filter { it.mode == PlayerMode.PLAY }
            .forEach { it.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = 100.0 }
    }


    override fun equip() {
        onlinePlayers.filter { it.mode == PlayerMode.PLAY }.forEach { it.kothEquip() }
    }


    override fun tick() {
        if (time % Random.nextInt(1, 6) == 0) {
            onlinePlayers.filter { it.mode == PlayerMode.PLAY }.forEach {
                if (it.player?.location?.clone()?.subtract(0.0, 1.0, 0.0)?.block?.type == Material.GOLD_BLOCK) {
                    it.addPoints(1, true)
                }
            }
        }
    }
}
