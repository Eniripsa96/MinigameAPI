package com.sucy.minigame.arena;

import com.sucy.minigame.plugin.ConfigValues;
import com.sucy.minigame.plugin.PluginData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

/**
 * A state for an arena.
 * Common examples are the lobby, pre-game, game, and death match.
 * Lobbies have a preset class to use.
 */
public abstract class ArenaState {

    protected static final List<String> DEFAULT_LINES = Arrays.asList("&4[%p]", "&2%n", "&1(%c / %x)", "&5In Progress");

    protected final Arena arena;
    protected final String name;

    protected List<String> signLines;
    protected BukkitRunnable task;
    protected boolean expTimer;
    protected boolean canJoin;
    protected int timeLimit;
    protected int timer;

    /**
     * <p>Creates an arena state that doesn't have a time limit.</p>
     * <p>The experience timer if enabled will count the elapsed time instead.</p>
     *
     * @param arena    arena reference
     * @param name     status name
     * @param canJoin  whether or not players can join during this state
     * @param expTimer whether or not to use an exp timer
     */
    public ArenaState(Arena arena, String name, boolean canJoin, boolean expTimer) {
        this(arena, name, canJoin, expTimer, 0);
    }

    /**
     * <p>Creates an arena state with a time limit.</p>
     * <p>The experience timer will show the time until the limit expires</p>
     *
     * @param arena     arena reference
     * @param name      status name
     * @param canJoin   whether or not players can join during this state
     * @param expTimer  whether or not to use an exp timer
     * @param timeLimit the duration the status should be active
     */
    public ArenaState(Arena arena, String name, boolean canJoin, boolean expTimer, int timeLimit) {
        this.arena = arena;
        this.name = name;
        this.canJoin = canJoin;
        this.timeLimit = timeLimit;
        this.expTimer = expTimer;
        signLines = DEFAULT_LINES;
    }

    /**
     * <p>Creates an arena from the configuration of an arena</p>
     * <p>This should not be directly called as an Arena will automatically use this
     * constructor to load the state from the config. Be sure to include your own
     * constructor using the same arguments that calls this by doing</p>
     * <code>
     *     public ExampleState(Arena arena, String name) {
     *         super(arena, name);
     *     }
     * </code>
     * <p>You should never use this constructor as it is meant to be used while reloading data.
     * Only use the other two constructors for creating states.</p>
     * <p>Any extra data you want to take from the config can be added onto that method
     * by just getting data from the config from getConfig().</p>
     *
     * @param arena arena to load from
     */
    public ArenaState(Arena arena, String name) {
        this.arena = arena;
        this.name = name;
        ConfigurationSection config = getConfig();

        // Load data
        canJoin = config.getBoolean(ConfigValues.CAN_JOIN);
        expTimer = config.getBoolean(ConfigValues.EXP_TIMER);
        timeLimit = config.getInt(ConfigValues.TIME_LIMIT);
        signLines = config.getStringList(ConfigValues.SIGN_LINES);
    }

    /**
     * @return owning arena
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * @return owning plugin data
     */
    public PluginData getPluginData() {
        return arena.getPluginData();
    }

    /**
     * @return game state name
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if players can join, false otherwise
     */
    public boolean canPlayersJoin() {
        return canJoin;
    }

    /**
     * Sets whether or not players can join during this state
     *
     * @param value whether or not players can join during this state
     */
    public void setCanPlayersJoin(boolean value) {
        canJoin = value;
    }

    /**
     * @return true if enabled, false otherwise
     */
    public boolean isTimerEnabled() {
        return expTimer;
    }

    /**
     * Sets the status of the exp timer
     *
     * @param value whether or not the timer should be enabled
     */
    public void setTimerEnabled(boolean value) {
        expTimer = value;
    }

    /**
     * @return time limit for the game state
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /**
     * @return true if the state has a time limit, false otherwise
     */
    public boolean hasTimeLimit() {
        return timeLimit > 0;
    }

    /**
     * Checks if the state is paused
     *
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return task == null;
    }

    /**
     * @return lines for the join sign
     */
    public List<String> getSignLines() {
        return signLines;
    }

