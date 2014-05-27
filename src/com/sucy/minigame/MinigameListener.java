package com.sucy.minigame;

import com.sucy.minigame.arena.Arena;
import com.sucy.minigame.event.PlayerJoinFailedEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listener for the API
 */
public class MinigameListener implements Listener {

    private final MinigameAPI api;

    /**
     * Constructor
     *
     * @param api API reference
     */
    public MinigameListener(MinigameAPI api) {
        this.api = api;
        api.getServer().getPluginManager().registerEvents(this, api);
    }

    /**
     * Saves plugin data upon disable
     *
     * @param event event details
     */
    @EventHandler
    public void onDisable(PluginDisableEvent event) {

        // Must be a JavaPlugin that has data loaded
        if (!(event.getPlugin() instanceof JavaPlugin) || api.getPluginData((JavaPlugin)event.getPlugin()) == null)
            return;

        // Save the data and remove it
        api.getPluginData((JavaPlugin)event.getPlugin()).save();
        api.clearPluginData((JavaPlugin)event.getPlugin());
    }

    /**
     * Handles joining arenas via sign
     *
     * @param event event details
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        // Must be a right click on a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        Arena arena = api.getArena(block.getLocation());
        api.getLogger().info("Arena: " + arena);

        // Must be an arena sign
        if (arena != null) {

            api.getLogger().info("Can Join: " + arena.canPlayersJoin());
            api.getLogger().info("Can Start: " + arena.canStartGame());

            // Arena must be functioning and allowing plyers in
            if (arena.canPlayersJoin() && arena.canStartGame()) {

                api.getLogger().info("In arena: " + (arena.getPlayer(event.getPlayer().getName()) != null));

                // Player is already in the arena
                if (arena.getPlayer(event.getPlayer().getName()) != null)
                    return;

                // Add the player to the arena
                arena.addPlayer(event.getPlayer());
            }

            // Arena is not functioning or isn't accepting new players
            else {
                PlayerJoinFailedEvent e = new PlayerJoinFailedEvent(arena, event.getPlayer());
                api.getServer().getPluginManager().callEvent(e);
            }
        }
    }

    /**
     * Clears an arena's sign location if the sign is broken
     *
     * @param event event details
     */
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Arena arena = api.getArena(event.getBlock().getLocation());
        if (arena != null) {
            arena.setSignLoc(null);
        }
    }
}
