/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.cache.ObfuscatedDataCache;

public class OrebfuscatorConfig
{
    private static Random random = new Random();
    
    private static final int CONFIG_VERSION = 9;
    private static boolean[] ObfuscateBlocks = new boolean[256];
    private static Integer[] RandomBlocks = new Integer[] { 1, 4, 5, 14, 15, 16, 21, 46, 48, 49, 56, 73, 82, 129 };
    private static Integer[] RandomBlocks2 = RandomBlocks;
    private static List<String> DisabledWorlds = new ArrayList<String>();
    private static int EngineMode = 2;
    private static int UpdateRadius = 2;
    private static int InitialRadius = 1;
    private static int ProcessingThreads = Runtime.getRuntime().availableProcessors() - 1;
    private static int MaxLoadedCacheFiles = 64;
    private static int AirGeneratorMaxChance = 43;
    private static int OrebfuscatorPriority = 0;
    private static boolean UpdateOnDamage = true;
    private static boolean DarknessHideBlocks = true;
    private static boolean NoObfuscationForOps = false;
    private static boolean NoObfuscationForPermission = false;
    private static boolean LoginNotification = true;
    private static boolean AntiTexturePackAndFreecam = true;
    private static boolean UseCache = true;
    private static boolean Enabled = true;
    private static boolean CheckForUpdates = true;
    private static String CacheLocation = "orebfuscator_cache";
    private static File CacheFolder = new File(Bukkit.getServer().getWorldContainer(), CacheLocation);
    
    private static int AntiHitHackDecrementFactor = 1000;
    private static int AntiHitHackMaxViolation = 15;
    private static int OverflowPacketCheckRate = 500;
    private static int ProximityHiderRate = 500;
    
    // Other gets
    public static File getCacheFolder()
    {
        if (!CacheFolder.exists())
        {
            CacheFolder.mkdirs();
        }
        if (!CacheFolder.exists())
        {
            CacheFolder = new File("orebfuscator_cache");
        }
        return CacheFolder;
    }
    
    // Get
    public static int getEngineMode()
    {
        return EngineMode;
    }
    
    public static int getUpdateRadius()
    {
        if (UpdateRadius < 0)
            return 0;
        return UpdateRadius;
    }
    
    public static int getInitialRadius()
    {
        if (InitialRadius < 0)
            return 0;
        return InitialRadius;
    }
    
    public static int getProcessingThreads()
    {
        if (ProcessingThreads <= 0)
            return 1;
        if (ProcessingThreads > Runtime.getRuntime().availableProcessors())
            return Runtime.getRuntime().availableProcessors();
        return ProcessingThreads;
    }
    
    public static int getMaxLoadedCacheFiles()
    {
        if (MaxLoadedCacheFiles <= 16)
            return 16;
        if (MaxLoadedCacheFiles > 128)
            return 128;
        return MaxLoadedCacheFiles;
    }
    
    public static int getAirGeneratorMaxChance()
    {
        if (AirGeneratorMaxChance < 40)
            return 41;
        if (AirGeneratorMaxChance > 100)
            return 101;
        return AirGeneratorMaxChance + 1;
    }
    
    public static int getOrebfuscatorPriority()
    {
        if (OrebfuscatorPriority < 0)
            return 0;
        if (OrebfuscatorPriority > 2)
            return 2;
        return OrebfuscatorPriority;
    }
    
    public static int getAntiHitHackDecrementFactor()
    {
        if (AntiHitHackDecrementFactor < 200)
            return 200;
        if (AntiHitHackDecrementFactor > 60000)
            return 60000;
        return AntiHitHackDecrementFactor;
    }
    
    public static int getAntiHitHackMaxViolation()
    {
        if (AntiHitHackMaxViolation < 3)
            return 3;
        if (AntiHitHackMaxViolation > 50)
            return 50;
        return AntiHitHackMaxViolation;
    }
    
    public static int getOverflowPacketCheckRate()
    {
        if (OverflowPacketCheckRate < 20)
            return 20;
        if (OverflowPacketCheckRate > 2000)
            return 2000;
        return OverflowPacketCheckRate;
    }
    
