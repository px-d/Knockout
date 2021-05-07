package de.soho5k.game.games

import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.getOrSet
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.extensions.sendLocalizedMessage
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team

class RainbowRun : GameSchematic(
    "Rainbow Run",
    "rainbowrun",
    Location(Bukkit.getWorld("rainbowrun"), 0.5, 70.0, 0.5),
    120,
    10,
    broadcast = 20
) {
    private val placement = PointDistributor()

    private val borderT = setConfig("border_top", Location(spawn.world, 3.0, 72.0, 22.0)) as Location
    private val borderB = setConfig("border_bot", Location(spawn.world, 3.0, 70.0, -22.0)) as Location

    override fun onStart() {
        barrier(borderB, borderT, Material.WHITE_STAINED_GLASS, Material.AIR)
        addEvent(listen<PlayerMoveEvent> {
            if (it.player.isInWater && it.player.mode == PlayerMode.PLAY) {
                placement.addPlayer(it.player)
                PlayerManager.setToSpectator(it.player)
                val loc = spawn.clone()
                loc.z = it.player.location.z
                it.player.teleport(loc)
                if (PlayerManager.alivePlayers == 1) {
                    GameManager.activeGame.stop()
                }

                onlinePlayers.forEach { p ->
                    p.sendLocalizedMessage(
                        "MESSAGES.game.player_finished",
                        false,
                        ChatColor.RED,
                        ChatColor.GOLD,
                        it.player.name
                    )
                }
            }
            if (it.player.location.y <= 50) {
                val loc = spawn.clone()
                loc.z = it.player.location.z
                it.player.teleport(loc)
            }
        })
        addEvent(listen<EntityDamageByEntityEvent> {
            if (it.entity is Player && it.damager is Player) {
                it.isCancelled = true
            }
        })
        addEvent(listen<EntityDamageEvent> { it.isCancelled = it.entity is Player })
        addEvent(listen<BlockBreakEvent> { it.isCancelled = it.player.gameMode != GameMode.CREATIVE })
        addEvent(listen<BlockPlaceEvent> { it.isCancelled = it.player.gameMode != GameMode.CREATIVE })
        addEvent(listen<FoodLevelChangeEvent> { it.isCancelled = true })

        Knockout.team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        Knockout.team.setCanSeeFriendlyInvisibles(true)
        for (i in Bukkit.getOnlinePlayers()) {
            i.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false))
        }
    }

    override fun onStop() {
        placement.giveRewards(RewardMode.POSITION_THEN_X)
        barrier(borderB, borderT, Material.AIR, Material.WHITE_STAINED_GLASS)
    }
}