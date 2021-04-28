package de.soho5k.game.events

import de.soho5k.utils.gameListen
import net.axay.kspigot.event.SingleListener
import net.axay.kspigot.event.listen
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent

class CommonEvents(
    private val cancelFoodChange: Boolean = true,
    private val cancelBlockBreak: Boolean = true,
    private val cancelBlockPlace: Boolean = true,
    private val cancelItemDrop: Boolean = true,
    private val cancelItemPickup: Boolean = true
) : Listener {
    fun allEvents(): List<Listener> {
        return listOf(
            listen<FoodLevelChangeEvent> { it.isCancelled = cancelFoodChange },
            listen<BlockBreakEvent> { it.isCancelled = cancelBlockBreak },
            listen<BlockPlaceEvent> { it.isCancelled = cancelBlockPlace },
            listen<PlayerDropItemEvent> { it.isCancelled = cancelItemDrop },
            listen<EntityDropItemEvent> { it.isCancelled = cancelItemDrop },
            listen<EntityPickupItemEvent> { it.isCancelled = cancelItemPickup }
        )
    }
}