    /**
     * <p>Sets the lines for the arena join sign for this state.</p>
     * <p>This accepts the & character for colors and uses a few filters
     * for dynamic data. The filters are:</p>
     * <p>%p = plugin name</p>
     * <p>%n = arena name</p>
     * <p>%x = maximum players</p>
     * <p>%c = current number of players</p>
     * <p>%s = state name</p>
     *
     * @param lines new sign lines
     */
    public void setSignLines(List<String> lines) {
        signLines = lines;

        // Update sign if this is the active state
        if (arena.getCurrentState() == this) {
            arena.updateSign();
        }
    }

    /**
     * Pauses the timer
     */
    public void pause() {

        // Must not already be paused
        if (!isPaused()) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Unpauses the timer
     */
    public void unpause() {

        // Must currently be paused
        if (isPaused()) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    updateTimer();
                }
            };
            task.runTaskTimer(arena.getPluginData().getPlugin(), 20, 20);
        }
    }

    /**
     * Sets the time limit for the game state
     */
    public void setTimeLimit(int limit) {
        timeLimit = limit;
    }

    /**
     * Starts the timer and launches starting functions
     */
    public void start() {
        timer = hasTimeLimit() ? timeLimit : 0;
        unpause();
        stateStarted();
    }

    /**
     * Stops the timer and launches ending functions
     */
    public void end() {
        pause();
        stateEnded();
    }

    /**
     * Updates the game timer, updating player levels if enabled
     * and ends the game if a time limit is set and reached.
     */
    public void updateTimer() {

        // Decrement time if there's a limit
        if (hasTimeLimit()) {
            timer = Math.max(timer - 1, 0);

            // Time expired
            if (timer == 0) {
                end();
            }
        }

        // Increment the timer if no limit
        else timer++;

        // Update exp bars if applicable
        if (expTimer) {
            for (ArenaPlayer player : arena.getPlayers()) {
                player.getPlayer().setLevel(timer);
            }
        }
    }

    /**
     * <p>Saves the basic state data to a config</p>
     * <p>You can override this to save additional data to the config.
     * In order to do so, save to the configuration section provided by
     * getConfig() and do not use any of the default nodes which include:</p>
     * <p>- class</p>
     * <p>- can-join</p>
     * <p>- exp-timer</p>
     * <p>- time-limit</p>
     * <p>- sign-lines</p>
     */
    public void save() {
        ConfigurationSection config = getConfig();

        config.set(ConfigValues.CLASS, this.getClass().getName());
        config.set(ConfigValues.CAN_JOIN, canJoin);
        config.set(ConfigValues.EXP_TIMER, expTimer);
        config.set(ConfigValues.TIME_LIMIT, timeLimit);
        config.set(ConfigValues.SIGN_LINES, signLines);
    }

    /**
     * @return configuration section of the arena state
     */
    public ConfigurationSection getConfig() {

        // Contains the section
        if (arena.getConfig().contains(ConfigValues.STATES + "." + name)) {
            return arena.getConfig().getConfigurationSection(ConfigValues.STATES + "." + name);
        }

        // Doesn't contain the section
        else {
            return arena.getConfig().createSection(ConfigValues.STATES + "." + name);
        }
    }

    /**
     * Applies game logic when the game state starts
     * such as preparing required mechanics
     */
    public abstract void stateStarted();

    /**
     * Applies game logic when the game state ends
     * such as cleaning up persisting mechanics
     */
    public abstract void stateEnded();

    /**
     * Applies game logic when a player joined the arena
     * generally for starting lobbies
     *
     * @param player player who joined the arena
     */
    public abstract void playerJoined(ArenaPlayer player);

    /**
     * Applies game logic when a player left the arena
     * such as ending the game when there are too few players
     *
     * @param player player who left the game
     */
    public abstract void playerLeft(ArenaPlayer player);

    /**
     * Applies game logic when a player dies while in the arena
     * such as checking if the game is over
     *
     * @param player player who died in the arena
     */
    public abstract void playerDied(ArenaPlayer player);

    /**
     * Applies game logic when a player respawns while in the game
     * such as setting the respawn location
     *
     * @param player player who respawned
     */
    public abstract void playerRespawned(ArenaPlayer player);
}
