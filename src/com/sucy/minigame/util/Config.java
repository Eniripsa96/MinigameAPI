package com.sucy.minigame.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Handles configs for files other than the default config.yml
 *
 * Slightly modified version of the one from the bukkit tutorial
 * Source: http://wiki.bukkit.org/Configuration_API_Reference
 */
public class Config {

    private final String fileName;
    private final JavaPlugin plugin;

    private File configFile;
    private FileConfiguration fileConfiguration;

    /**
     * Constructor
     *
     * @param plugin plugin reference
     */
    public Config(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.fileName = name + ".yml";

        // Setup the path
        this.configFile = new File(plugin.getDataFolder().getAbsolutePath() + "/" + fileName);
    }

    /**
     * @return plugin owning this config file
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * @return name of the file this config saves to
     */
    public String getFile() {
        return fileName.replace(".yml", "");
    }

    /**
     * Reloads the config
     */
    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfiguration.setDefaults(defConfig);
        }
    }

    /**
     * @return config file
     */
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            this.reloadConfig();
        }
        return fileConfiguration;
    }

    /**
     * Saves the config
     */
    public void saveConfig() {
        if (fileConfiguration != null || configFile != null) {
            try {
                getConfig().save(configFile);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
            }
        }
    }

    /**
     * Saves the default config if no file exists yet
     */
    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder().getAbsolutePath() + "/" + fileName);
        }
        if (!configFile.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }
}
