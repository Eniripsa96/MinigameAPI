package com.sucy.minigame.arena;

import com.sucy.minigame.util.DataParser;
import com.sucy.minigame.event.ArenaEndEvent;
import com.sucy.minigame.event.ArenaStartEvent;
import com.sucy.minigame.event.PlayerJoinArenaEvent;
import com.sucy.minigame.event.PlayerLeaveArenaEvent;
import com.sucy.minigame.plugin.ConfigValues;
import com.sucy.minigame.plugin.PluginData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;

/**
 * <p>An arena instance for a mini-game</p>
 * <p>This is meant for handling the players and states of the arena. Actual game functionality
 * is located in the ArenaState objects which is why you cannot extend this class but you need
 * to extend ArenaState.</p>
 */
public final class Arena {

    private final HashMap<String, ArenaState> arenaStates = new HashMap<String, ArenaState>();
    private final HashMap<String, ArenaPlayer> players = new HashMap<String, ArenaPlayer>();
    private final HashMap<String, ArenaTeam> teams = new HashMap<String, ArenaTeam>();

    private final PluginData plugin;
    private final ArenaLobby lobby;
    private final String name;

    private ArenaState currentState;
    private ArenaState startState;
    private ArenaTeam defaultTeam;
    private Location signLoc;
    private Location lobbyLoc;
    private int maxPlayers;

    /**
     * <p>Creates an arena with no time limit for the lobby</p>
     * <p>Do not use this constructor. Instead, create an arena through
     * your plugin data using this method:</p>
     * <code>
     *     PluginData.createArena(String, int, int)
     * </code>
     *
     * @param plugin     plugin data
     * @param name       arena name
     * @param maxPlayers maximum players allowed in the arena
     * @param minPlayers minimum players needed to start a game
     */
    public Arena(PluginData plugin, String name, int maxPlayers, int minPlayers) {
        this(plugin, name, maxPlayers, minPlayers, 0);
    }

    /**
     * <p>Creates an arena with a lobby time limit</p>
     * <p>Do not use this constructor. Instead, create an arena through
     * your plugin data using this method:</p>
     * <code>
     *     PluginData.createArena(String, int, int, int)
     * </code>
     *
     * @param plugin         plugin data
     * @param name           arena name
     * @param maxPlayers     maximum players allowed in the arena
     * @param minPlayers     minimum players needed to start a game
     * @param lobbyTimeLimit time limit for the lobby
     */
    public Arena(PluginData plugin, String name, int maxPlayers, int minPlayers, int lobbyTimeLimit) {
        this.plugin = plugin;
        this.name = name;
        this.maxPlayers = maxPlayers;

        lobby = new ArenaLobby(this, lobbyTimeLimit, minPlayers);
        arenaStates.put(lobby.getName().toLowerCase(), lobby);
        this.currentState = lobby;
        save();
    }

    /**
     * <p>Config constructor.</p>
     * <p>This constructor is strictly for API use while reloading plugin data.
     * You should never call this constructor yourself.</p>
     *
     * @param plugin plugin to load from
     * @param name   arena name
     */
    public Arena(PluginData plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        // Get basic values
        ConfigurationSection config = getConfig();
        this.maxPlayers = config.getInt(ConfigValues.MAX_PLAYERS);

        // Load teams
        if (config.contains(ConfigValues.TEAMS)) {
            for (String team : config.getConfigurationSection(ConfigValues.TEAMS).getKeys(false)) {
                teams.put(team.toLowerCase(), new ArenaTeam(this, team));
            }
        }

        // Load states
        for (String state : config.getConfigurationSection(ConfigValues.STATES).getKeys(false)) {
            ConfigurationSection stateConfig = config.getConfigurationSection(ConfigValues.STATES + "." + state);
            try {
                String className = stateConfig.getString(ConfigValues.CLASS);
                Constructor constructor = Class.forName(className).getDeclaredConstructor(Arena.class, String.class);
                ArenaState arenaState = (ArenaState)constructor.newInstance(this, state);
                arenaStates.put(state.toLowerCase(), arenaState);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                plugin.getPlugin().getLogger().severe("Failed to load arena state: " + state);
            }
        }
        lobby = (ArenaLobby)getState(ArenaLobby.STATE_NAME);
        currentState = lobby;

        // Sign location
        if (config.contains(ConfigValues.SIGN_LOC)) {
            signLoc = DataParser.parseLocation(config.getString(ConfigValues.SIGN_LOC));
        }

        // Lobby location
        if (config.contains(ConfigValues.LOBBY_SPAWN)) {
            lobbyLoc = DataParser.parseLocation(config.getString(ConfigValues.LOBBY_SPAWN));
        }

        // Starting game state
        if (config.contains(ConfigValues.START_STATE)) {
            startState = getState(config.getString(ConfigValues.START_STATE));
        }

        // Default team
        if (config.contains(ConfigValues.DEFAULT_TEAM)) {
            defaultTeam = getTeam(config.getString(ConfigValues.DEFAULT_TEAM));
        }
    }

