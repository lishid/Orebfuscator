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

package lishid.orebfuscator.utils;

import gnu.trove.set.hash.TByteHashSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lishid.orebfuscator.Orebfuscator;

public class OrebfuscatorConfig {
	private static TByteHashSet TransparentBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{6,8,9,10,11,18,20,26,27,28,30,31,32,34,37,38,39,40,44,50,51,52,53,54,55,59,63,64,65,66,67,68,69,70,71,72,75,76,77,78,79,81,83,85,90,92,93,94,96,101,102,104,105,106,107,108,109,111,113,114,115,116,117,118,119,120,122}));
	//private static TByteHashSet TopTransparentBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{44,60,53,78,93,94,108,109,116}));
	private static TByteHashSet ObfuscateBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{14,15,16,21,54,56,73,74}));
	private static TByteHashSet DarknessObfuscateBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{48,52}));
	private static TByteHashSet ProximityHiderBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{54, 61, 62}));
	private static List<Integer> RandomBlocks = Arrays.asList(new Integer[]{1,5,14,15,16,21,48,56,73});
	private static List<String> DisabledWorlds = new ArrayList<String>();
	private static final Random randomGenerator = new Random();
	private static int EngineMode = 2;
	private static int UpdateRadius = 2;
	private static int InitialRadius = 1;
	private static int ProcessingThreads = 1;
	private static int MaxLoadedCacheFiles = 64;
	private static int ProximityHiderDistance = 8;
	private static long Seed = 0;
	private static boolean UseChunkScrambler = true;
	private static boolean UseProximityHider = true;
	private static boolean UpdateOnBreak = true;
	private static boolean UpdateOnDamage = true;
	private static boolean UpdateOnPhysics = true;
	private static boolean UpdateOnExplosion = true;
	private static boolean UpdateOnHoe = true;
	private static boolean UpdateThread = true;
	private static boolean DarknessHideBlocks = true;
	private static boolean VerboseMode = false;
	private static boolean NoObfuscationForOps = true;
	private static boolean NoObfuscationForPermission = true;
	private static boolean UseCache = true;
	private static boolean Enabled = true;
	private static File CacheFolder = new File(Bukkit.getServer().getWorldContainer(), "orebfuscator_cache");
	
	//Other gets
	public static File getCacheFolder()
	{
		return CacheFolder;
	}
	
	//Get
	public static int getEngineMode()
	{
		return EngineMode;
	}
	
	public static int getUpdateRadius()
	{
		if(UpdateRadius < 1)
			return 1;
		return UpdateRadius;
	}
	
	public static int getInitialRadius()
	{
	 	if(InitialRadius < 0)
	 	 	 return 0;
		return InitialRadius;
	}
	
	public static int getProcessingThreads()
	{
		if(ProcessingThreads <= 0)
			return 1;
		if(ProcessingThreads > 4)
			return 4;
		return ProcessingThreads;
	}
	
	public static int getMaxLoadedCacheFiles()
	{
		if(MaxLoadedCacheFiles <= 16)
			return 16;
		if(MaxLoadedCacheFiles > 128)
			return 128;
		return MaxLoadedCacheFiles;
	}
	
	public static int getProximityHiderDistance()
	{
		if(ProximityHiderDistance <= 2)
			return 2;
		if(MaxLoadedCacheFiles > 32)
			return 32;
		return ProximityHiderDistance;
	}
	
	public static long getSeed()
	{
		return Seed;
	}
	
	public static boolean getUseChunkScrambler()
	{
		return UseChunkScrambler;
	}
	
	public static boolean getUseProximityHider()
	{
		return UseProximityHider;
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
	
	public static boolean getVerboseMode()
	{
		return VerboseMode;
	}
	
	public static boolean getNoObfuscationForOps()
	{
		return NoObfuscationForOps;
	}
	
	public static boolean getNoObfuscationForPermission()
	{
		return NoObfuscationForPermission;
	}
	
	public static boolean getUseCache()
	{
		return UseCache;
	}
	
	public static boolean getEnabled()
	{
		return Enabled;
	}
	
	public static boolean isTransparent(byte id)
	{
	    if (id == 0) return true;
	    
	    if(TransparentBlocks.contains(id))
	    	return true;
	    return false;
	}
	
	public static boolean isObfuscated(byte id)
	{
	    if (id == 1) return true;

	    if(ObfuscateBlocks.contains(id))
	    	return true;
	    return false;
	}
	
	public static boolean isDarknessObfuscated(byte id)
	{
	    if(DarknessObfuscateBlocks.contains(id))
	    	return true;
	    return false;
	}
	
	public static boolean isProximityObfuscated(byte id)
	{
	    if(ProximityHiderBlocks.contains(id))
	    	return true;
	    return false;
	}
	
	public static boolean isWorldDisabled(String name)
	{
		for(String world : DisabledWorlds)
		{
			if(world.equalsIgnoreCase(name))
				return true;
		}
	    return false;
	}
	
	public static String getDisabledWorlds()
	{
		String retval = "";
		for(String world : DisabledWorlds)
		{
			retval += world + ", ";
		}
		return retval.length() > 1 ? retval.substring(0,  retval.length() - 2) : retval;
	}
	
	public static byte generateRandomBlock()
	{
		return (byte)(int)RandomBlocks.get(randomGenerator.nextInt(RandomBlocks.size()));
	}
	
	public static List<Integer> getRandomBlocks()
	{
		return RandomBlocks;
	}
	
	public static void shuffleRandomBlocks()
	{
		synchronized(RandomBlocks)
		{
			Collections.shuffle(RandomBlocks);
		}
	}
	
	
	
	//Set

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
	
	public static void setSeed(long data)
	{
		setData("Integers.ScrambleSeed", data);
		Seed = data;
	}
	
	public static void setUseChunkScrambler(boolean data)
	{
		setData("Booleans.UseChunkScrambler", data);
		UseChunkScrambler = data;
	}
	
	public static void setUseProximityHider(boolean data)
	{
		setData("Booleans.UseProximityHider", data);
		UseProximityHider = data;
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
	
	public static void setVerboseMode(boolean data)
	{
		setData("Booleans.VerboseMode", data);
		VerboseMode = data;
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
	
	public static void setDisabledWorlds(String name, boolean data)
	{
		if(!data)
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
		if(Orebfuscator.instance.getConfig().get(path) == null)
			setData(path, defaultData);
		return Orebfuscator.instance.getConfig().getBoolean(path, defaultData);
	}
	
	private static int getInt(String path, int defaultData)
	{
		if(Orebfuscator.instance.getConfig().get(path) == null)
			setData(path, defaultData);
		return Orebfuscator.instance.getConfig().getInt(path, defaultData);
	}
	
	private static long getLong(String path, long defaultData)
	{
		if(Orebfuscator.instance.getConfig().get(path) == null)
			setData(path, defaultData);
		return Orebfuscator.instance.getConfig().getLong(path, defaultData);
	}
	
	private static List<Integer> getIntList(String path, List<Integer> defaultData)
	{
		if(Orebfuscator.instance.getConfig().get(path) == null)
			setData(path, defaultData);
		return Orebfuscator.instance.getConfig().getIntegerList(path);
	}
	
	private static List<String> getStringList(String path, List<String> defaultData)
	{
		if(Orebfuscator.instance.getConfig().get(path) == null)
			setData(path, defaultData);
		return Orebfuscator.instance.getConfig().getStringList(path);
	}
	
	private static void setData(String path, Object data)
	{
		try{
			Orebfuscator.instance.getConfig().set(path, data);
	    	save();
		}
		catch (Exception e) { Orebfuscator.log(e); }
	}
	/*
	private static byte[] IntListToByteArray(List<Integer> list)
	{
		byte[] byteArray = new byte[list.size()];
	    for (int i=0; i < byteArray.length; i++)
	    {
	    	byteArray[i] = (byte)(int)list.get(i);
	    }
	    return byteArray;
	}*/
	
	private static TByteHashSet IntListToTByteHashSet(List<Integer> list)
	{
		TByteHashSet bytes = new TByteHashSet();
	    for (int i=0; i < list.size(); i++)
	    {
	    	bytes.add((byte)(int)list.get(i));
	    }
	    return bytes;
	}
	
	public static void load()
	{
		EngineMode = getInt("Integers.EngineMode", EngineMode);
		if(EngineMode != 1 && EngineMode != 2)
		{
			EngineMode = 2;
			System.out.println("[Orebfuscator] EngineMode must be 1 or 2.");
		}
		UpdateRadius = getInt("Integers.UpdateRadius", UpdateRadius);
		InitialRadius = getInt("Integers.InitialRadius", InitialRadius);
		if(InitialRadius > 5)
		{
			InitialRadius = 5;
			System.out.println("[Orebfuscator] InitialRadius must be less than 6.");
		}
		if(InitialRadius == 0)
		{
			System.out.println("[Orebfuscator] Warning, InitialRadius is 0. This will cause all exposed blocks to be obfuscated.");
		}
		ProcessingThreads = getInt("Integers.ProcessingThreads", ProcessingThreads);
		MaxLoadedCacheFiles = getInt("Integers.MaxLoadedCacheFiles", MaxLoadedCacheFiles);
		ProximityHiderDistance = getInt("Integers.ProximityHiderDistance", ProximityHiderDistance);
		Seed = getLong("Integers.ScrambleSeed", Seed);
		UseChunkScrambler = getBoolean("Booleans.UseChunkScrambler", UseChunkScrambler);
		UseProximityHider = getBoolean("Booleans.UseProximityHider", UseProximityHider);
		UpdateOnBreak = getBoolean("Booleans.UpdateOnBreak", UpdateOnBreak);
		UpdateOnDamage = getBoolean("Booleans.UpdateOnDamage", UpdateOnDamage);
		UpdateOnPhysics = getBoolean("Booleans.UpdateOnPhysics", UpdateOnPhysics);
		UpdateOnExplosion = getBoolean("Booleans.UpdateOnExplosion", UpdateOnExplosion);
		UpdateOnHoe = getBoolean("Booleans.UpdateOnHoe", UpdateOnHoe);
		UpdateThread = getBoolean("Booleans.UpdateThread", UpdateThread);
		DarknessHideBlocks = getBoolean("Booleans.DarknessHideBlocks", DarknessHideBlocks);
		VerboseMode = getBoolean("Booleans.VerboseMode", VerboseMode);
		NoObfuscationForOps = getBoolean("Booleans.NoObfuscationForOps", NoObfuscationForOps);
		NoObfuscationForPermission = getBoolean("Booleans.NoObfuscationForPermission", NoObfuscationForPermission);
		UseCache = getBoolean("Booleans.UseCache", UseCache);
		Enabled = getBoolean("Booleans.Enabled", Enabled);
		TransparentBlocks = IntListToTByteHashSet(getIntList("Lists.TransparentBlocks", Arrays.asList(new Integer[]{6,8,9,10,11,18,20,26,27,28,30,31,32,34,37,38,39,40,44,50,51,52,53,54,55,59,63,64,65,66,67,68,69,70,71,72,75,76,77,78,79,81,83,85,90,92,93,94,96,101,102,104,105,106,107,108,109,111,113,114,115,116,117,118,119,120,122})));
		ObfuscateBlocks = IntListToTByteHashSet(getIntList("Lists.ObfuscateBlocks", Arrays.asList(new Integer[]{14,15,16,21,54,56,73,74})));
		DarknessObfuscateBlocks = IntListToTByteHashSet(getIntList("Lists.DarknessObfuscateBlocks", Arrays.asList(new Integer[]{48,52})));
		ProximityHiderBlocks = IntListToTByteHashSet(getIntList("Lists.ProximityHiderBlocks", Arrays.asList(new Integer[]{54, 61, 62})));
		RandomBlocks = getIntList("Lists.RandomBlocks", Arrays.asList(new Integer[]{1,5,14,15,16,21,48,56,73}));
		DisabledWorlds = getStringList("Lists.DisabledWorlds", DisabledWorlds);
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
		return OrebfuscatorConfig.getNoObfuscationForOps() && player.isOp();
	}
	
	public static boolean playerBypassPerms(Player player)
	{
		return OrebfuscatorConfig.getNoObfuscationForPermission() && PermissionRelay.hasPermission(player, "Orebfuscator.deobfuscate");
	}
}
