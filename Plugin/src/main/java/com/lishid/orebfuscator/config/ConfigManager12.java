/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.orebfuscator.DeprecatedMethods;

public class ConfigManager12 {
    private static final int CONFIG_VERSION = 12;
	
    private boolean[] transparentBlocks;
    private boolean[] transparentBlocksMode1;
    private boolean[] transparentBlocksMode2;
    
    private JavaPlugin plugin;
    private Logger logger;
    private OrebfuscatorConfig orebfuscatorConfig;
    
    public ConfigManager12(JavaPlugin plugin, Logger logger, OrebfuscatorConfig orebfuscatorConfig) {
    	this.plugin = plugin;
		this.logger = logger;
		this.orebfuscatorConfig = orebfuscatorConfig;
    }

	public void load() {
        // Version check
        int version = getInt("ConfigVersion", CONFIG_VERSION);
        if (version < CONFIG_VERSION) {
            getConfig().set("ConfigVersion", CONFIG_VERSION);
        }
        
        boolean useCache = getBoolean("Booleans.UseCache", true);
        int maxLoadedCacheFiles = getInt("Integers.MaxLoadedCacheFiles", 64, 16, 128);
        String cacheLocation = getString("Strings.CacheLocation", "orebfuscator_cache");
        int deleteCacheFilesAfterDays = getInt("Integers.DeleteCacheFilesAfterDays", 0);
        boolean enabled = getBoolean("Booleans.Enabled", true);
        boolean updateOnDamage = getBoolean("Booleans.UpdateOnDamage", true);

        int engineMode = getInt("Integers.EngineMode", 2);
        if (engineMode != 1 && engineMode != 2) {
            engineMode = 2;
            logger.info("EngineMode must be 1 or 2.");
        }
        
        int initialRadius = getInt("Integers.InitialRadius", 1, 0, 2);
        if (initialRadius == 0) {
            logger.info("Warning, InitialRadius is 0. This will cause all exposed blocks to be obfuscated.");
        }

        int updateRadius = getInt("Integers.UpdateRadius", 2, 1, 5);
        boolean noObfuscationForMetadata = getBoolean("Booleans.NoObfuscationForMetadata", true);
        String noObfuscationForMetadataTagName = getString("Strings.NoObfuscationForMetadataTagName", "NPC");
        boolean noObfuscationForOps = getBoolean("Booleans.NoObfuscationForOps", false);
        boolean noObfuscationForPermission = getBoolean("Booleans.NoObfuscationForPermission", false);
        boolean loginNotification = getBoolean("Booleans.LoginNotification", true);

        generateTransparentBlocks(engineMode);
        
        WorldConfig defaultWorld = readDefaultWorld();
        WorldConfig normalWorld = readNormalWorld(defaultWorld);
        WorldConfig netherWorld = readNetherWorld(defaultWorld);
        Map<World, WorldConfig> worlds = readWorldList(normalWorld, netherWorld);
        
        this.orebfuscatorConfig.setUseCache(useCache);
        this.orebfuscatorConfig.setMaxLoadedCacheFiles(maxLoadedCacheFiles);
        this.orebfuscatorConfig.setCacheLocation(cacheLocation);
        this.orebfuscatorConfig.setDeleteCacheFilesAfterDays(deleteCacheFilesAfterDays);
        this.orebfuscatorConfig.setEnabled(enabled);
        this.orebfuscatorConfig.setUpdateOnDamage(updateOnDamage);
        this.orebfuscatorConfig.setEngineMode(engineMode);
        this.orebfuscatorConfig.setInitialRadius(initialRadius);
        this.orebfuscatorConfig.setUpdateRadius(updateRadius);
        this.orebfuscatorConfig.setNoObfuscationForMetadata(noObfuscationForMetadata);
        this.orebfuscatorConfig.setNoObfuscationForMetadataTagName(noObfuscationForMetadataTagName);
        this.orebfuscatorConfig.setNoObfuscationForOps(noObfuscationForOps);
        this.orebfuscatorConfig.setNoObfuscationForPermission(noObfuscationForPermission);
        this.orebfuscatorConfig.setLoginNotification(loginNotification);
        this.orebfuscatorConfig.setTransparentBlocks(this.transparentBlocks);
        this.orebfuscatorConfig.setDefaultWorld(defaultWorld);
        this.orebfuscatorConfig.setNormalWorld(normalWorld);
        this.orebfuscatorConfig.setEndWorld(normalWorld.clone());
        this.orebfuscatorConfig.setNetherWorld(netherWorld);
        this.orebfuscatorConfig.setWorlds(worlds);
        this.orebfuscatorConfig.setProximityHiderEnabled();
        
        save();
	}
	
