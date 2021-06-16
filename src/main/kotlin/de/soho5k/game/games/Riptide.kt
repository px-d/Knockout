package de.soho5k.game.games

import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.riptideEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team

class Riptide : GameSchematic("Riptide", "riptide", Location(Bukkit.getWorld("riptide"), 0.5, 71.0, 0.5), 90, 5) {

    private val barrierBlocks = arrayListOf<Location>()
    private val placement = PointDistributor()

    init {
        val radius = 10
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = spawn.world?.getBlockAt(
                        spawn.blockX + x,
                        spawn.blockY + y,
                        spawn.blockZ + z
                    )
                    if (block?.type == Material.WHITE_STAINED_GLASS) {
                        barrierBlocks.add(block.location.clone())
                    }
                }
            }
        }
    }

    override fun equip() {
        onlinePlayers.filter { it.mode == PlayerMode.PLAY }.forEach { equipPlayer(it) }
    }


    override fun equipPlayer(player: Player) {
        player.riptideEquip()
    }

    override fun onRejoin(player: Player) {
        player.mode = PlayerMode.PLAY
        equipPlayer(player)
        player.teleport(spawn)
    }

    override fun onStart() {
        addEvent(CommonEvents(cancelItemDrop = false).allEvents())
        addEvent(listen<PlayerInteractEvent> {
            if (it.action == Action.PHYSICAL) {
                if (it.clickedBlock?.type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                    println("Dogshit")
                }
            }
        })

        addEvent(listen<PlayerDropItemEvent> {
            it.isCancelled = it.player.gameMode != GameMode.CREATIVE
        })

        addEvent(listen<EntityDamageEvent> {
            it.isCancelled = it.entity is Player
        })

        addEvent(listen<PlayerMoveEvent> {
            if (it.player.location.y < 60) it.player.teleport(spawn)
        })

        Knockout.team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        Knockout.team.setCanSeeFriendlyInvisibles(true)
        onlinePlayers.forEach {
            it.isCollidable = false
            it.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false))
        }

        barrierBlocks.forEach { it.block.type = Material.AIR }
    }

    override fun onStop() {
        for (i in barrierBlocks) i.block.type = Material.WHITE_STAINED_GLASS
        placement.giveRewards(RewardMode.POSITION_THEN_Z)
    }
}