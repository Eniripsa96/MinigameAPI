package com.sucy.minigame;

import com.sucy.minigame.arena.Arena;
import com.sucy.minigame.plugin.PluginData;
import com.sucy.minigame.util.DataParser;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/**
 * MinigameAPI
 * Provides a flexible framework for creating mini-games easily
 *
 * Developed by Steven Sucy (Eniripsa96)
 */
public class MinigameAPI extends JavaPlugin {

    private final HashMap<String, PluginData> plugins = new HashMap<String, PluginData>();

    /**
     * Sets up the listener
     */
    @Override
    public void onEnable() {
        new MinigameListener(this);
    }

    /**
     * Saves all arena data and ends all games before closing
     */
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        for (PluginData data : plugins.values()) {
            for (Arena arena : data.getArenas()) {
                arena.endGame();
                arena.removeAllPlayers();
            }
            data.save();
        }
    }

    /**
     * <p>Retrieves an arena by its join sign location</p>
     * <p>The arena returned by this method can be from any plugin</p>
     *
     * @param signLoc location of a sign
     * @return        arena attached to the sign or null if not found
     */
    public Arena getArena(Location signLoc) {

        // Search for arenas
        String serializedLoc = DataParser.serializeLocation(signLoc);
        for (PluginData data : plugins.values()) {
            Arena arena = data.getArena(signLoc);
            if (arena != null)
                return arena;
        }

        // Arena not found
        return null;
    }

    /**
     * Loads the data for a plugin and registers it with the API
     *
     * @param plugin  plugin to load
     */
    public PluginData loadPluginData(JavaPlugin plugin) {
        PluginData data = new PluginData(this, plugin);
        data.loadData();
        plugins.put(plugin.getName(), data);
        return data;
    }

    /**
     * Retrieves the data for a registered plugin
     *
     * @param plugin registered plugin
     * @return       data for the plugin or null if not registered
     */
    public PluginData getPluginData(JavaPlugin plugin) {
        return plugins.get(plugin.getName());
    }

    /**
     * Unloads the data for a plugin from memory
     *
     * @param plugin plugin to unload
     */
    public void clearPluginData(JavaPlugin plugin) {
        plugins.remove(plugin.getName());
    }

    /**
     * Wipes all data for a plugin
     *
     * @param plugin plugin data to wipe
     */
    public void deletePluginData(JavaPlugin plugin) {
        if (plugins.containsKey(plugin.getName())) {
            PluginData data = plugins.get(plugin.getName());
            data.deleteData();
            plugins.remove(plugin.getName());
        }
    }
}