    public void setEngineMode(int value) {
        getConfig().set("Integers.EngineMode", value);
        save();
        this.orebfuscatorConfig.setEngineMode(value);
    }

    public void setUpdateRadius(int value) {
        getConfig().set("Integers.UpdateRadius", value);
        save();
        this.orebfuscatorConfig.setUpdateRadius(value);
    }

    public void setInitialRadius(int value) {
        getConfig().set("Integers.InitialRadius", value);
        save();
        this.orebfuscatorConfig.setInitialRadius(value);
    }

    public void setProximityHiderDistance(int value) {
        getConfig().set("Integers.ProximityHiderDistance", value);
        save();
        this.orebfuscatorConfig.getDefaultWorld().getProximityHiderConfig().setDistance(value);
    }

    public void setAirGeneratorMaxChance(int value) {
        getConfig().set("Integers.AirGeneratorMaxChance", value);
        save();
        
        this.orebfuscatorConfig.getDefaultWorld().setAirGeneratorMaxChance(value);
        this.orebfuscatorConfig.getNormalWorld().setAirGeneratorMaxChance(value);
        this.orebfuscatorConfig.getEndWorld().setAirGeneratorMaxChance(value);
        this.orebfuscatorConfig.getNetherWorld().setAirGeneratorMaxChance(value);
        
        for(WorldConfig world : this.orebfuscatorConfig.getWorlds().values()) {
        	world.setAirGeneratorMaxChance(value);
        }
    }

    public void setUseProximityHider(boolean value) {
        getConfig().set("Booleans.UseProximityHider", value);
        save();
        this.orebfuscatorConfig.getDefaultWorld().getProximityHiderConfig().setEnabled(value);
        this.orebfuscatorConfig.setProximityHiderEnabled();
    }

    public void setDarknessHideBlocks(boolean value) {
        getConfig().set("Booleans.DarknessHideBlocks", value);
        save();
        
        this.orebfuscatorConfig.getDefaultWorld().setDarknessHideBlocks(value);
        this.orebfuscatorConfig.getNormalWorld().setDarknessHideBlocks(value);
        this.orebfuscatorConfig.getEndWorld().setDarknessHideBlocks(value);
        this.orebfuscatorConfig.getNetherWorld().setDarknessHideBlocks(value);
        
        for(WorldConfig world : this.orebfuscatorConfig.getWorlds().values()) {
        	world.setDarknessHideBlocks(value);
        }
    }

    public void setNoObfuscationForOps(boolean value) {
        getConfig().set("Booleans.NoObfuscationForOps", value);
        save();
        this.orebfuscatorConfig.setNoObfuscationForOps(value);
    }

    public void setNoObfuscationForPermission(boolean value) {
        getConfig().set("Booleans.NoObfuscationForPermission", value);
        save();
        this.orebfuscatorConfig.setNoObfuscationForPermission(value);
    }

    public void setLoginNotification(boolean value) {
        getConfig().set("Booleans.LoginNotification", value);
        save();
        this.orebfuscatorConfig.setLoginNotification(value);
    }

    public void setAntiTexturePackAndFreecam(boolean value) {
        getConfig().set("Booleans.AntiTexturePackAndFreecam", value);
        save();
        
        this.orebfuscatorConfig.getDefaultWorld().setAntiTexturePackAndFreecam(value);
        this.orebfuscatorConfig.getNormalWorld().setAntiTexturePackAndFreecam(value);
        this.orebfuscatorConfig.getEndWorld().setAntiTexturePackAndFreecam(value);
        this.orebfuscatorConfig.getNetherWorld().setAntiTexturePackAndFreecam(value);
        
        for(WorldConfig world : this.orebfuscatorConfig.getWorlds().values()) {
        	world.setAntiTexturePackAndFreecam(value);
        }
    }

