package com.sucy.minigame.arena;

import com.sucy.minigame.plugin.ConfigValues;

import java.util.List;

/**
 * <p>The lobby state for all arenas</p>
 * <p>To extend the functionality of a lobby, use the events for
 * starting a game and players joining/leaving.</p>
 */
public final class ArenaLobby extends ArenaState {

    public static final String STATE_NAME = "Lobby";

    private int minPlayers;

    /**
     * <p>Creates a lobby state</p>
     * <p>You should not ever have to use this constructor
     * as creating an arena automatically adds a lobby state
     * to it. To access it, use Arena.getLobbyState().</p>
     *
     * @param arena     arena reference
     * @param timeLimit time limit
     */
    public ArenaLobby(Arena arena, int timeLimit, int minPlayers) {
        super(arena, STATE_NAME, true, true, timeLimit);
        this.minPlayers = minPlayers;

        // Modify sign for a lobby
        List<String> lines = getSignLines();
        lines.set(3, "&5Lobby");
        setSignLines(lines);
    }

    /**
     * <p>Config constructor</p>
     * <p>Do not use this constructor as it is strictly for
     * reloading the lobby details from the config.</p>
     *
     * @param arena parent arena
     * @param name  state name
     */
    public ArenaLobby(Arena arena, String name) {
        super(arena, name);
        this.minPlayers = getConfig().getInt(ConfigValues.MIN_PLAYERS);
    }

    /**
     * @return minimum players required to start
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Resets the timer if there's not enough players
     */
    @Override
    public void updateTimer() {
        if (getArena().getPlayers().size() < minPlayers) {
            timer = timeLimit + 1;
        }

        super.updateTimer();
    }

    /**
     * Applies game logic when the game state starts
     * such as preparing required mechanics
     */
    @Override
    public void stateStarted() { }

    /**
     * Applies game logic when the game state ends
     * such as cleaning up persisting mechanics
     */
    @Override
    public void stateEnded() {
        getArena().startGame();
    }

    /**
     * Applies game logic when a player joined the arena
     * generally for starting lobbies
     *
     * @param player player who joined the arena
     */
    public void playerJoined(ArenaPlayer player) {

    }

    /**
     * Applies game logic when a player left the arena
     * such as ending the game when there are too few players
     *
     * @param player player who left the game
     */
    public void playerLeft(ArenaPlayer player) { }

    /**
     * Applies game logic when a player dies while in the arena
     * such as checking if the game is over
     *
     * @param player player who died in the arena
     */
    public void playerDied(ArenaPlayer player) { }

    /**
     * Applies game logic when a player respawns while in the game
     * such as setting the respawn location
     *
     * @param player player who respawned
     */
    public void playerRespawned(ArenaPlayer player) {
    }

    /**
     * Saves the minimum players along with the normal data
     */
    @Override
    public void save() {
        super.save();
        getConfig().set(ConfigValues.MIN_PLAYERS, minPlayers);
    }
}
