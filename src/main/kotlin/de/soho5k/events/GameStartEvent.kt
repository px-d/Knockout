package de.soho5k.events

import de.soho5k.game.GameSchematic
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener


class GameStartEvent
/**
 * Called when a Minigame has started.
 *
 * @param game     The Game that has just started.
 * @param listener All Listeners that have been registered for the Game that just started.
 */(var game: GameSchematic, var listener: List<Listener>) :
    Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}
