package com.sucy.minigame.arena;

import com.sucy.minigame.plugin.PluginData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/**
 * Player data while inside an arena
 */
public class ArenaPlayer {

    private static final ItemStack[] NO_ARMOR = new ItemStack[4];

    // Custom data
    private final HashMap<String, Object> data = new HashMap<String, Object>();

    // Owner references
    private final Arena arena;

    // Backup data
    private final String playerName;
    private final GameMode prevMode;
    private final Location prevLoc;
    private final ItemStack[] prevArmor;
    private final ItemStack[] prevInv;
    private final int prevLevel;
    private final float prevExp;

    // Team in the arena
    private ArenaTeam team;

    /**
     * Constructor
     *
     * @param arena  arena reference
     * @param player player reference
     */
    public ArenaPlayer(Arena arena, Player player) {
        this.arena = arena;

        // Backup player data
        playerName = player.getName();
        prevMode = player.getGameMode();
        prevLoc = player.getLocation();
        prevInv = player.getInventory().getContents();
        prevArmor = player.getInventory().getArmorContents();
        prevLevel = player.getLevel();
        prevExp = player.getExp();

        // Clear the player inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(NO_ARMOR);
        player.updateInventory();

        // Set details
        player.setGameMode(GameMode.SURVIVAL);
        player.setLevel(0);
        player.setExp(0);
    }

    /**
     * @return name of the player
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return arena.getPluginData().getPlugin().getServer().getPlayer(playerName);
    }

    /**
     * @return arena the player is in
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * @return team the player is on or null if not set
     */
    public ArenaTeam getTeam() {
        return team;
    }

    /**
     * Puts this player onto the team and leaves their current team if on one
     *
     * @param team team to join
     */
    public void setTeam(ArenaTeam team) {

        // Only set the team if it isn't the current team
        if (this.team != team) {
            leaveTeam();
            this.team = team;
            this.team.addMember(this);
        }
    }

    /**
     * Leaves the player's current team
     */
    public void leaveTeam() {
        if (this.team != null) {
            ArenaTeam team = this.team;
            this.team = null;
            team.removeMember(playerName);
        }
    }

    /**
     * @return data of the plugin owning the arena the player is in
     */
    public PluginData getPluginData() {
        return arena.getPluginData();
    }

    /**
     * @return plugin owning the arena the player is in
     */
    public JavaPlugin getPlugin() {
        return arena.getPluginData().getPlugin();
    }

    /**
     * Attaches custom data to the player
     *
     * @param key  key for the data
     * @param data value for the data
     */
    public void setData(String key, Object data) {
        this.data.put(key, data);
    }

    /**
     * Retrieves custom data from the player
     *
     * @param key key for the data
     * @return    attached data value or null if not found
     */
    public Object getData(String key) {
        if (data.get(key) != null) {
            return data.get(key);
        }
        else return null;
    }

    /**
     * Restores the player data and leaves the arena
     */
    public void leaveArena() {

        Player player = getPlayer();
        arena.removePlayer(this);

        player.setGameMode(prevMode);
        player.getInventory().setContents(prevInv);
        player.getInventory().setArmorContents(prevArmor);
        player.setLevel(prevLevel);
        player.setExp(prevExp);
        player.updateInventory();
        player.teleport(prevLoc);
    }
}
