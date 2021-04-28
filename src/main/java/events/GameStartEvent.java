package events;

import de.soho5k.game.GameSchematic;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GameStartEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    public GameSchematic game;
    public List<Listener> listener;

    /**
     * Called when a Minigame has started.
     *
     * @param game The Game that has just started.
     * @param listener All Listeners that have been registered for the Game that just started.
     */
    public GameStartEvent(GameSchematic game, List<Listener> listener) {
        this.game = game;
        this.listener = listener;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