    public void setUseCache(boolean value) {
        getConfig().set("Booleans.UseCache", value);
        save();
        this.orebfuscatorConfig.setUseCache(value);
    }

    public void setEnabled(boolean value) {
        getConfig().set("Booleans.Enabled", value);
        save();
        this.orebfuscatorConfig.setEnabled(value);
    }

    public void setUseWorldsAsBlacklist(boolean value) {
        getConfig().set("Booleans.UseWorldsAsBlacklist", value);
        save();
        
        this.orebfuscatorConfig.getDefaultWorld().setEnabled(value);
        this.orebfuscatorConfig.getNormalWorld().setEnabled(value);
        this.orebfuscatorConfig.getEndWorld().setEnabled(value);
        this.orebfuscatorConfig.getNetherWorld().setEnabled(value);
        
        for(WorldConfig world : this.orebfuscatorConfig.getWorlds().values()) {
        	world.setEnabled(!value);
        }
    }

    public void setWorldEnabled(String name, boolean enabled) {
    	World world = Bukkit.getServer().getWorld(name);
    	
    	if(world == null) return;
    	
    	boolean useWorldsAsBlacklist = this.orebfuscatorConfig.getDefaultWorld().isEnabled();
    	
    	if (enabled && !useWorldsAsBlacklist || !enabled && useWorldsAsBlacklist) {
        	WorldConfig defaultConfig = world.getEnvironment() == Environment.NETHER
        			? this.orebfuscatorConfig.getNetherWorld()
        			: this.orebfuscatorConfig.getNormalWorld();
        			
        	WorldConfig cfg = defaultConfig.clone();
        	
        	cfg.setEnabled(!cfg.isEnabled());
    		
    		this.orebfuscatorConfig.getWorlds().put(world, cfg);
        } else {
        	this.orebfuscatorConfig.getWorlds().remove(world);
        }
    	
    	List<String> worldNames = new ArrayList<String>();
    	
    	for(World current : this.orebfuscatorConfig.getWorlds().keySet()) {
    		worldNames.add(current.getName());
    	}
    	
        getConfig().set("Lists.Worlds", worldNames);
        save();
    }
    
    private FileConfiguration getConfig() {
    	return this.plugin.getConfig();
    }
    
    private void save() {
    	this.plugin.saveConfig();
    }
	
	private WorldConfig readDefaultWorld() {
		boolean enabled = getBoolean("Booleans.UseWorldsAsBlacklist", true);
	    boolean darknessHideBlocks = getBoolean("Booleans.DarknessHideBlocks", false);
	    boolean antiTexturePackAndFreecam = getBoolean("Booleans.AntiTexturePackAndFreecam", true);
	    int airGeneratorMaxChance = getInt("Integers.AirGeneratorMaxChance", 43, 40, 100);
	    boolean[] darknessBlocks = new boolean[256];
	    ProximityHiderConfig proximityHiderConfig = readProxmityHider();
	    
        setBlockValues(darknessBlocks, getMaterialIdsByPath("Lists.DarknessBlocks", new Integer[]{52, 54}));

        WorldConfig cfg = new WorldConfig();
	    cfg.setEnabled(enabled);
	    cfg.setDarknessHideBlocks(darknessHideBlocks);
	    cfg.setAntiTexturePackAndFreecam(antiTexturePackAndFreecam);
	    cfg.setAirGeneratorMaxChance(airGeneratorMaxChance);
	    cfg.setObfuscateBlocks(null);
	    cfg.setDarknessBlocks(darknessBlocks);
	    cfg.setRandomBlocks(null);
	    cfg.setPaletteBlocks(null);
	    cfg.setProximityHiderConfig(proximityHiderConfig);

	    return cfg;
	}
	
