package de.soho5k.game.events

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.WorldInitEvent

class LobbyEvents : Listener {

    @EventHandler
    fun hit(e: EntityDamageEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        e.isCancelled = e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun food(e: FoodLevelChangeEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun interact(e: PlayerInteractEvent) {
        e.isCancelled = e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun worldInit(e: WorldInitEvent) {
        e.world.keepSpawnInMemory = false
    }

}