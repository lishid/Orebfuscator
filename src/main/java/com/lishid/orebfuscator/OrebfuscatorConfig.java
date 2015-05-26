/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator;

import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.internal.MinecraftInternals;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class OrebfuscatorConfig {
    // Constant/persistent data
    private static final int CONFIG_VERSION = 10;
    private static Random random = new Random();
    private static int AvailableProcessors = Runtime.getRuntime().availableProcessors();

    // Main engine config
    public static boolean Enabled = true;
    public static boolean UpdateOnDamage = true;
    public static int EngineMode = 2;
    public static int InitialRadius = 1;
    public static int UpdateRadius = 2;
    public static int OrebfuscatorPriority = 1;
    public static int CompressionLevel = 1;
    public static int ProcessingThreads = AvailableProcessors - 1;

    // Darkness
    public static boolean DarknessHideBlocks = false;

    // Caching
    public static boolean UseCache = true;
    public static int MaxLoadedCacheFiles = 64;
    public static String CacheLocation = "orebfuscator_cache";
    public static File CacheFolder = new File(Bukkit.getServer().getWorldContainer(), CacheLocation);

    // ProximityHider
    public static int ProximityHiderRate = 500;
    public static int ProximityHiderDistance = 8;
    public static int ProximityHiderID = 1;
    public static int ProximityHiderEnd = 255;
    public static boolean UseProximityHider = true;
    public static boolean UseSpecialBlockForProximityHider = true;
    public static boolean UseYLocationProximity = false;

    // AntiTexturePackAndFreecam
    public static boolean AntiTexturePackAndFreecam = true;
    public static int AirGeneratorMaxChance = 43;

    // Misc
    public static boolean NoObfuscationForOps = false;
    public static boolean NoObfuscationForPermission = false;
    public static boolean LoginNotification = true;

    // Anti Hit Hack
    public static int AntiHitHackDecrementFactor = 1000;
    public static int AntiHitHackMaxViolation = 15;

    // Utilities
    private static boolean[] ObfuscateBlocks = new boolean[256];
    private static boolean[] NetherObfuscateBlocks = new boolean[256];
    private static boolean[] DarknessBlocks = new boolean[256];
    private static boolean[] ProximityHiderBlocks = new boolean[256];
    private static Integer[] RandomBlocks = new Integer[]{1, 4, 5, 14, 15, 16, 21, 46, 48, 49, 56, 73, 82, 129};
    private static Integer[] NetherRandomBlocks = new Integer[]{13, 87, 88, 112, 153};
    private static Integer[] RandomBlocks2 = RandomBlocks;
    private static List<String> DisabledWorlds = new ArrayList<String>();

    public static File getCacheFolder() {
        // Try to make the folder
        if (!CacheFolder.exists()) {
            CacheFolder.mkdirs();
        }
        // Can't make folder? Use default
        if (!CacheFolder.exists()) {
            CacheFolder = new File("orebfuscator_cache");
        }
        return CacheFolder;
    }

    private static boolean[] TransparentBlocks = new boolean[256];
    private static boolean TransparentCached = false;

    public static boolean isBlockTransparent(int id) {
        if (!TransparentCached) {
            // Generate TransparentBlocks by reading them from Minecraft
            generateTransparentBlocks();
        }

        if (id < 0)
            id += 256;

        if (id >= 256) {
            return false;
        }

        return TransparentBlocks[id];
    }

    private static void generateTransparentBlocks() {
        for (int i = 0; i < TransparentBlocks.length; i++) {
            TransparentBlocks[i] = MinecraftInternals.isBlockTransparent(i);
            if (i == org.bukkit.Material.TNT.getId()) {
                TransparentBlocks[i] = false;
            }
            if (i == org.bukkit.Material.AIR.getId() || i == org.bukkit.Material.WATER.getId() || i == org.bukkit.Material.STATIONARY_WATER.getId()) {
                TransparentBlocks[i] = true;
            }
        }
        TransparentCached = true;
    }

    public static boolean isObfuscated(int id, World.Environment environment) {
        if (id < 0)
            id += 256;

        // Nether case
        if (environment == World.Environment.NETHER) {
            return id == 87 || NetherObfuscateBlocks[id];
        }

        // Normal case
        return id == 1 || ObfuscateBlocks[id];
    }

    public static boolean isDarknessObfuscated(int id) {
        if (id < 0)
            id += 256;

        return DarknessBlocks[id];
    }

    public static boolean isProximityObfuscated(int y, int id) {
        if (id < 0)
            id += 256;

        return ProximityHiderBlocks[id];
    }

    public static boolean isProximityHiderOn(int y, int id) {
        return (UseYLocationProximity && y >= ProximityHiderEnd) ||
                (!UseYLocationProximity && y <= ProximityHiderEnd);
    }

    public static boolean skipProximityHiderCheck(int y) {
        return UseYLocationProximity && y < ProximityHiderEnd;
    }

    public static boolean proximityHiderDeobfuscate(int playerY, Block block) {
        return UseYLocationProximity;
    }

    public static boolean isWorldDisabled(String name) {
        for (String world : DisabledWorlds) {
            if (world.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public static String getDisabledWorlds() {
        String retval = "";
        for (String world : DisabledWorlds) {
            retval += world + ", ";
        }
        return retval.length() > 1 ? retval.substring(0, retval.length() - 2) : retval;
    }

    public static int getRandomBlock(int index, boolean alternate, World.Environment environment) {
        if (environment == World.Environment.NETHER)
            return (int) (NetherRandomBlocks[index]);
        return (int) (alternate ? RandomBlocks2[index] : RandomBlocks[index]);
    }

    public static Integer[] getRandomBlocks(boolean alternate, World.Environment environment) {
        if (environment == World.Environment.NETHER)
            return NetherRandomBlocks;
        return (alternate ? RandomBlocks2 : RandomBlocks);
    }

    public static void shuffleRandomBlocks() {
        synchronized (RandomBlocks) {
            Collections.shuffle(Arrays.asList(RandomBlocks));
            Collections.shuffle(Arrays.asList(RandomBlocks2));
        }
    }

    public static int random(int max) {
        return random.nextInt(max);
    }

    // Set

    public static void setEngineMode(int data) {
        setData("Integers.EngineMode", data);
        EngineMode = data;
    }

    public static void setUpdateRadius(int data) {
        setData("Integers.UpdateRadius", data);
        UpdateRadius = data;
    }

    public static void setInitialRadius(int data) {
        setData("Integers.InitialRadius", data);
        InitialRadius = data;
    }

    public static void setProcessingThreads(int data) {
        setData("Integers.ProcessingThreads", data);
        ProcessingThreads = data;
    }

    public static void setProximityHiderDistance(int data) {
        setData("Integers.ProximityHiderDistance", data);
        ProximityHiderDistance = data;
    }

    public static void setAirGeneratorMaxChance(int data) {
        setData("Integers.AirGeneratorMaxChance", data);
        AirGeneratorMaxChance = data;
    }

    public static void setUseProximityHider(boolean data) {
        setData("Booleans.UseProximityHider", data);
        UseProximityHider = data;
    }

    public static void setDarknessHideBlocks(boolean data) {
        setData("Booleans.DarknessHideBlocks", data);
        DarknessHideBlocks = data;
    }

    public static void setNoObfuscationForOps(boolean data) {
        setData("Booleans.NoObfuscationForOps", data);
        NoObfuscationForOps = data;
    }

    public static void setNoObfuscationForPermission(boolean data) {
        setData("Booleans.NoObfuscationForPermission", data);
        NoObfuscationForPermission = data;
    }

    public static void setLoginNotification(boolean data) {
        setData("Booleans.LoginNotification", data);
        LoginNotification = data;
    }

    public static void setAntiTexturePackAndFreecam(boolean data) {
        setData("Booleans.AntiTexturePackAndFreecam", data);
        AntiTexturePackAndFreecam = data;
    }

    public static void setUseCache(boolean data) {
        setData("Booleans.UseCache", data);
        UseCache = data;
    }

    public static void setEnabled(boolean data) {
        setData("Booleans.Enabled", data);
        Enabled = data;
    }

    public static void setDisabledWorlds(String name, boolean data) {
        if (!data) {
            DisabledWorlds.remove(name);
        } else {
            DisabledWorlds.add(name);
        }
        setData("Lists.DisabledWorlds", DisabledWorlds);
    }

    private static boolean getBoolean(String path, boolean defaultData) {
        if (getConfig().get(path) == null)
            setData(path, defaultData);
        return getConfig().getBoolean(path, defaultData);
    }

    private static String getString(String path, String defaultData) {
        if (getConfig().get(path) == null)
            setData(path, defaultData);
        return getConfig().getString(path, defaultData);
    }

    private static int getInt(String path, int defaultData) {
        if (getConfig().get(path) == null)
            setData(path, defaultData);
        return getConfig().getInt(path, defaultData);
    }

    private static List<Integer> getIntList(String path, List<Integer> defaultData) {
        if (getConfig().get(path) == null)
            setData(path, defaultData);
        return getConfig().getIntegerList(path);
    }

    private static Integer[] getIntList2(String path, List<Integer> defaultData) {
        if (getConfig().get(path) == null)
            setData(path, defaultData);
        return getConfig().getIntegerList(path).toArray(new Integer[1]);
    }

    private static List<String> getStringList(String path, List<String> defaultData) {
        if (getConfig().get(path) == null)
            setData(path, defaultData);
        return getConfig().getStringList(path);
    }

    private static void setData(String path, Object data) {
        try {
            getConfig().set(path, data);
            save();
        } catch (Exception e) {
            Orebfuscator.log(e);
        }
    }

    private static void setBlockValues(boolean[] boolArray, List<Integer> blocks, boolean transparent) {
        for (int i = 0; i < boolArray.length; i++) {
            boolArray[i] = blocks.contains(i);

            // If block is transparent while we don't want them to, or the other way around
            if (transparent != isBlockTransparent((short) i)) {
                // Remove it
                boolArray[i] = false;
            }
        }
    }

    private static void setBlockValues(boolean[] boolArray, List<Integer> blocks) {
        for (int i = 0; i < boolArray.length; i++) {
            boolArray[i] = blocks.contains(i);
        }
    }

    public static void load() {

        // Version check
        int version = getInt("ConfigVersion", CONFIG_VERSION);
        if (version < CONFIG_VERSION) {
            // Orebfuscator.log("Configuration out of date. Recreating new configuration file.");
            // File configFile = new File(Orebfuscator.instance.getDataFolder(), "config.yml");
            // File destination = new File(Orebfuscator.instance.getDataFolder(), "config_old.yml");
            // if (destination.exists())
            // {
            // try
            // {
            // destination.delete();
            // }
            // catch (Exception e)
            // {
            // Orebfuscator.log(e);
            // }
            // }
            // configFile.renameTo(destination);
            // reload();

            ObfuscatedDataCache.ClearCache();
            setData("ConfigVersion", CONFIG_VERSION);
        }

        EngineMode = getInt("Integers.EngineMode", EngineMode);
        if (EngineMode != 1 && EngineMode != 2) {
            EngineMode = 2;
            Orebfuscator.log("EngineMode must be 1 or 2.");
        }

        InitialRadius = clamp(getInt("Integers.InitialRadius", InitialRadius), 0, 2);
        if (InitialRadius == 0) {
            Orebfuscator.log("Warning, InitialRadius is 0. This will cause all exposed blocks to be obfuscated.");
        }

        UpdateRadius = clamp(getInt("Integers.UpdateRadius", UpdateRadius), 1, 5);
        ProcessingThreads = clamp(getInt("Integers.ProcessingThreads", ProcessingThreads), 1, AvailableProcessors);
        MaxLoadedCacheFiles = clamp(getInt("Integers.MaxLoadedCacheFiles", MaxLoadedCacheFiles), 16, 128);
        ProximityHiderDistance = clamp(getInt("Integers.ProximityHiderDistance", ProximityHiderDistance), 2, 64);

        ProximityHiderID = getInt("Integers.ProximityHiderID", ProximityHiderID);
        ProximityHiderEnd = clamp(getInt("Integers.ProximityHiderEnd", ProximityHiderEnd), 0, 255);
        AirGeneratorMaxChance = clamp(getInt("Integers.AirGeneratorMaxChance", AirGeneratorMaxChance), 40, 100);
        OrebfuscatorPriority = clamp(getInt("Integers.OrebfuscatorPriority", OrebfuscatorPriority), Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
        CompressionLevel = clamp(getInt("Integers.CompressionLevel", CompressionLevel), 1, 9);
        UseProximityHider = getBoolean("Booleans.UseProximityHider", UseProximityHider);
        UseSpecialBlockForProximityHider = getBoolean("Booleans.UseSpecialBlockForProximityHider", UseSpecialBlockForProximityHider);
        UseYLocationProximity = getBoolean("Booleans.UseYLocationProximity", UseYLocationProximity);
        UpdateOnDamage = getBoolean("Booleans.UpdateOnDamage", UpdateOnDamage);
        DarknessHideBlocks = getBoolean("Booleans.DarknessHideBlocks", DarknessHideBlocks);
        NoObfuscationForOps = getBoolean("Booleans.NoObfuscationForOps", NoObfuscationForOps);
        NoObfuscationForPermission = getBoolean("Booleans.NoObfuscationForPermission", NoObfuscationForPermission);
        UseCache = getBoolean("Booleans.UseCache", UseCache);
        LoginNotification = getBoolean("Booleans.LoginNotification", LoginNotification);
        AntiTexturePackAndFreecam = getBoolean("Booleans.AntiTexturePackAndFreecam", AntiTexturePackAndFreecam);
        Enabled = getBoolean("Booleans.Enabled", Enabled);

        // Read block lists
        setBlockValues(ObfuscateBlocks, getIntList("Lists.ObfuscateBlocks", Arrays.asList(new Integer[]{14, 15, 16, 21, 54, 56, 73, 74, 129, 130})), false);
        setBlockValues(NetherObfuscateBlocks, getIntList("Lists.NetherObfuscateBlocks", Arrays.asList(new Integer[]{87, 153})), false);
        setBlockValues(DarknessBlocks, getIntList("Lists.DarknessBlocks", Arrays.asList(new Integer[]{52, 54})));
        setBlockValues(ProximityHiderBlocks, getIntList("Lists.ProximityHiderBlocks", Arrays.asList(new Integer[]{23, 52, 54, 56, 58, 61, 62, 116, 129, 130, 145, 146})));

        // Disable worlds
        DisabledWorlds = getStringList("Lists.DisabledWorlds", DisabledWorlds);

        // Read the cache location
        CacheLocation = getString("Strings.CacheLocation", CacheLocation);
        CacheFolder = new File(CacheLocation);

        RandomBlocks = getIntList2("Lists.RandomBlocks", Arrays.asList(RandomBlocks));
        NetherRandomBlocks = getIntList2("Lists.NetherRandomBlocks", Arrays.asList(NetherRandomBlocks));

        // Validate RandomBlocks
        for (int i = 0; i < RandomBlocks.length; i++) {
            // Don't want people to put chests and other stuff that lags the hell out of players.
            if (RandomBlocks[i] == null || OrebfuscatorConfig.isBlockTransparent((short) (int) RandomBlocks[i])) {
                RandomBlocks[i] = 1;
            }
        }
        RandomBlocks2 = RandomBlocks;

        save();
    }

    public static void reload() {
        Orebfuscator.instance.reloadConfig();
        load();
    }

    public static void save() {
        Orebfuscator.instance.saveConfig();
    }

    public static boolean obfuscateForPlayer(Player player) {
        return !(playerBypassOp(player) || playerBypassPerms(player));
    }

    public static boolean playerBypassOp(Player player) {
        boolean ret = false;
        try {
            ret = OrebfuscatorConfig.NoObfuscationForOps && player.isOp();
        } catch (Exception e) {
            Orebfuscator.log("Error while obtaining Operator status for player" + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean playerBypassPerms(Player player) {
        boolean ret = false;
        try {
            ret = OrebfuscatorConfig.NoObfuscationForPermission && player.hasPermission("Orebfuscator.deobfuscate");
        } catch (Exception e) {
            Orebfuscator.log("Error while obtaining permissions for player" + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    private static FileConfiguration getConfig() {
        return Orebfuscator.instance.getConfig();
    }

    public static int clamp(int value, int min, int max) {
        if (value < min)
            value = min;
        if (value > max)
            value = max;
        return value;
    }
}
