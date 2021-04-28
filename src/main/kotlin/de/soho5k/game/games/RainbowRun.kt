package de.soho5k.game.games

import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.mode
import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
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
import java.lang.Exception

class RainbowRun : GameSchematic(
    "Rainbow Run",
    "rainbowrun",
    Location(Bukkit.getWorld("rainbowrun"), 0.5, 70.0, 0.5),
    120,
    10,
    broadcast = 20
) {

    private val border = arrayListOf<Location>()
    private val placement = PointDistributor()

    override fun onStart() {
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

        val radius = 50
        val world = spawn.world ?: throw Exception("World Not Existing")
        val loc = spawn

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = world.getBlockAt(
                        loc.blockX + x,
                        loc.blockY + y,
                        loc.blockZ + z
                    )
                    if (block.type == Material.WHITE_STAINED_GLASS) {
                        border.add(block.location.clone())
                    }
                }
            }
        }

        for (i in border) {
            i.block.type = Material.AIR
        }

        Knockout.team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        Knockout.team.setCanSeeFriendlyInvisibles(true)
        for (i in Bukkit.getOnlinePlayers()) {
            i.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false))
        }
    }

    override fun onStop() {
        for (i in border) i.block.type = Material.WHITE_STAINED_GLASS
        placement.giveRewards(RewardMode.POSITION_THEN_X)
    }
}