package com.sucy.minigame.event;

import com.sucy.minigame.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <p>Event for players joining an arena</p>
 * <p>Can be cancelled to deny a player access to an arena</p>
 */
public class PlayerJoinArenaEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();

    private final Arena arena;
    private final Player player;

    private boolean cancelled;

    /**
     * Constructor
     *
     * @param arena   arena the player is trying to join
     * @param player  player trying to join the arena
     */
    public PlayerJoinArenaEvent(Arena arena, Player player) {
        this.arena = arena;
        this.player = player;
        this.cancelled = false;
    }

    /**
     * @return arena the player is trying to join
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * @return the player trying to join the arena
     */
    public Player getPlayer() {
        return player;
    }

     /**
     * @return whether or not the event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of the event
     *
     * @param b new cancelled state
     */
    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
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