    /**
     * <p>Checks if the arena is ready to start accepting players to start a game</p>
     * <p>The factors this method takes into consideration is if the lobby, initial
     * game state, and default teams are all set.</p>
     * <p>To ensure that all of your game states and teams are added before a game is
     * started, use the event ArenaStartEvent and cancel it if anything is missing.</p>
     *
     * @return true if the arena is ready to start a game, false otherwise
     */
    public boolean canStartGame() {
        return lobbyLoc != null
                && startState != null
                && defaultTeam != null;
    }

    /**
     * <p>Checks if there are enough players to start a match. This can be used before or during
     * a game to see if a match should be started or ended.</p>
     *
     * @return true if enough players are present in the arena to play a match, false otherwise
     */
    public boolean hasEnoughPlayers() {
        return lobby.getMinPlayers() <= players.size();
    }

    /**
     * @return owning plugin data
     */
    public PluginData getPluginData() {
        return plugin;
    }

    /**
     * @return arena name;
     */
    public String getName() {
        return name;
    }

    /**
     * @return arena state for the lobby
     */
    public ArenaLobby getLobbyState() {
        return lobby;
    }

    /**
     * @return lobby spawn location
     */
    public Location getLobbyLoc() {
        return lobbyLoc;
    }

    /**
     * @return join sign location
     */
    public Location getSignLoc() {
        return signLoc;
    }

    /**
     * @return current state of the arena
     */
    public ArenaState getCurrentState() {
        return currentState;
    }

    /**
     * @return true if players can join, false otherwise
     */
    public boolean canPlayersJoin() {
        return currentState.canPlayersJoin();
    }

    /**
     * @return maximum number of players allowed in the arena at once
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Retrieves an arena state from the arena
     *
     * @param name arena state name
     * @return     arena state or null if not found
     */
    public ArenaState getState(String name) {
        return arenaStates.get(name.toLowerCase());
    }

    /**
     * Retrieves a team from the arena
     *
     * @param name team name
     * @return     arena team
     */
    public ArenaTeam getTeam(String name) {
        return teams.get(name.toLowerCase());
    }

    /**
     * Retrieves a player in the arena
     *
     * @param name player name
     * @return     player in the arena
     */
    public ArenaPlayer getPlayer(String name) {
        return players.get(name.toLowerCase());
    }

    /**
     * @return collection of all players in the arena
     */
    public Collection<ArenaPlayer> getPlayers() {
        return players.values();
    }

    /**
     * Sets the lobby spawn location for the arena
     *
     * @param loc new spawn location
     */
    public void setLobbyLoc(Location loc) {
        lobbyLoc = loc;
    }

    /**
     * Sets the location of the join sign and updates the sign
     *
     * @param loc sign location
     * @return    true if location pointed to a sign and could be set, false otherwise
     */
    public boolean setSignLoc(Location loc) {
        if (loc == null) {
            signLoc = null;
            return true;
        }
        else if (loc.getBlock().getType() == Material.SIGN_POST || loc.getBlock().getType() == Material.WALL_SIGN) {
            signLoc = loc;
            updateSign();
            return true;
        }
        else return false;
    }

