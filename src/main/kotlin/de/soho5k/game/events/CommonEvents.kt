package de.soho5k.game.events

import de.soho5k.utils.gameListen
import net.axay.kspigot.event.SingleListener
import net.axay.kspigot.event.listen
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.event.player.PlayerPickupItemEvent

class CommonEvents(
    private val cancelFoodChange: Boolean = true,
    private val cancelBlockBreak: Boolean = true,
    private val cancelBlockPlace: Boolean = true,
    private val cancelItemDrop: Boolean = true,
    private val cancelItemPickup: Boolean = true,
    private val cancelEntityDamage: Boolean = false
) : Listener {
    fun allEvents(): List<Listener> {
        return listOf(
            listen<FoodLevelChangeEvent> { if (cancelFoodChange) it.isCancelled = true },
            listen<BlockBreakEvent> { if (cancelBlockBreak) it.isCancelled = true },
            listen<BlockPlaceEvent> { if (cancelBlockPlace) it.isCancelled = true },
            listen<PlayerDropItemEvent> { if (cancelItemDrop) it.isCancelled = true },
            listen<EntityDropItemEvent> { if (cancelItemDrop) it.isCancelled = true },
            listen<EntityDamageEvent> { if (cancelEntityDamage) it.isCancelled = true },
            listen<EntityPickupItemEvent> { if (cancelItemPickup) it.isCancelled = true }
        )
    }
}