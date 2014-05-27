package com.sucy.minigame.event;

import com.sucy.minigame.arena.Arena;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a minigame arena is starting
 */
public class ArenaStartEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();

    private final Arena arena;

    private boolean cancelled;

    /**
     * Constructor
     *
     * @param arena   arena the player that left the arena
     */
    public ArenaStartEvent(Arena arena) {
        this.arena = arena;
        cancelled = false;
    }

    /**
     * @return arena the player left
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * @return whether or not the event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether or not the event is cancelled
     *
     * @param value whether or not the event should be cancelled
     */
    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
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
