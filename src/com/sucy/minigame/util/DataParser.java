package com.sucy.minigame.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;

/**
 * Parses config data into various values
 */
public class DataParser {

    /**
     * Parses a location from a string
     *
     * @param data data to parse
     * @return     location or null if invalid input
     */
    public static Location parseLocation(String data) {
        String[] pieces = data.split(",");

        // A location requires four arguments
        if (pieces.length != 6)
            return null;

        // Try to parse a location from the data
        try {
            return new Location(Bukkit.getWorld(pieces[0]),
                    Double.parseDouble(pieces[1]),
                    Double.parseDouble(pieces[2]),
                    Double.parseDouble(pieces[3]),
                    Float.parseFloat(pieces[4]),
                    Float.parseFloat(pieces[5]));
        }

        // Return null if failed to parse
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Serializes a location into a string
     *
     * @param loc location to serialize
     * @return    serialized string
     */
    public static String serializeLocation(Location loc) {

        // Null locations will clear the config value
        if (loc == null)
            return null;

        // Otherwise include all necessary data
        return loc.getWorld().getName() + ","
                + loc.getX() + ","
                + loc.getY() + ","
                + loc.getZ() + ","
                + loc.getYaw() + ","
                + loc.getPitch();
    }
}