	private ProximityHiderConfig readProxmityHider() {
	    boolean enabled = getBoolean("Booleans.UseProximityHider", true);
	    int distance = getInt("Integers.ProximityHiderDistance", 8, 2, 64);
	    int specialBlockID = getMaterialIdByPath("Integers.ProximityHiderID", 1);
	    int endY = getInt("Integers.ProximityHiderEnd", 255, 0, 255);
	    boolean useSpecialBlock = getBoolean("Booleans.UseSpecialBlockForProximityHider", true);
	    boolean useYLocationProximity = getBoolean("Booleans.UseYLocationProximity", false); 
	    boolean[] proximityHiderBlocks = new boolean[256];
	    
        setBlockValues(proximityHiderBlocks, getMaterialIdsByPath("Lists.ProximityHiderBlocks", new Integer[]{23, 52, 54, 56, 58, 61, 62, 116, 129, 130, 145, 146}));
		
		ProximityHiderConfig cfg = new ProximityHiderConfig();
	    cfg.setEnabled(enabled);
	    cfg.setDistance(distance);
	    cfg.setSpecialBlockID(specialBlockID);
	    cfg.setEndY(endY);
	    cfg.setUseSpecialBlock(useSpecialBlock);
	    cfg.setUseYLocationProximity(useYLocationProximity); 
	    cfg.setProximityHiderBlocks(proximityHiderBlocks);
	    
	    return cfg;
	}
	
	private WorldConfig readNormalWorld(WorldConfig defaultConfig) {
		boolean[] obfuscateBlocks = new boolean[256];
		Integer[] randomBlocks = getMaterialIdsByPath("Lists.RandomBlocks", new Integer[]{1, 4, 5, 14, 15, 16, 21, 46, 48, 49, 56, 73, 82, 129}).toArray(new Integer[0]); 
		
		setBlockValues(obfuscateBlocks, getMaterialIdsByPath("Lists.ObfuscateBlocks", new Integer[]{14, 15, 16, 21, 54, 56, 73, 74, 129, 130}), false);
		
		obfuscateBlocks[1] = true;
		
		WorldConfig cfg = defaultConfig.clone();
		cfg.setObfuscateBlocks(obfuscateBlocks);
		cfg.setRandomBlocks(randomBlocks);
		cfg.setMode1BlockId(1);
		
		createPaletteBlocks(cfg);
		
		return cfg;
	}
	
	private WorldConfig readNetherWorld(WorldConfig defaultConfig) {
		boolean[] obfuscateBlocks = new boolean[256];
		Integer[] randomBlocks = getMaterialIdsByPath("Lists.NetherRandomBlocks", new Integer[]{13, 87, 88, 112, 153}).toArray(new Integer[0]); 
		
		setBlockValues(obfuscateBlocks, getMaterialIdsByPath("Lists.NetherObfuscateBlocks", new Integer[]{87, 153}), false);
		
		obfuscateBlocks[87] = true;
		
		WorldConfig cfg = defaultConfig.clone();
		cfg.setObfuscateBlocks(obfuscateBlocks);
		cfg.setRandomBlocks(randomBlocks);
		cfg.setMode1BlockId(87);
		
		createPaletteBlocks(cfg);
		
		return cfg;
	}
	
	private Map<World, WorldConfig> readWorldList(WorldConfig normalWorld, WorldConfig netherWorld) {
		Map<World, WorldConfig> worlds = new HashMap<World, WorldConfig>();
		List<String> worldNames = new ArrayList<String>();
		Server server = Bukkit.getServer(); 

        //Support old DisabledWorlds value
        if(getConfig().get("Lists.DisabledWorlds") != null) {
        	worldNames = getConfig().getStringList("Lists.DisabledWorlds");
        	getConfig().set("Lists.DisabledWorlds", null);
        }

        // List of worlds (either disabled or enabled depending on WorldsAsBlacklist value).
        worldNames = getStringList("Lists.Worlds", new ArrayList<String>());
        
        for(String worldName : worldNames) {
        	World world = server.getWorld(worldName);
        	
        	if(world == null || worlds.containsKey(world)) continue;
        	
        	WorldConfig defaultConfig = world.getEnvironment() == Environment.NETHER ? netherWorld: normalWorld;
        	WorldConfig cfg = defaultConfig.clone();

        	cfg.setEnabled(!cfg.isEnabled());
        	
        	worlds.put(world, cfg);
        }
        
        return worlds;
	}

    private static void setBlockValues(boolean[] boolArray, List<Integer> blocks) {
        for (int i = 0; i < boolArray.length; i++) {
            boolArray[i] = blocks.contains(i);
        }
    }
    
