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

package lishid.orebfuscator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OrebfuscatorConfig
{
    private static final int CONFIG_VERSION = 5;
    private static boolean[] ObfuscateBlocks = new boolean[256];
    private static boolean[] ProximityHiderBlocks = new boolean[256];
    private static Integer[] RandomBlocks = new Integer[] { 0, 1, 4, 5, 14, 15, 16, 21, 46, 48, 49, 56, 73, 82 };
    private static List<String> DisabledWorlds = new ArrayList<String>();
    private static int EngineMode = 2;
    private static int UpdateRadius = 2;
    private static int InitialRadius = 1;
    private static int ProcessingThreads = Runtime.getRuntime().availableProcessors() - 1;
    private static int MaxLoadedCacheFiles = 64;
    private static int ProximityHiderDistance = 8;
    private static int ProximityHiderID = 1;
    private static int ProximityHiderEnd = 255;
    private static int AirGeneratorMaxChance = 15;
    private static boolean UseProximityHider = true;
    private static boolean UseSpecialBlockForProximityHider = true;
    private static boolean UpdateOnBreak = true;
    private static boolean UpdateOnDamage = true;
    private static boolean UpdateOnPhysics = true;
    private static boolean UpdateOnPiston = true;
    private static boolean UpdateOnExplosion = true;
    private static boolean UpdateOnHoe = true;
    private static boolean UpdateThread = true;
    private static boolean DarknessHideBlocks = true;
    private static boolean NoObfuscationForOps = true;
    private static boolean NoObfuscationForPermission = true;
    private static boolean LoginNotification = true;
    private static boolean AntiTexturePackAndFreecam = true;
    private static boolean UseCache = true;
    private static boolean Enabled = true;
    private static String CacheLocation = "orebfuscator_cache";
    private static File CacheFolder = new File(Bukkit.getServer().getWorldContainer(), CacheLocation);
    
    // Other gets
    public static File getCacheFolder()
    {
        if (!CacheFolder.exists())
            CacheFolder = new File("orebfuscator_cache");
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
        if (ProcessingThreads > 16)
            return 16;
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
    
    public static int getProximityHiderDistance()
    {
        if (ProximityHiderDistance <= 2)
            return 2;
        if (ProximityHiderDistance > 32)
            return 32;
        return ProximityHiderDistance;
    }
    
    public static int getProximityHiderID()
    {
        return ProximityHiderID;
    }
    
    public static int getProximityHiderEnd()
    {
        return ProximityHiderEnd;
    }
    
    public static int getAirGeneratorMaxChance()
    {
        if (AirGeneratorMaxChance < 1)
            return 2;
        if (AirGeneratorMaxChance > 23)
            return 24;
        return AirGeneratorMaxChance + 1;
    }
    
    public static boolean getUseProximityHider()
    {
        return UseProximityHider;
    }
    
    public static boolean getUseSpecialBlockForProximityHider()
    {
        return UseSpecialBlockForProximityHider;
    }
    
    public static boolean getUpdateOnBreak()
    {
        return UpdateOnBreak;
    }
    
    public static boolean getUpdateOnDamage()
    {
        return UpdateOnDamage;
    }
    
    public static boolean getUpdateOnPhysics()
    {
        return UpdateOnPhysics;
    }
    
    public static boolean getUpdateOnPiston()
    {
        return UpdateOnPiston;
    }
    
    public static boolean getUpdateOnExplosion()
    {
        return UpdateOnExplosion;
    }
    
    public static boolean getUpdateOnHoe()
    {
        return UpdateOnHoe;
    }
    
    public static boolean getUpdateThread()
    {
        return UpdateThread;
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
    
    public static boolean isBlockTransparent(byte id)
    {
        if (id < 0 || !net.minecraft.server.Block.g(id))
        {
            return true;
        }
        return false;
    }
    
    public static boolean isObfuscated(byte id)
    {
        if (id == 1)
            return true;
        
        if (ObfuscateBlocks[id])
            return true;
        return false;
    }
    
    public static boolean isDarknessObfuscated(byte id)
    {
        if (id == 52 || id == 54)
            return true;
        return false;
    }
    
    public static boolean isProximityObfuscated(byte id)
    {
        if (ProximityHiderBlocks[id])
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
    
    public static byte getRandomBlock(int index)
    {
        return (byte) (int) RandomBlocks[index];
    }
    
    public static Integer[] getRandomBlocks()
    {
        return RandomBlocks;
    }
    
    public static void shuffleRandomBlocks()
    {
        synchronized (RandomBlocks)
        {
            Collections.shuffle(Arrays.asList(RandomBlocks));
        }
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
    
    public static void setProximityHiderDistance(int data)
    {
        setData("Integers.ProximityHiderDistance", data);
        ProximityHiderDistance = data;
    }
    
    public static void setProximityHiderID(int data)
    {
        setData("Integers.ProximityHiderID", data);
        ProximityHiderID = data;
    }
    
    public static void setProximityHiderEnd(int data)
    {
        setData("Integers.ProximityHiderEnd", data);
        ProximityHiderEnd = data;
    }
    
    public static void setAirGeneratorMaxChance(int data)
    {
        setData("Integers.AirGeneratorMaxChance", data);
        AirGeneratorMaxChance = data;
    }
    
    public static void setUseProximityHider(boolean data)
    {
        setData("Booleans.UseProximityHider", data);
        UseProximityHider = data;
    }
    
    public static void setUseSpecialBlockForProximityHider(boolean data)
    {
        setData("Booleans.UseSpecialBlockForProximityHider", data);
        UseSpecialBlockForProximityHider = data;
    }
    
    public static void setUpdateOnBreak(boolean data)
    {
        setData("Booleans.UpdateOnBreak", data);
        UpdateOnBreak = data;
    }
    
    public static void setUpdateOnDamage(boolean data)
    {
        setData("Booleans.UpdateOnDamage", data);
        UpdateOnDamage = data;
    }
    
    public static void setUpdateOnPhysics(boolean data)
    {
        setData("Booleans.UpdateOnPhysics", data);
        UpdateOnPhysics = data;
    }
    
    public static void setUpdateOnPiston(boolean data)
    {
        setData("Booleans.UpdateOnPiston", data);
        UpdateOnPiston = data;
    }
    
    public static void setUpdateOnExplosion(boolean data)
    {
        setData("Booleans.UpdateOnExplosion", data);
        UpdateOnExplosion = data;
    }
    
    public static void setUpdateOnHoe(boolean data)
    {
        setData("Booleans.UpdateOnHoe", data);
        UpdateOnHoe = data;
    }
    
    public static void setUpdateThread(boolean data)
    {
        setData("Booleans.UpdateThread", data);
        UpdateThread = data;
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
    
    private static void setBlockValues(boolean[] boolArray, List<Integer> blocks)
    {
        for (int i = 0; i < boolArray.length; i++)
        {
            boolArray[i] = blocks.contains(i);
        }
    }
    
    public static void load()
    {
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
        ProximityHiderDistance = getInt("Integers.ProximityHiderDistance", ProximityHiderDistance);
        ProximityHiderID = getInt("Integers.ProximityHiderID", ProximityHiderID);
        ProximityHiderEnd = getInt("Integers.ProximityHiderEnd", ProximityHiderEnd);
        AirGeneratorMaxChance = getInt("Integers.AirGeneratorMaxChance", AirGeneratorMaxChance);
        UseProximityHider = getBoolean("Booleans.UseProximityHider", UseProximityHider);
        UseSpecialBlockForProximityHider = getBoolean("Booleans.UseSpecialBlockForProximityHider", UseSpecialBlockForProximityHider);
        UpdateOnBreak = getBoolean("Booleans.UpdateOnBreak", UpdateOnBreak);
        UpdateOnDamage = getBoolean("Booleans.UpdateOnDamage", UpdateOnDamage);
        UpdateOnPhysics = getBoolean("Booleans.UpdateOnPhysics", UpdateOnPhysics);
        UpdateOnPiston = getBoolean("Booleans.UpdateOnPiston", UpdateOnPiston);
        UpdateOnExplosion = getBoolean("Booleans.UpdateOnExplosion", UpdateOnExplosion);
        UpdateOnHoe = getBoolean("Booleans.UpdateOnHoe", UpdateOnHoe);
        UpdateThread = getBoolean("Booleans.UpdateThread", UpdateThread);
        DarknessHideBlocks = getBoolean("Booleans.DarknessHideBlocks", DarknessHideBlocks);
        NoObfuscationForOps = getBoolean("Booleans.NoObfuscationForOps", NoObfuscationForOps);
        NoObfuscationForPermission = getBoolean("Booleans.NoObfuscationForPermission", NoObfuscationForPermission);
        UseCache = getBoolean("Booleans.UseCache", UseCache);
        LoginNotification = getBoolean("Booleans.LoginNotification", LoginNotification);
        AntiTexturePackAndFreecam = getBoolean("Booleans.AntiTexturePackAndFreecam", AntiTexturePackAndFreecam);
        Enabled = getBoolean("Booleans.Enabled", Enabled);
        setBlockValues(ObfuscateBlocks, getIntList("Lists.ObfuscateBlocks", Arrays.asList(new Integer[] { 14, 15, 16, 21, 54, 56, 73, 74 })));
        setBlockValues(ProximityHiderBlocks, getIntList("Lists.ProximityHiderBlocks", Arrays.asList(new Integer[] { 23, 54, 56, 58, 61, 62, 116 })));
        RandomBlocks = getIntList2("Lists.RandomBlocks", Arrays.asList(RandomBlocks));
        DisabledWorlds = getStringList("Lists.DisabledWorlds", DisabledWorlds);
        CacheLocation = getString("Strings.CacheLocation", CacheLocation);
        CacheFolder = new File(CacheLocation);
        
        // Version check
        int version = getInt("ConfigVersion", 0);
        
        if (version < CONFIG_VERSION)
        {
            List<Integer> RandomBlocks2 = new ArrayList<Integer>(Arrays.asList(RandomBlocks));
            if (!RandomBlocks2.contains(4))
                RandomBlocks2.add(4);
            if (!RandomBlocks2.contains(46))
                RandomBlocks2.add(46);
            if (!RandomBlocks2.contains(49))
                RandomBlocks2.add(49);
            if (!RandomBlocks2.contains(82))
                RandomBlocks2.add(82);
            RandomBlocks = RandomBlocks2.toArray(new Integer[0]);
            
            setData("Lists.RandomBlocks", Arrays.asList(RandomBlocks));
            
            setData("Lists.ProximityHiderBlocks", Arrays.asList(new Integer[] { 23, 54, 56, 58, 61, 62, 116, 117 }));
        }
        setData("ConfigVersion", CONFIG_VERSION);
        
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
            Orebfuscator.log("Error while obtaining OP status for player" + player.getName() + ": " + e.getMessage());
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
        }
        return ret;
    }
}