    /**
     * <p>Adds a player to the arena</p>
     * <br/>
     * <p>Before actually adding the player, it checks a few things in this order:</p>
     * <p>- The default team must be set</p>
     * <p>- The current state must allow players to join</p>
     * <p>- A join event is fired and can cancel this method</p>
     * <p>- There must not be too many players</p>
     * <br/>
     * <p>This order allows you to make vip players to kick someone else out to join
     * a full lobby. this can be done by listening on the event and removing a player
     * when the joining player has the permission and the lobby is full.</p>
     * <br/>
     * <p>This method does several things:</p>
     * <p>- Backs up inventory, game mode, level, experience, and location</p>
     * <p>- Sets game mode to survival</p>
     * <p>- Sets level and experience to 0</p>
     * <p>- Clears their inventory</p>
     * <p>- Assigns the player to the default team</p>
     *
     * @param player player to add to the arena
     * @return       the arena data for the player or null if unable to add the player
     */
    public ArenaPlayer addPlayer(Player player) {

        // There needs to be a default team to put the player on
        if (defaultTeam != null && currentState.canPlayersJoin()) {

            // Run the event to make sure they can join
            PlayerJoinArenaEvent event = new PlayerJoinArenaEvent(this, player);
            plugin.getPlugin().getServer().getPluginManager().callEvent(event);

            // Cancelled event
            if (event.isCancelled()) {
                return null;
            }

            // Too many players
            if (players.size() >= maxPlayers) {
                return null;
            }

            // Add the players
            ArenaPlayer arenaPlayer = new ArenaPlayer(this, player);
            players.put(player.getName().toLowerCase(), arenaPlayer);
            player.teleport(lobbyLoc);
            defaultTeam.addMember(arenaPlayer);
            updateSign();
            return arenaPlayer;
        }

        // Cannot add the player
        else {
            return null;
        }
    }

    /**
     * <p>Removes a player from the arena</p>
     * <p>This only removes them from the map in the arena and will not
     * restore the player to before they entered. Because of this, to remove
     * a player, you should instead use ArenaPlayer.leaveArena() to remove
     * a player from an arena.</p>
     * <p>Because of this, this method should not be used in your plugin.</p>
     *
     * @param player player to remove
     */
    public void removePlayer(ArenaPlayer player) {
        if (players.containsKey(player.getPlayer().getName().toLowerCase())) {
            players.remove(player.getPlayer().getName().toLowerCase());
            currentState.playerLeft(player);
            updateSign();

            // Launch an event
            PlayerLeaveArenaEvent event = new PlayerLeaveArenaEvent(this, player.getPlayer());
            plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        }
    }

    /**
     * <p>Removes all players from the arena</p>
     * <p>This method does launch the leave event for each and every
     * player that was in the arena.</p>
     */
    public void removeAllPlayers() {
        for (ArenaPlayer player : players.values()) {
            player.leaveArena();
        }
    }

    /**
     * <p>Starts the arena game by transitioning to the starting
     * state and moving all players to their team spawn location.</p>
     * <p>This method does nothing if the arena is not ready to start a game
     * due to the arena not being fully set up, not being in the lobby state,
     * or not having enough players.</p>
     * <p>This method can be cancelled by the start event it launches</p>
     */
    public void startGame() {

        // Do not start if not able to
        if (!canStartGame() || currentState != lobby || lobby.getMinPlayers() > players.size()) {
            return;
        }

        // Call Event
        ArenaStartEvent event = new ArenaStartEvent(this);
        plugin.getPlugin().getServer().getPluginManager().callEvent(event);

        // Don't start if cancelled
        if (event.isCancelled()) {
            return;
        }

        // Transition to the starting state
        currentState.pause();
        currentState = startState;
        startState.start();

        // Move players
        for (ArenaTeam team : teams.values()) {
            team.spawnPlayers();
        }
    }

    /**
     * Ends the current arena game by transitioning to the lobby
     * and removing all players from the arena
     */
    public void endGame() {

        // Cannot be in the lobby state already
        if (currentState != lobby) {
            currentState.end();
            removeAllPlayers();
            currentState = lobby;
            currentState.start();

            // Call an event
            ArenaEndEvent event = new ArenaEndEvent(this);
            plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        }
    }

    /**
     * Sets the active state of the arena without ending the current state.
     * If you want the current state to perform ending functions, simply call
     * it on the current state before calling this method.
     *
     * @param state state to transition to
     * @return      true if successfully transitioned, false otherwise
     */
    public boolean setState(ArenaState state) {

        // Must be a state of this arena
        if (state != null && state.getArena() == this) {
            currentState.pause();
            currentState = state;
            state.start();
            return true;
        }

        // Wasn't a valid state
        else {
            return false;
        }
    }