    private void setBlockValues(boolean[] boolArray, List<Integer> blocks, boolean transparent) {
        for (int i = 0; i < boolArray.length; i++) {
            boolArray[i] = blocks.contains(i);

            // If block is transparent while we don't want them to, or the other way around
            if (transparent != isBlockTransparent((short) i)) {
                // Remove it
                boolArray[i] = false;
            }
        }
    }
    
    private void createPaletteBlocks(WorldConfig cfg) {
    	HashSet<Integer> map = new HashSet<Integer>();
    	
    	map.add(0);
    	
    	if(cfg.getProximityHiderConfig().isUseSpecialBlock()) {
    		map.add(cfg.getProximityHiderConfig().getSpecialBlockID());
    	}
    	
    	for(Integer id : cfg.getRandomBlocks()) {
    		if(id != null) {
    			map.add(id);
    		}
    	}
    	
    	int[] paletteBlocks = new int[map.size()];
    	int index = 0;
    	
    	for(Integer id : map) {
    		paletteBlocks[index++] = id;
    	}
    	
    	cfg.setPaletteBlocks(paletteBlocks);
    }
	
    private String getString(String path, String defaultData) {
        if (getConfig().get(path) == null)
            getConfig().set(path, defaultData);
        
        return getConfig().getString(path, defaultData);
    }

    private int getInt(String path, int defaultData) {
        if (getConfig().get(path) == null)
            getConfig().set(path, defaultData);
        
        return getConfig().getInt(path, defaultData);
    }
    
    private int getInt(String path, int defaultData, int min, int max) {
        if (getConfig().get(path) == null)
            getConfig().set(path, defaultData);
        
        int value = getConfig().getInt(path, defaultData);
        
        if(value < min) { 
        	value = min;
        }
        else if(value > max) {
        	value = max;
        }
        
        return value;
    }
    
    private boolean getBoolean(String path, boolean defaultData) {
        if (getConfig().get(path) == null)
        	getConfig().set(path, defaultData);
        
        return getConfig().getBoolean(path, defaultData);
    }

    private List<String> getStringList(String path, List<String> defaultData) {
        if (getConfig().get(path) == null)
            getConfig().set(path, defaultData);
        
        return getConfig().getStringList(path);
    }
    
    private int getMaterialIdByPath(String path, int defaultMaterialId) {
    	String materialName = getConfig().get(path) != null ? getConfig().getString(path): Integer.toString(defaultMaterialId);
    	MaterialResult material = getMaterial(materialName, defaultMaterialId);
    	
    	getConfig().set(path, material.name);
		
		return material.id;
    }
    
    private List<Integer> getMaterialIdsByPath(String path, Integer[] defaultMaterials) {
    	List<String> list;
    	
    	if(getConfig().get(path) != null) {
    		list = getConfig().getStringList(path);
    	} else {
    		list = new ArrayList<String>();
    		
    		for(int materialId : defaultMaterials) {
    			list.add(DeprecatedMethods.getMaterial(materialId).name());
    		}
    	}
    	
    	List<Integer> result = new ArrayList<Integer>();
    	
    	for(int i = 0; i < list.size(); i++) {
    		MaterialResult material = getMaterial(list.get(i), null);
    		
    		if(material != null) {
    			list.set(i, material.name);
    			result.add(material.id);
    		}
    	}
    	
    	getConfig().set(path, list);
    	
    	return result;
    }
    
