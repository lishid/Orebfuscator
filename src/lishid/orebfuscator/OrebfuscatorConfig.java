package lishid.orebfuscator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.util.config.Configuration;

public class OrebfuscatorConfig {
	private static Configuration config;
	private static Byte[] TransparentBlocks = {};
	private static Byte[] ObfuscateBlocks = {};
	private static Byte[] DarknessObfuscateBlocks = {};
	private static Byte[] LightEmissionBlocks = {};
	private static Byte[] RandomBlocks = {};
	private static final Random randomGenerator = new Random();
	private static int EngineMode;
	private static int UpdateRadius;
	private static int InitialRadius;
	private static boolean UpdateOnBreak;
	private static boolean UpdateOnDamage;
	private static boolean UpdateOnPhysics;
	private static boolean UpdateOnExplosion;
	private static boolean DarknessHideBlocks;
	private static boolean NoObfuscationForOps;
	private static boolean NoObfuscationForPermission;
	private static boolean Enabled;
	
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
		return InitialRadius;
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
	
	public static boolean Enabled()
	{
		return Enabled;
	}
	
	public static boolean isTransparent(byte id)
	{
	    if (id == 0) return true;
	    if (id == -127) return false;
	    
	    for (byte i : TransparentBlocks) {
	      if (id == i)
	    	  return true;
	    }
	    return false;
	}
	
	public static boolean isObfuscated(byte id)
	{
	    if (id == 1) return true;

	    for (byte i : ObfuscateBlocks) {
	      if (id == i)
	    	  return true;
	    }
	    return false;
	}
	
	public static boolean isDarknessObfuscated(byte id)
	{
	    for (byte i : DarknessObfuscateBlocks) {
	      if (id == i)
	    	  return true;
	    }
	    return false;
	}
	
	public static boolean emitsLight(byte id)
	{
	    for (byte i : LightEmissionBlocks) {
	      if (id == i)
	    	  return true;
	    }
	    return false;
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
	
	public static void UpdateOnPhysics(boolean data)
	{
		SetData("Booleans.UpdateOnPhysics", data);
		UpdateOnPhysics = data;
	}
	
	public static void SetUpdateOnExplosion(boolean data)
	{
		SetData("Booleans.UpdateOnExplosion", data);
		UpdateOnExplosion = data;
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
	
	public static void SetEnabled(boolean data)
	{
		SetData("Booleans.Enabled", data);
		Enabled = data;
	}
	
	private static boolean GetBoolean(String path, boolean defaultData)
	{
		if(config.getProperty(path) == null)
			SetData(path, defaultData);
		return config.getBoolean(path, defaultData);
	}
	
	private static int GetInt(String path, int defaultData)
	{
		if(config.getProperty(path) == null)
			SetData(path, defaultData);
		return config.getInt(path, defaultData);
	}
	
	private static List<Integer> GetIntList(String path, List<Integer> defaultData)
	{
		if(config.getProperty(path) == null)
			SetData(path, defaultData);
		return (List<Integer>) config.getIntList(path, defaultData);
	}
	
	private static void SetData(String path, Object data)
	{
    	config.setProperty(path, data);
    	config.save();
	}
	
	private static Byte[] IntListToByteArray(List<Integer> list)
	{
		Byte[] byteArray = new Byte[list.size()];
	    for (int i=0; i < byteArray.length; i++)
	    {
	    	byteArray[i] = (byte)(int)list.get(i);
	    }
	    return byteArray;
	}
	
	public static void Load(Configuration config)
	{
		OrebfuscatorConfig.config = config;
		EngineMode = GetInt("Integers.EngineMode", 2);
		if(EngineMode != 1 && EngineMode != 2)
		{
			EngineMode = 1;
			System.out.println("[Orebfuscator] EngineMode must be 1 or 2.");
		}
		UpdateRadius = GetInt("Integers.UpdateRadius", 2);
		InitialRadius = GetInt("Integers.InitialRadius", 1);
		if(InitialRadius > 4)
		{
			InitialRadius = 4;
			System.out.println("[Orebfuscator] InitialRadius must be less than 5.");
		}
		UpdateOnBreak = GetBoolean("Booleans.UpdateOnBreak", true);
		UpdateOnDamage = GetBoolean("Booleans.UpdateOnDamage", true);
		UpdateOnPhysics = GetBoolean("Booleans.UpdateOnPhysics", true);
		UpdateOnExplosion = GetBoolean("Booleans.UpdateOnExplosion", true);
		DarknessHideBlocks = GetBoolean("Booleans.DarknessHideBlocks", true);
		NoObfuscationForOps = GetBoolean("Booleans.NoObfuscationForOps", true);
		NoObfuscationForPermission = GetBoolean("Booleans.NoObfuscationForPermission", true);
		Enabled = GetBoolean("Booleans.Enabled", true);
		TransparentBlocks = IntListToByteArray(GetIntList("Lists.TransparentBlocks", Arrays.asList(new Integer[]{6,8,9,10,11,18,20,26,27,28,30,31,32,34,37,38,39,40,44,50,51,52,53,54,55,59,63,64,65,66,67,68,69,70,71,72,75,76,77,78,79,81,83,85,90,92,93,94,96,101,102,104,105,106,107,108,109,111,113,114,115})));
		ObfuscateBlocks = IntListToByteArray(GetIntList("Lists.ObfuscateBlocks", Arrays.asList(new Integer[]{14,15,16,21,54,56,73,74})));
		DarknessObfuscateBlocks = IntListToByteArray(GetIntList("Lists.DarknessObfuscateBlocks", Arrays.asList(new Integer[]{48,52})));
		LightEmissionBlocks = IntListToByteArray(GetIntList("Lists.LightEmissionBlocks", Arrays.asList(new Integer[]{10,11,50,51,62,74,76,89,90,91,94})));
		RandomBlocks = IntListToByteArray(GetIntList("Lists.RandomBlocks", Arrays.asList(new Integer[]{14,15,16,21,56,73})));
		config.save();
	}
	
	public static void Reload()
	{
		OrebfuscatorConfig.config.load();
		Load(OrebfuscatorConfig.config);
	}
	
	public static void Save()
	{
		config.save();
	}
}
