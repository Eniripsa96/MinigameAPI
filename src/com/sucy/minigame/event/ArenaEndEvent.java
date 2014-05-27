package com.sucy.minigame.event;

import com.sucy.minigame.arena.Arena;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a minigame arena ends
 */
public class ArenaEndEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final Arena arena;

    /**
     * Constructor
     *
     * @param arena   arena the player that left the arena
     */
    public ArenaEndEvent(Arena arena) {
        this.arena = arena;
    }

    /**
     * @return arena the player left
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * @return handler list for the event
     */
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    /**
     * @return handler list for the event
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
