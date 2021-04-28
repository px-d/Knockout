package de.soho5k.utils

import net.axay.kspigot.event.SingleListener
import org.bukkit.event.EventPriority

inline fun <reified T : org.bukkit.event.Event> gameListen(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    crossinline onEvent: (event: T) -> Unit
): SingleListener<T> {
    return object : SingleListener<T> {
        override fun onEvent(event: T) = onEvent.invoke(event)
    }
}
