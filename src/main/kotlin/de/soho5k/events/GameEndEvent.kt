package de.soho5k.events

import de.soho5k.game.GameSchematic
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GameEndEvent
/**
 * Called when a Minigame has ended.
 *
 * @param game The Game that has been ended.
 */(var game: GameSchematic) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}