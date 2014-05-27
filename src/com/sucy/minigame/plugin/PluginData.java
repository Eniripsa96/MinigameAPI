package com.sucy.minigame.plugin;

import com.sucy.minigame.MinigameAPI;
import com.sucy.minigame.arena.Arena;
import com.sucy.minigame.util.Config;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;

/**
 * A wrapper for plugins to provide extra data and configuration helper methods
 */
public final class PluginData {

    private final HashMap<String, Arena> arenas = new HashMap<String, Arena>();

    private final MinigameAPI api;
    private final JavaPlugin plugin;
    private final Config config;

    /**
     * Creates a wrapper for a plugin with needed data
     *
     * @param plugin  plugin reference
     */
    public PluginData(MinigameAPI api, JavaPlugin plugin) {
        this.api = api;
        this.plugin = plugin;
        this.config = new Config(plugin, "arena-data");
    }

    /**
     * @return MinigameAPI reference
     */
    public MinigameAPI getApi() {
        return api;
    }

    /**
     * @return plugin owning the data
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * @return collection of all registered arenas for this plugin
     */
    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    /**
     * Deletes all plugin data
     */
    public void deleteData() {
        config.getConfig().set("", null);
        config.saveConfig();
    }

    /**
     * @return config file for the plugin arena data
     */
    public ConfigurationSection getConfig() {
        return config.getConfig();
    }

    /**
     * Loads the plugin data
     */
    public void loadData() {
        for (String key : config.getConfig().getKeys(false)) {
            Arena arena = new Arena(this, key);
            arenas.put(key.toLowerCase(), arena);
        }
    }

    /**
     * Retrieves an arena by name
     *
     * @param name arena name
     * @return     arena or null if not found
     */
    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    /**
     * Retrieves the arena that the player is in or null if not found
     *
     * @param player player to search for
     * @return       arena the player is in
     */
    public Arena getArena(Player player) {
        for (Arena arena : arenas.values()) {
            if (arena.getPlayer(player.getName()) != null) {
                return arena;
            }
        }
        return null;
    }

    /**
     * Retrieves the arena with a join sign at the given location
     *
     * @param signLoc location of join sign
     * @return        arena with the join sign or null if not found
     */
    public Arena getArena(Location signLoc) {
        for (Arena arena : arenas.values()) {
            if (arena.getSignLoc() != null && arena.getSignLoc().equals(signLoc)) {
                return arena;
            }
        }
        return null;
    }

    /**
     * <p>Registers a new arena for the plugin without a lobby time limit</p>
     * <p>This should be followed up by you adding the needed game states to
     * the arena. You can add teams with a default spawn location as well,
     * although generally you can just keep that with separate commands
     * for setting the spawn point of those teams.</p>
     *
     * @param name       arena name
     * @param maxPlayers maximum allowed players
     * @param minPlayers minimum allowed players
     * @return           created arena
     */
    public Arena createArena(String name, int maxPlayers, int minPlayers) {
        return createArena(name, maxPlayers, minPlayers, 0);
    }

    /**
     * <p>Registers a new arena for the plugin with a lobby time limit</p>
     * <p>This should be followed up by you adding the needed game states to
     * the arena. You can add teams with a default spawn location as well,
     * although generally you can just keep that with separate commands
     * for setting the spawn point of those teams.</p>
     *
     * @param name           arena name
     * @param maxPlayers     maximum allowed players
     * @param minPlayers     minimum allowed players
     * @param lobbyTimeLimit time before the game starts
     * @return               created arena
     */
    public Arena createArena(String name, int maxPlayers, int minPlayers, int lobbyTimeLimit) {

        // Name taken
        if (arenas.containsKey(name.toLowerCase())) {
            return null;
        }

        // Create the arena
        Arena arena = new Arena(this, name, maxPlayers, minPlayers, lobbyTimeLimit);
        arenas.put(name.toLowerCase(), arena);
        return arena;
    }

    /**
     * Unregisters an arena
     *
     * @param name arena name
     * @return     Arena that was removed or null if not found
     */
    public Arena deleteArena(String name) {

        // Unregister it
        Arena arena = arenas.remove(name.toLowerCase());

        // Remove it from config if present
        if (arena != null) {
            config.getConfig().set(arena.getName(), null);
        }

        return arena;
    }

    /**
     * Saves the plugin data
     */
    public void save() {
        for (Arena arena : arenas.values()) {
            arena.save();
        }
        config.saveConfig();
    }
}
