package com.sucy.minigame.event;

import com.sucy.minigame.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event for when a player attempts to join arena but could not
 * due to the arena being full, the current state not allowing it,
 * or the arena is just not fully set up.
 */
public class PlayerJoinFailedEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final Arena arena;
    private final Player player;

    /**
     * Constructor
     *
     * @param arena   arena the player that left the arena
     * @param player  player that left the arena
     */
    public PlayerJoinFailedEvent(Arena arena, Player player) {
        this.arena = arena;
        this.player = player;
    }

    /**
     * @return arena the player left
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * @return the player who left
     */
    public Player getPlayer() {
        return player;
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