    public static int getProximityHiderRate()
    {
        if (ProximityHiderRate < 20)
            return 20;
        if (ProximityHiderRate > 2000)
            return 2000;
        return ProximityHiderRate;
    }
    
    public static boolean getUpdateOnDamage()
    {
        return UpdateOnDamage;
    }
    
    public static boolean getDarknessHideBlocks()
    {
        return DarknessHideBlocks;
    }
    
    public static boolean getNoObfuscationForOps()
    {
        return NoObfuscationForOps;
    }
    
    public static boolean getNoObfuscationForPermission()
    {
        return NoObfuscationForPermission;
    }
    
    public static boolean getLoginNotification()
    {
        return LoginNotification;
    }
    
    public static boolean getCheckForUpdates()
    {
        return CheckForUpdates;
    }
    
    public static boolean getAntiTexturePackAndFreecam()
    {
        return AntiTexturePackAndFreecam;
    }
    
    public static boolean getUseCache()
    {
        return UseCache;
    }
    
    public static boolean getEnabled()
    {
        return Enabled;
    }
    
    public static String getCacheLocation()
    {
        return CacheLocation;
    }
    
    public static boolean isBlockTransparent(short id)
    {
        if (id < 0)
            id += 256;
        if (!net.minecraft.server.v1_4_5.Block.i(id))
        {
            return true;
        }
        return false;
    }
    
    public static boolean isObfuscated(short id)
    {
        if (id < 0)
            id += 256;
        
        if (id == 1)
            return true;

        return ObfuscateBlocks[id];
    }
    
    public static boolean isDarknessObfuscated(byte id)
    {
        if (id == 52 || id == 54)
            return true;
        return false;
    }
    
