package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.extensions.addPoints
import de.soho5k.utils.extensions.amountOf
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.snowFightEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
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
                    if (!it.entity.hasMetadata("snowball")) return@listen
                    if (it.entity.shooter is Player) respawnPlayer(it.hitEntity as Player, it.entity.shooter as Player)
                },
                listen<ProjectileLaunchEvent> {
                    if (it.entity is Snowball) it.entity.customName = "snowBall"
                },
                listen<BlockBreakEvent> {
                    it.isCancelled = true
                    if (it.block.type == Material.SNOW_BLOCK) {
                        if (it.player.mode == PlayerMode.PLAY) {
                            if (it.player.amountOf(Material.SNOWBALL) < 16) it.player.inventory.addItem(ItemStack(Material.SNOWBALL, 1))
                        }
                    }
                },
                listen<CraftItemEvent> { it.isCancelled = true },
            )
        )
        addEvent(CommonEvents(cancelBlockBreak = false).allEvents())
    }

    private fun respawnPlayer(player: Player, killer: Player) {
        player.addPoints(-1)
        killer.addPoints(2)
        player.playSound(player.location, Sound.ENTITY_DONKEY_HURT, 1F, 1F)
        player.teleport(spawns.random())
    }

}