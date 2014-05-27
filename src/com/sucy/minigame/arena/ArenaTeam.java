package com.sucy.minigame.arena;

import com.sucy.minigame.util.DataParser;
import com.sucy.minigame.plugin.ConfigValues;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;

/**
 * A team definition for arenas
 */
public class ArenaTeam {

    private final HashMap<String, ArenaPlayer> members = new HashMap<String, ArenaPlayer>();

    private final Arena arena;
    private final String name;

    private Location spawn;

    /**
     * Constructor
     *
     * @param arena parent arena
     * @param name  team name
     * @param spawn team spawn location
     */
    public ArenaTeam(Arena arena, String name, Location spawn) {
        this.arena = arena;
        this.name = name;
        this.spawn = spawn;
    }

    /**
     * Config constructor
     *
     * @param arena arena to load from
     * @param name  team name
     */
    public ArenaTeam(Arena arena, String name) {
        this.arena = arena;
        this.name = name;
        this.spawn = DataParser.parseLocation(arena.getConfig().getString(ConfigValues.TEAMS + "." + name));
    }

    /**
     * @return arena owning the team
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * @return arena name
     */
    public String getName() {
        return name;
    }

    /**
     * @return spawn location for the team
     */
    public Location getSpawn() {
        return spawn;
    }

    /**
     * Sets the spawn location for the team
     *
     * @param loc spawn location
     */
    public void setSpawn(Location loc) {
        spawn = loc;
    }

    /**
     * Retrieves a member of the team
     *
     * @param playerName player playerName
     * @return     player data
     */
    public ArenaPlayer getMember(String playerName) {
        return members.get(playerName.toLowerCase());
    }

    /**
     * @return the collection of all team members
     */
    public Collection<ArenaPlayer> getMembers() {
        return members.values();
    }

    /**
     * @return number of players on the team
     */
    public int getTeamSize() {
        return members.size();
    }

    /**
     * Moves all team members to their spawn locations
     */
    public void spawnPlayers() {
        for (ArenaPlayer player : members.values()) {
            player.getPlayer().teleport(spawn);
        }
    }

    /**
     * Adds a player to the team
     *
     * @param player player to add
     */
    public void addMember(ArenaPlayer player) {
        if (!members.containsKey(player.getPlayerName().toLowerCase())) {
            members.put(player.getPlayerName().toLowerCase(), player);
            player.setTeam(this);
        }
    }

    /**
     * @param playerName name of player to remove
     * @return           data of the player that was removed
     */
    public ArenaPlayer removeMember(String playerName) {
        if (members.containsKey(playerName.toLowerCase())) {
            ArenaPlayer player = members.get(playerName.toLowerCase());
            members.remove(playerName.toLowerCase());
            player.leaveTeam();
            return player;
        }
        else return null;
    }

    /**
     * Removes all players from the team
     *
     * @return players that were removed from the team
     */
    public Collection<ArenaPlayer> removeAllMembers() {
        Collection<ArenaPlayer> removedPlayers = members.values();
        for (String key : members.keySet()) {
            removeMember(key);
        }
        return removedPlayers;
    }

    /**
     * Saves the team to the config
     */
    public void save() {
        arena.getConfig().set(ConfigValues.TEAMS + "." + name, DataParser.serializeLocation(spawn));
    }
}
