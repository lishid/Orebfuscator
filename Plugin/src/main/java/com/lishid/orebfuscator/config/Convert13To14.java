/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Convert13To14 {
	private static final int CONFIG_VERSION = 14;
	
    private JavaPlugin plugin;
    private FileConfiguration config;
    
    public Convert13To14(JavaPlugin plugin) {
    	this.plugin = plugin;
    }

    public void convert() {
    	this.config = new YamlConfiguration();
    	setGlobalValues();
    	setWorldValues();
    	
    	String contents = this.config.saveToString();
    	
    	try {
			this.plugin.getConfig().loadFromString(contents);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
    }
    
    private void setGlobalValues() {
    	FileConfiguration oldConfig = this.plugin.getConfig();
    	
        this.config.set("ConfigVersion", CONFIG_VERSION);
        this.config.set("Booleans.UseCache", oldConfig.get("Booleans.UseCache"));
        this.config.set("Booleans.Enabled", oldConfig.get("Booleans.Enabled"));
        this.config.set("Booleans.UpdateOnDamage", oldConfig.get("Booleans.UpdateOnDamage"));
        this.config.set("Booleans.NoObfuscationForMetadata", oldConfig.get("Booleans.NoObfuscationForMetadata"));
        this.config.set("Booleans.NoObfuscationForOps", oldConfig.get("Booleans.NoObfuscationForOps"));
        this.config.set("Booleans.NoObfuscationForPermission", oldConfig.get("Booleans.NoObfuscationForPermission"));
        this.config.set("Booleans.LoginNotification", oldConfig.get("Booleans.LoginNotification"));
        this.config.set("Integers.MaxLoadedCacheFiles", oldConfig.get("Integers.MaxLoadedCacheFiles"));
        this.config.set("Integers.DeleteCacheFilesAfterDays", oldConfig.get("Integers.DeleteCacheFilesAfterDays"));
        this.config.set("Integers.EngineMode", oldConfig.get("Integers.EngineMode"));
        this.config.set("Integers.InitialRadius", oldConfig.get("Integers.InitialRadius"));
        this.config.set("Integers.UpdateRadius", oldConfig.get("Integers.UpdateRadius"));
        this.config.set("Strings.CacheLocation", oldConfig.get("Strings.CacheLocation"));
        this.config.set("Strings.NoObfuscationForMetadataTagName", oldConfig.get("Strings.NoObfuscationForMetadataTagName"));
        this.config.set("Lists.TransparentBlocks", oldConfig.get("Lists.TransparentBlocks"));
        this.config.set("Lists.NonTransparentBlocks", oldConfig.get("Lists.NonTransparentBlocks"));
    }
    
    private void setWorldValues() {
    	FileConfiguration oldConfig = this.plugin.getConfig();
    	
    	boolean worldEnabled = oldConfig.get("Booleans.UseWorldsAsBlacklist") == null
    			|| oldConfig.getBoolean("Booleans.UseWorldsAsBlacklist");
    	
        // Default World
        this.config.set("Worlds.Default.Types", new String[] { "DEFAULT" });
		this.config.set("Worlds.Default.Enabled", worldEnabled);
		this.config.set("Worlds.Default.AntiTexturePackAndFreecam", oldConfig.get("Booleans.AntiTexturePackAndFreecam"));
		this.config.set("Worlds.Default.AirGeneratorMaxChance", oldConfig.get("Integers.AirGeneratorMaxChance"));
		this.config.set("Worlds.Default.DarknessHideBlocks", oldConfig.get("Booleans.DarknessHideBlocks"));
		this.config.set("Worlds.Default.DarknessBlocks", oldConfig.get("Lists.DarknessBlocks"));
		this.config.set("Worlds.Default.Mode1Block", Material.STONE);
		
	    this.config.set("Worlds.Default.ProximityHider.Enabled", oldConfig.get("Booleans.UseProximityHider"));
	    this.config.set("Worlds.Default.ProximityHider.Distance", oldConfig.get("Integers.ProximityHiderDistance"));
	    this.config.set("Worlds.Default.ProximityHider.SpecialBlock", oldConfig.get("Integers.ProximityHiderID"));
	    this.config.set("Worlds.Default.ProximityHider.Y", oldConfig.get("Integers.ProximityHiderEnd"));
	    this.config.set("Worlds.Default.ProximityHider.UseSpecialBlock", oldConfig.get("Booleans.UseSpecialBlockForProximityHider"));
	    this.config.set("Worlds.Default.ProximityHider.ObfuscateAboveY", oldConfig.get("Booleans.UseYLocationProximity"));
	    this.config.set("Worlds.Default.ProximityHider.ProximityHiderBlocks", oldConfig.get("Lists.ProximityHiderBlocks"));
	    
	    //Normal and TheEnd Worlds
		this.config.set("Worlds.Normal.Types", new String[] { "NORMAL", "THE_END" });
		this.config.set("Worlds.Normal.Mode1Block", Material.STONE);
		this.config.set("Worlds.Normal.RandomBlocks", oldConfig.get("Lists.RandomBlocks"));
		this.config.set("Worlds.Normal.ObfuscateBlocks", oldConfig.get("Lists.ObfuscateBlocks"));

		//Nether World
		this.config.set("Worlds.Nether.Types", new String[] { "NETHER" });
		this.config.set("Worlds.Nether.Mode1Block", Material.NETHERRACK);
		this.config.set("Worlds.Nether.RandomBlocks", oldConfig.get("Lists.NetherRandomBlocks"));
		this.config.set("Worlds.Nether.ObfuscateBlocks", oldConfig.get("Lists.NetherObfuscateBlocks"));
		
		List<String> worldNames = oldConfig.getStringList("Lists.Worlds");
		
        if(worldNames == null) {
        	worldNames = oldConfig.getStringList("Lists.DisabledWorlds");
        }
        
		if(worldNames != null && worldNames.size() > 0) {
			this.config.set("Worlds.CustomWorld.Names", worldNames);
			this.config.set("Worlds.CustomWorld.Enabled", !worldEnabled);
		}
    }
}