    /**
     * Adds a new game state to the arena
     *
     * @param state arena state to add
     * @param start whether or not the state is the first state of the game
     * @return      true if successfully added
     */
    public boolean addState(ArenaState state, boolean start) {

        // State doesn't exist or is already added
        if (state == null || arenaStates.containsKey(state.getName())) {
            return false;
        }

        // Add it to the map
        arenaStates.put(state.getName(), state);

        // Make it the starting arena state
        if (start) {
            this.startState = state;
        }

        return true;
    }

    /**
     * Adds a team to the arena
     *
     * @param team        name of team to add
     * @param spawn       spawn location for the team
     * @param defaultTeam whether or not this is the default team
     * @return            team that was added or null if unable to add the team
     */
    public ArenaTeam addTeam(String team, Location spawn, boolean defaultTeam) {

        // Cannot already have a team with that name
        if (!teams.containsKey(team.toLowerCase())) {
            ArenaTeam arenaTeam = new ArenaTeam(this, team, spawn);
            teams.put(team.toLowerCase(), arenaTeam);

            // Set it as the default team if not already set
            if (defaultTeam) {
                this.defaultTeam = arenaTeam;
            }

            return arenaTeam;
        }

        // Unable to add the team
        else {
            return null;
        }
    }

    /**
     * <p>Updates the join sign with the current state's details</p>
     * <p>If the sign location no longer points to a sign, the location will
     * be unregistered instead</p>
     */
    public void updateSign() {

        // Requires a sign to be set
        if (signLoc != null) {
            BlockState state = signLoc.getBlock().getState();

            // Not a valid sign
            if (state == null || !(state instanceof Sign)) {
                signLoc = null;
                return;
            }

            Sign sign = (Sign)state;
            for (int i = 0; i < 4 && i < currentState.getSignLines().size(); i++) {
                String line = currentState.getSignLines().get(i);
                line = line.replace("%p", plugin.getPlugin().getName());
                line = line.replace("%n", name);
                line = line.replace("%x", maxPlayers + "");
                line = line.replace("%c", players.size() + "");
                line = line.replace("%s", currentState.getName());
                line = line.replace('&', ChatColor.COLOR_CHAR);
                sign.setLine(i, line);
            }
            sign.update();
        }
    }

    /**
     * <p>Saves the initial data for the arena.</p>
     * <p>If you want to save your own data to the config, override this
     * method, call the super implementation, and then save values to the
     * config provided by getConfig(). An example:</p>
     * <code>
     *     \@Override
     *     public void save() {
     *         super.save();
     *         getConfig().set("myData", dataValue);
     *     }
     * </code>
     */
    public void save() {
        ConfigurationSection config = getConfig();

        // Definite values
        config.set(ConfigValues.NAME, name);
        config.set(ConfigValues.MAX_PLAYERS, maxPlayers);
        config.set(ConfigValues.CURRENT_STATE, currentState.getName());

        // Starting game state
        if (startState != null) {
            config.set(ConfigValues.START_STATE, startState.getName());
        }

        // Sign location
        config.set(ConfigValues.SIGN_LOC, DataParser.serializeLocation(signLoc));

        // Lobby location
        config.set(ConfigValues.LOBBY_SPAWN, DataParser.serializeLocation(lobbyLoc));

        // Default team
        if (defaultTeam != null) {
            config.set(ConfigValues.DEFAULT_TEAM, defaultTeam.getName());
        }

        // Teams
        for (ArenaTeam team : teams.values()) {
            team.save();
        }

        // States
        for (ArenaState state : arenaStates.values()) {
            state.save();
        }
    }

    /**
     * <p>Retrieves the configuration section for this arena</p>
     * <p>This is a sub-section of the plugin data's configuration</p>
     *
     * @return configuration section
     */
    public ConfigurationSection getConfig() {

        // Section exists
        if (plugin.getConfig().contains(name)) {
            return plugin.getConfig().getConfigurationSection(name);
        }

        // Section doesn't exist
        else {
            return plugin.getConfig().createSection(name);
        }
    }
}