    public static boolean isWorldDisabled(String name)
    {
        for (String world : DisabledWorlds)
        {
            if (world.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }
    
    public static String getDisabledWorlds()
    {
        String retval = "";
        for (String world : DisabledWorlds)
        {
            retval += world + ", ";
        }
        return retval.length() > 1 ? retval.substring(0, retval.length() - 2) : retval;
    }
    
    public static byte getRandomBlock(int index, boolean alternate)
    {
        return (byte) (int) (alternate ? RandomBlocks2[index] : RandomBlocks[index]);
    }
    
    public static Integer[] getRandomBlocks(boolean alternate)
    {
        return (alternate ? RandomBlocks2 : RandomBlocks);
    }
    
    public static void shuffleRandomBlocks()
    {
        synchronized (RandomBlocks)
        {
            Collections.shuffle(Arrays.asList(RandomBlocks));
            Collections.shuffle(Arrays.asList(RandomBlocks2));
        }
    }
    
    public static int random(int max)
    {
        return random.nextInt(max);
    }
    
    // Set
    
    public static void setEngineMode(int data)
    {
        setData("Integers.EngineMode", data);
        EngineMode = data;
    }
    
    public static void setUpdateRadius(int data)
    {
        setData("Integers.UpdateRadius", data);
        UpdateRadius = data;
    }
    
    public static void setInitialRadius(int data)
    {
        setData("Integers.InitialRadius", data);
        InitialRadius = data;
    }
    
    public static void setProcessingThreads(int data)
    {
        setData("Integers.ProcessingThreads", data);
        ProcessingThreads = data;
    }
    
    public static void setMaxLoadedCacheFiles(int data)
    {
        setData("Integers.MaxLoadedCacheFiles", data);
        MaxLoadedCacheFiles = data;
    }
    
    public static void setAirGeneratorMaxChance(int data)
    {
        setData("Integers.AirGeneratorMaxChance", data);
        AirGeneratorMaxChance = data;
    }
    
    public static void setOrebfuscatorPriority(int data)
    {
        setData("Integers.OrebfuscatorPriority", data);
        OrebfuscatorPriority = data;
    }
    
    public static void setUpdateOnDamage(boolean data)
    {
        setData("Booleans.UpdateOnDamage", data);
        UpdateOnDamage = data;
    }
    
    public static void setDarknessHideBlocks(boolean data)
    {
        setData("Booleans.DarknessHideBlocks", data);
        DarknessHideBlocks = data;
    }
    
    public static void setNoObfuscationForOps(boolean data)
    {
        setData("Booleans.NoObfuscationForOps", data);
        NoObfuscationForOps = data;
    }
    
    public static void setNoObfuscationForPermission(boolean data)
    {
        setData("Booleans.NoObfuscationForPermission", data);
        NoObfuscationForPermission = data;
    }
    
    public static void setLoginNotification(boolean data)
    {
        setData("Booleans.LoginNotification", data);
        LoginNotification = data;
    }
    
    public static void setCheckForUpdates(boolean data)
    {
        setData("Booleans.CheckForUpdates", data);
        CheckForUpdates = data;
    }
    
    public static void setAntiTexturePackAndFreecam(boolean data)
    {
        setData("Booleans.AntiTexturePackAndFreecam", data);
        AntiTexturePackAndFreecam = data;
    }
    
    public static void setUseCache(boolean data)
    {
        setData("Booleans.UseCache", data);
        UseCache = data;
    }
    
    public static void setEnabled(boolean data)
    {
        setData("Booleans.Enabled", data);
        Enabled = data;
    }
    
    public static void setCacheLocation(String data)
    {
        setData("Strings.CacheLocation", data);
        CacheFolder = new File(data);
        CacheLocation = data;
    }
    
    public static void setDisabledWorlds(String name, boolean data)
    {
        if (!data)
        {
            DisabledWorlds.remove(name);
        }
        else
        {
            DisabledWorlds.add(name);
        }
        setData("Lists.DisabledWorlds", DisabledWorlds);
    }
    
    private static boolean getBoolean(String path, boolean defaultData)
    {
        if (Orebfuscator.instance.getConfig().get(path) == null)
            setData(path, defaultData);
        return Orebfuscator.instance.getConfig().getBoolean(path, defaultData);
    }
    
    private static String getString(String path, String defaultData)
    {
        if (Orebfuscator.instance.getConfig().get(path) == null)
            setData(path, defaultData);
        return Orebfuscator.instance.getConfig().getString(path, defaultData);
    }
    
    private static int getInt(String path, int defaultData)
    {
        if (Orebfuscator.instance.getConfig().get(path) == null)
            setData(path, defaultData);
        return Orebfuscator.instance.getConfig().getInt(path, defaultData);
    }
    
    private static List<Integer> getIntList(String path, List<Integer> defaultData)
    {
        if (Orebfuscator.instance.getConfig().get(path) == null)
            setData(path, defaultData);
        return Orebfuscator.instance.getConfig().getIntegerList(path);
    }
    
    private static Integer[] getIntList2(String path, List<Integer> defaultData)
    {
        if (Orebfuscator.instance.getConfig().get(path) == null)
            setData(path, defaultData);
        return Orebfuscator.instance.getConfig().getIntegerList(path).toArray(new Integer[1]);
    }
    
    private static List<String> getStringList(String path, List<String> defaultData)
    {
        if (Orebfuscator.instance.getConfig().get(path) == null)
            setData(path, defaultData);
        return Orebfuscator.instance.getConfig().getStringList(path);
    }
    
    private static void setData(String path, Object data)
    {
        try
        {
            Orebfuscator.instance.getConfig().set(path, data);
            save();
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
    }
    
    private static void setBlockValues(boolean[] boolArray, List<Integer> blocks)//, boolean removeTransparent)
    {
        for (int i = 0; i < boolArray.length; i++)
        {
            boolArray[i] = blocks.contains(i);
            /*
            if (removeTransparent && boolArray[i] && isBlockTransparent((short) i))
            {
                boolArray[i] = false;
            }
            */
        }
    }
    
    public static void load()
    {
        // Version check
        int version = getInt("ConfigVersion", CONFIG_VERSION);
        if (version < CONFIG_VERSION)
        {
            /*
             * Orebfuscator.log("Configuration out of date. Recreating new configuration file.");
             * File configFile = new File(Orebfuscator.instance.getDataFolder(), "config.yml");
             * File destination = new File(Orebfuscator.instance.getDataFolder(), "config_old.yml");
             * if (destination.exists())
             * {
             * try
             * {
             * destination.delete();
             * }
             * catch (Exception e)
             * {
             * Orebfuscator.log(e);
             * }
             * }
             * configFile.renameTo(destination);
             * reload();
             */
            ObfuscatedDataCache.ClearCache();
            setData("ConfigVersion", CONFIG_VERSION);
        }
        
        EngineMode = getInt("Integers.EngineMode", EngineMode);
        if (EngineMode != 1 && EngineMode != 2)
        {
            EngineMode = 2;
            System.out.println("[Orebfuscator] EngineMode must be 1 or 2.");
        }
        UpdateRadius = getInt("Integers.UpdateRadius", UpdateRadius);
        InitialRadius = getInt("Integers.InitialRadius", InitialRadius);
        if (InitialRadius > 5)
        {
            InitialRadius = 5;
            System.out.println("[Orebfuscator] InitialRadius must be less than 6.");
        }
        if (InitialRadius == 0)
        {
            System.out.println("[Orebfuscator] Warning, InitialRadius is 0. This will cause all exposed blocks to be obfuscated.");
        }
        ProcessingThreads = getInt("Integers.ProcessingThreads", ProcessingThreads);
        MaxLoadedCacheFiles = getInt("Integers.MaxLoadedCacheFiles", MaxLoadedCacheFiles);
        AirGeneratorMaxChance = getInt("Integers.AirGeneratorMaxChance", AirGeneratorMaxChance);
        OrebfuscatorPriority = getInt("Integers.OrebfuscatorPriority", OrebfuscatorPriority);
        UpdateOnDamage = getBoolean("Booleans.UpdateOnDamage", UpdateOnDamage);
        DarknessHideBlocks = getBoolean("Booleans.DarknessHideBlocks", DarknessHideBlocks);
        NoObfuscationForOps = getBoolean("Booleans.NoObfuscationForOps", NoObfuscationForOps);
        NoObfuscationForPermission = getBoolean("Booleans.NoObfuscationForPermission", NoObfuscationForPermission);
        UseCache = getBoolean("Booleans.UseCache", UseCache);
        LoginNotification = getBoolean("Booleans.LoginNotification", LoginNotification);
        AntiTexturePackAndFreecam = getBoolean("Booleans.AntiTexturePackAndFreecam", AntiTexturePackAndFreecam);
        Enabled = getBoolean("Booleans.Enabled", Enabled);
        CheckForUpdates = getBoolean("Booleans.CheckForUpdates", CheckForUpdates);
        setBlockValues(ObfuscateBlocks, getIntList("Lists.ObfuscateBlocks", Arrays.asList(new Integer[] { 14, 15, 16, 21, 54, 56, 73, 74, 129, 130 })));//, true);
        DisabledWorlds = getStringList("Lists.DisabledWorlds", DisabledWorlds);
        CacheLocation = getString("Strings.CacheLocation", CacheLocation);
        CacheFolder = new File(CacheLocation);
        
        RandomBlocks = getIntList2("Lists.RandomBlocks", Arrays.asList(RandomBlocks));
        
        // Validate RandomBlocks
        for (int i = 0; i < RandomBlocks.length; i++)
        {
            // Don't want people to put chests and other stuff that lags the hell out of players.
            if (OrebfuscatorConfig.isBlockTransparent((short) (int) RandomBlocks[i]))
            {
                RandomBlocks[i] = 1;
            }
        }
        RandomBlocks2 = RandomBlocks;
        
        save();
    }
    
    public static void reload()
    {
        Orebfuscator.instance.reloadConfig();
        load();
    }
    
    public static void save()
    {
        Orebfuscator.instance.saveConfig();
    }
    
    public static boolean obfuscateForPlayer(Player player)
    {
        return !(playerBypassOp(player) || playerBypassPerms(player));
    }
    
    public static boolean playerBypassOp(Player player)
    {
        boolean ret = false;
        try
        {
            ret = OrebfuscatorConfig.getNoObfuscationForOps() && player.isOp();
        }
        catch (Exception e)
        {
            Orebfuscator.log("Error while obtaining Operator status for player" + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }
    
    public static boolean playerBypassPerms(Player player)
    {
        boolean ret = false;
        try
        {
            ret = OrebfuscatorConfig.getNoObfuscationForPermission() && player.hasPermission("Orebfuscator.deobfuscate");
        }
        catch (Exception e)
        {
            Orebfuscator.log("Error while obtaining permissions for player" + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }
}
