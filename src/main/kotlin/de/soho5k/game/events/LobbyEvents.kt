package de.soho5k.game.events

import de.soho5k.knockout.Knockout
import de.soho5k.knockout.managers.ConfigManager
import net.axay.kspigot.extensions.bukkit.isFeetInWater
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.util.Vector

class LobbyEvents : Listener {

    @EventHandler
    fun hit(e: EntityDamageEvent) {
        if (e.entity is Player) e.isCancelled = true
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        if (e.player.gameMode != GameMode.CREATIVE) e.isCancelled = true
    }

    @EventHandler
    fun onDrop(e: PlayerDropItemEvent) {
        if (e.player.gameMode != GameMode.CREATIVE) e.isCancelled = true
    }

    @EventHandler
    fun food(e: FoodLevelChangeEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun interact(e: PlayerInteractEvent) {
        if (e.player.gameMode == GameMode.SURVIVAL || e.player.gameMode == GameMode.ADVENTURE) {
            e.isCancelled = true
            if (e.action == Action.PHYSICAL) {
                if (e.clickedBlock?.type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                    e.player.velocity = e.player.location.direction.multiply(2.0).setY(0.9)
                }
                if (e.clickedBlock?.type == Material.WARPED_PRESSURE_PLATE) {
                    e.player.velocity = Vector(0.0, 4.0, 0.0)
                }
                if (e.clickedBlock?.type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                    e.player.velocity = e.player.location.direction.multiply(4.0).setY(1.2)
                }
            }
        }
    }

    @EventHandler
    fun move(e: PlayerMoveEvent) {
        if (e.player.isFeetInWater) {
            e.player.teleport(ConfigManager.lobbyLoc)
        }
    }

    @EventHandler
    fun worldInit(e: WorldInitEvent) {
        e.world.keepSpawnInMemory = false
    }

}