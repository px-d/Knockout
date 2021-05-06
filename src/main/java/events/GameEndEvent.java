package events;

import de.soho5k.game.GameSchematic;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameEndEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    public GameSchematic game;

    /**
     * Called when a Minigame has ended.
     *
     * @param game The Game that has been ended.
     */
    public GameEndEvent(GameSchematic game) {
        this.game = game;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
