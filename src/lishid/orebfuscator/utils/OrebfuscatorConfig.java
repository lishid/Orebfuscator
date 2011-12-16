package lishid.orebfuscator.utils;

import gnu.trove.set.hash.TByteHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

import lishid.orebfuscator.Orebfuscator;

public class OrebfuscatorConfig {
	private static TByteHashSet TransparentBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{6,8,9,10,11,18,20,26,27,28,30,31,32,34,37,38,39,40,44,50,51,52,53,54,55,59,63,64,65,66,67,68,69,70,71,72,75,76,77,78,79,81,83,85,90,92,93,94,96,101,102,104,105,106,107,108,109,111,113,114,115}));
	private static TByteHashSet ObfuscateBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{14,15,16,21,54,56,73,74}));
	private static TByteHashSet DarknessObfuscateBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{48,52}));
	private static TByteHashSet LightEmissionBlocks = IntListToTByteHashSet(Arrays.asList(new Integer[]{10,11,50,51,62,74,76,89,90,91,94}));
	private static byte[] RandomBlocks = {5,14,15,16,21,48,56,73};
	private static List<String> DisabledWorlds = new ArrayList<String>();
	private static final Random randomGenerator = new Random();
	private static int EngineMode = 2;
	private static int UpdateRadius = 2;
	private static int InitialRadius = 1;
	private static int ProcessingThreads = 1;
	private static boolean UpdateOnBreak = true;
	private static boolean UpdateOnDamage = true;
	private static boolean UpdateOnPhysics = true;
	private static boolean UpdateOnExplosion = true;
	private static boolean UpdateOnHoe = true;
	private static boolean DarknessHideBlocks = true;
	private static boolean NoObfuscationForOps = true;
	private static boolean NoObfuscationForPermission = true;
	private static boolean UseCache = true;
	private static boolean Enabled = true;
	
	//Get
	public static int EngineMode()
	{
		return EngineMode;
	}
	
	public static int UpdateRadius()
	{
		return UpdateRadius;
	}
	
	public static int InitialRadius()
	{
	 	if(InitialRadius < 0)
	 	 	 return 0;
		return InitialRadius;
	}
	
	public static int ProcessingThreads()
	{
		if(ProcessingThreads <= 0)
			return 1;
		return ProcessingThreads;
	}
	
	public static boolean UpdateOnBreak()
	{
		return UpdateOnBreak;
	}
	
	public static boolean UpdateOnDamage()
	{
		return UpdateOnDamage;
	}
	
	public static boolean UpdateOnPhysics()
	{
		return UpdateOnPhysics;
	}
	
	public static boolean UpdateOnExplosion()
	{
		return UpdateOnExplosion;
	}
	
	public static boolean UpdateOnHoe()
	{
		return UpdateOnHoe;
	}
	
	public static boolean DarknessHideBlocks()
	{
		return DarknessHideBlocks;
	}
	
	public static boolean NoObfuscationForOps()
	{
		return NoObfuscationForOps;
	}
	
	public static boolean NoObfuscationForPermission()
	{
		return NoObfuscationForPermission;
	}
	
	public static boolean UseCache()
	{
		return UseCache;
	}
	
	public static boolean Enabled()
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

	public static boolean emitsLight(byte id)
	{
	    if(LightEmissionBlocks.contains(id))
	    	return true;
	    return false;
	}
	
	public static boolean worldDisabled(String name)
	{
		for(String world : DisabledWorlds)
		{
			if(world.equalsIgnoreCase(name))
				return true;
		}
	    return false;
	}
	
	public static String disabledWorlds()
	{
		String retval = "";
		for(String world : DisabledWorlds)
		{
			retval += world + ", ";
		}
		return retval.length() > 1 ? retval.substring(0,  retval.length() - 2) : retval;
	}
	
	public static byte GenerateRandomBlock()
	{
		return RandomBlocks[randomGenerator.nextInt(RandomBlocks.length)];
	}
	
	//Set

	public static void SetEngineMode(int data)
	{
		SetData("Integers.EngineMode", data);
		EngineMode = data;
	}
	
	public static void SetUpdateRadius(int data)
	{
		SetData("Integers.UpdateRadius", data);
		UpdateRadius = data;
	}
	
	public static void SetInitialRadius(int data)
	{
		SetData("Integers.InitialRadius", data);
		InitialRadius = data;
	}
	
	public static void SetProcessingThreads(int data)
	{
		SetData("Integers.ProcessingThreads", data);
		ProcessingThreads = data;
	}
	
	public static void SetUpdateOnBreak(boolean data)
	{
		SetData("Booleans.UpdateOnBreak", data);
		UpdateOnBreak = data;
	}
	
	public static void SetUpdateOnDamage(boolean data)
	{
		SetData("Booleans.UpdateOnDamage", data);
		UpdateOnDamage = data;
	}
	
	public static void SetUpdateOnPhysics(boolean data)
	{
		SetData("Booleans.UpdateOnPhysics", data);
		UpdateOnPhysics = data;
	}
	
	public static void SetUpdateOnExplosion(boolean data)
	{
		SetData("Booleans.UpdateOnExplosion", data);
		UpdateOnExplosion = data;
	}
	
	public static void SetUpdateOnHoe(boolean data)
	{
		SetData("Booleans.UpdateOnHoe", data);
		UpdateOnHoe = data;
	}
	
	public static void SetDarknessHideBlocks(boolean data)
	{
		SetData("Booleans.DarknessHideBlocks", data);
		DarknessHideBlocks = data;
	}
	
	public static void SetNoObfuscationForOps(boolean data)
	{
		SetData("Booleans.NoObfuscationForOps", data);
		NoObfuscationForOps = data;
	}
	
	public static void SetNoObfuscationForPermission(boolean data)
	{
		SetData("Booleans.NoObfuscationForPermission", data);
		NoObfuscationForPermission = data;
	}
	
	public static void SetUseCache(boolean data)
	{
		SetData("Booleans.UseCache", data);
		UseCache = data;
	}
	
	public static void SetEnabled(boolean data)
	{
		SetData("Booleans.Enabled", data);
		Enabled = data;
	}
	
	public static void SetDisabledWorlds(String name, boolean data)
	{
		if(!data)
		{
			DisabledWorlds.remove(name);
		}
		else
		{
			DisabledWorlds.add(name);
		}
		SetData("Lists.DisabledWorlds", DisabledWorlds);
	}
	
	private static boolean GetBoolean(String path, boolean defaultData)
	{
		if(Orebfuscator.mainPlugin.getConfig().get(path) == null)
			SetData(path, defaultData);
		return Orebfuscator.mainPlugin.getConfig().getBoolean(path, defaultData);
	}
	
	private static int GetInt(String path, int defaultData)
	{
		if(Orebfuscator.mainPlugin.getConfig().get(path) == null)
			SetData(path, defaultData);
		return Orebfuscator.mainPlugin.getConfig().getInt(path, defaultData);
	}
	
	private static List<Integer> GetIntList(String path, List<Integer> defaultData)
	{
		if(Orebfuscator.mainPlugin.getConfig().get(path) == null)
			SetData(path, defaultData);
		return Orebfuscator.mainPlugin.getConfig().getIntegerList(path);
	}
	
	private static List<String> GetStringList(String path, List<String> defaultData)
	{
		if(Orebfuscator.mainPlugin.getConfig().get(path) == null)
			SetData(path, defaultData);
		return Orebfuscator.mainPlugin.getConfig().getStringList(path);
	}
	
	private static void SetData(String path, Object data)
	{
		try{
			Orebfuscator.mainPlugin.getConfig().set(path, data);
	    	Save();
		}catch(Exception e){}
	}
	
	private static byte[] IntListToByteArray(List<Integer> list)
	{
		byte[] byteArray = new byte[list.size()];
	    for (int i=0; i < byteArray.length; i++)
	    {
	    	byteArray[i] = (byte)(int)list.get(i);
	    }
	    return byteArray;
	}
	
	private static TByteHashSet IntListToTByteHashSet(List<Integer> list)
	{
		TByteHashSet bytes = new TByteHashSet();
	    for (int i=0; i < list.size(); i++)
	    {
	    	bytes.add((byte)(int)list.get(i));
	    }
	    return bytes;
	}
	
	public static void Load()
	{
		EngineMode = GetInt("Integers.EngineMode", EngineMode);
		if(EngineMode != 1 && EngineMode != 2)
		{
			EngineMode = 1;
			System.out.println("[Orebfuscator] EngineMode must be 1 or 2.");
		}
		UpdateRadius = GetInt("Integers.UpdateRadius", UpdateRadius);
		InitialRadius = GetInt("Integers.InitialRadius", InitialRadius);
		if(InitialRadius > 4)
		{
			InitialRadius = 4;
			System.out.println("[Orebfuscator] InitialRadius must be less than 5.");
		}
		ProcessingThreads = GetInt("Integers.ProcessingThreads", ProcessingThreads);
		UpdateOnBreak = GetBoolean("Booleans.UpdateOnBreak", UpdateOnBreak);
		UpdateOnDamage = GetBoolean("Booleans.UpdateOnDamage", UpdateOnDamage);
		UpdateOnPhysics = GetBoolean("Booleans.UpdateOnPhysics", UpdateOnPhysics);
		UpdateOnExplosion = GetBoolean("Booleans.UpdateOnExplosion", UpdateOnExplosion);
		UpdateOnHoe = GetBoolean("Booleans.UpdateOnHoe", UpdateOnHoe);
		DarknessHideBlocks = GetBoolean("Booleans.DarknessHideBlocks", DarknessHideBlocks);
		NoObfuscationForOps = GetBoolean("Booleans.NoObfuscationForOps", NoObfuscationForOps);
		NoObfuscationForPermission = GetBoolean("Booleans.NoObfuscationForPermission", NoObfuscationForPermission);
		UseCache = GetBoolean("Booleans.UseCache", UseCache);
		Enabled = GetBoolean("Booleans.Enabled", Enabled);
		TransparentBlocks = IntListToTByteHashSet(GetIntList("Lists.TransparentBlocks", Arrays.asList(new Integer[]{6,8,9,10,11,18,20,26,27,28,30,31,32,34,37,38,39,40,44,50,51,52,53,54,55,59,63,64,65,66,67,68,69,70,71,72,75,76,77,78,79,81,83,85,90,92,93,94,96,101,102,104,105,106,107,108,109,111,113,114,115})));
		ObfuscateBlocks = IntListToTByteHashSet(GetIntList("Lists.ObfuscateBlocks", Arrays.asList(new Integer[]{14,15,16,21,54,56,73,74})));
		DarknessObfuscateBlocks = IntListToTByteHashSet(GetIntList("Lists.DarknessObfuscateBlocks", Arrays.asList(new Integer[]{48,52})));
		LightEmissionBlocks = IntListToTByteHashSet(GetIntList("Lists.LightEmissionBlocks", Arrays.asList(new Integer[]{10,11,50,51,62,74,76,89,90,91,94})));
		RandomBlocks = IntListToByteArray(GetIntList("Lists.RandomBlocks", Arrays.asList(new Integer[]{5,14,15,16,21,48,56,73})));
		DisabledWorlds = GetStringList("Lists.DisabledWorlds", DisabledWorlds);
		Save();
	}
	
	public static void Reload()
	{
		Load();
	}
	
	public static void Save()
	{
		Orebfuscator.mainPlugin.saveConfig();
	}
}