    private MaterialResult getMaterial(String materialName, Integer defaultMaterialId) {
    	Integer materialId;
    	String defaultMaterialName = defaultMaterialId != null ? DeprecatedMethods.getMaterial(defaultMaterialId).name(): null;
    	
		try {
    		if(Character.isDigit(materialName.charAt(0))) {
    			materialId = Integer.parseInt(materialName);
    			
    			Material obj = DeprecatedMethods.getMaterial(materialId);
    			
    			if(obj != null) {
    				materialName = obj.name();
    			} else {
    				if(defaultMaterialId != null) {
	    				this.logger.info("Material with ID = " + materialId + " is not found. Will be used default material: " + defaultMaterialName);
	    				materialId = defaultMaterialId;
	    				materialName = defaultMaterialName;
    				} else {
    					this.logger.info("Material with ID = " + materialId + " is not found. Skipped.");
    					materialId = null;
    				}
    			}
    		} else {
    			Material obj = Material.getMaterial(materialName.toUpperCase());
    			
    			if(obj != null) {
    				materialId = DeprecatedMethods.getMaterialId(obj);
    			} else {
    				if(defaultMaterialId != null) {
	    				this.logger.info("Material " + materialName + " is not found. Will be used default material: " + defaultMaterialName);
	    				materialId = defaultMaterialId;
	    				materialName = defaultMaterialName;
    				} else {
    					this.logger.info("Material " + materialName + " is not found. Skipped.");
    					materialId = null;
    				}
    			}
    		}
		} catch (Exception e) {
			if(defaultMaterialId != null) {
				this.logger.info("Invalid material ID or name: " + materialName + ".  Will be used default material: " + defaultMaterialName);
				materialId = defaultMaterialId;
				materialName = defaultMaterialName;
			} else {
				this.logger.info("Invalid material ID or name: " + materialName + ". Skipped.");
				materialId = null;
			}
		}
		
		return materialId != null ? new MaterialResult(materialId, materialName): null;
    }
    
    private boolean isBlockTransparent(int id) {
        if (id < 0)
            id += 256;

        if (id >= 256) {
            return false;
        }

        return this.transparentBlocks[id];
    }
    
    private void generateTransparentBlocks(int engineMode) {
    	if(this.transparentBlocks == null) {
    		readInitialTransparentBlocks();
    	}
    	
    	boolean[] transparentBlocks = engineMode == 1
    			? this.transparentBlocksMode1
    			: this.transparentBlocksMode2;
    	
    	System.arraycopy(transparentBlocks, 0, this.transparentBlocks, 0, this.transparentBlocks.length);
    	
    	List<Integer> customTransparentBlocks = getMaterialIdsByPath("Lists.TransparentBlocks", new Integer[]{ });
    	
    	for(int blockId : customTransparentBlocks) {
    		if(blockId >= 0 && blockId <= 255) {
    			this.transparentBlocks[blockId] = true;
    		}
    	}
    	
    	List<Integer> customNonTransparentBlocks = getMaterialIdsByPath("Lists.NonTransparentBlocks", new Integer[]{ });

    	for(int blockId : customNonTransparentBlocks) {
    		if(blockId >= 0 && blockId <= 255) {
    			this.transparentBlocks[blockId] = false;
    		}
    	}
    }
    
    private void readInitialTransparentBlocks() {
    	this.transparentBlocks = new boolean[256];
    	Arrays.fill(this.transparentBlocks, false);
    	
		InputStream mainStream = ConfigManager12.class.getResourceAsStream("/resources/transparent_blocks.txt");
		readTransparentBlocks(this.transparentBlocks, mainStream);
    	
		this.transparentBlocksMode1 = new boolean[256];
		System.arraycopy(this.transparentBlocks, 0, this.transparentBlocksMode1, 0, this.transparentBlocksMode1.length);
		InputStream mode1Stream = ConfigManager12.class.getResourceAsStream("/resources/transparent_blocks_mode1.txt");
		if(mode1Stream != null) readTransparentBlocks(this.transparentBlocksMode1, mode1Stream);

		this.transparentBlocksMode2 = new boolean[256];
		System.arraycopy(this.transparentBlocks, 0, this.transparentBlocksMode2, 0, this.transparentBlocksMode2.length);
		InputStream mode2Stream = ConfigManager12.class.getResourceAsStream("/resources/transparent_blocks_mode2.txt");
		if(mode2Stream != null) readTransparentBlocks(this.transparentBlocksMode2, mode2Stream);
    }
    
    private void readTransparentBlocks(boolean[] transparentBlocks, InputStream stream) {
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    		String line;
    		
            while ((line = reader.readLine()) != null) { 
            	int index1 = line.indexOf(":");
            	int index2 = line.indexOf(" ", index1);
            	int blockId = Integer.parseInt(line.substring(0,  index1));
            	boolean isTransparent = line.substring(index1 + 1, index2).equals("true");
            	
            	transparentBlocks[blockId] = isTransparent;
            }
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}
