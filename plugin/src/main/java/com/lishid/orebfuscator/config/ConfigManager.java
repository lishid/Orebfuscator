/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.config.IConfigManager;
import com.lishid.orebfuscator.api.config.IWorldConfig;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.utils.Globals;
import com.lishid.orebfuscator.api.utils.IMaterialHelper;

public class ConfigManager implements IConfigManager {
	private static final int CONFIG_VERSION = 13;

	private Orebfuscator plugin;
	private Logger logger;
	private OrebfuscatorConfig orebfuscatorConfig;
	private MaterialReader materialReader;
	private IMaterialHelper materialHelper;

	public ConfigManager(Orebfuscator plugin, Logger logger, OrebfuscatorConfig orebfuscatorConfig) {
		this.plugin = plugin;
		this.logger = logger;
		this.orebfuscatorConfig = orebfuscatorConfig;

		this.materialReader = new MaterialReader(this.plugin, this.logger);
		this.materialHelper = this.plugin.getMaterialHelper();
	}

	public IWorldConfig getWorld(World world) {
		if (world == null) {
			return null;
		}

		IWorldConfig baseCfg;

		switch (world.getEnvironment()) {
		case THE_END:
			baseCfg = this.orebfuscatorConfig.getEndWorld();
			break;
		case NETHER:
			baseCfg = this.orebfuscatorConfig.getNetherWorld();
			break;
		default:
			baseCfg = this.orebfuscatorConfig.getNormalWorld();
			break;
		}

		IWorldConfig cfg = this.orebfuscatorConfig.getWorld(world.getName());

		if (cfg == null) {
			return baseCfg;
		}

		if (!cfg.isInitialized()) {
			cfg.init(baseCfg);
			this.logger.log(Level.INFO, Globals.LogPrefix + "Config for world '" + world.getName() + "' is initialized.");
		}

		return cfg;
	}

	public void load() {
		// Version check
		int version = getInt("ConfigVersion", CONFIG_VERSION);
		if (version < CONFIG_VERSION) {
			if (version <= 12) {
				new Convert12To13(this.plugin).convert();
				logger.info(Globals.LogPrefix + "Configuration file have been converted to new version.");
			} else {
				this.getConfig().set("ConfigVersion", CONFIG_VERSION);
			}
		}

		boolean useCache = this.getBoolean("Booleans.UseCache", true);
		int maxLoadedCacheFiles = this.getInt("Integers.MaxLoadedCacheFiles", 64, 16, 128);
		String cacheLocation = this.getString("Strings.CacheLocation", "orebfuscator_cache");
		int deleteCacheFilesAfterDays = this.getInt("Integers.DeleteCacheFilesAfterDays", 0);
		boolean enabled = this.getBoolean("Booleans.Enabled", true);
		boolean updateOnDamage = this.getBoolean("Booleans.UpdateOnDamage", true);

		int engineMode = this.getInt("Integers.EngineMode", 2);
		if (engineMode != 1 && engineMode != 2) {
			engineMode = 2;
			logger.info(Globals.LogPrefix + "EngineMode must be 1 or 2.");
		}

		int initialRadius = this.getInt("Integers.InitialRadius", 1, 0, 2);
		if (initialRadius == 0) {
			logger.info(Globals.LogPrefix + "Warning, InitialRadius is 0. This will cause all exposed blocks to be obfuscated.");
		}

		int updateRadius = this.getInt("Integers.UpdateRadius", 2, 1, 5);
		boolean noObfuscationForMetadata = this.getBoolean("Booleans.NoObfuscationForMetadata", true);
		String noObfuscationForMetadataTagName = this.getString("Strings.NoObfuscationForMetadataTagName", "NPC");
		boolean noObfuscationForOps = this.getBoolean("Booleans.NoObfuscationForOps", false);
		boolean noObfuscationForPermission = this.getBoolean("Booleans.NoObfuscationForPermission", false);
		boolean loginNotification = this.getBoolean("Booleans.LoginNotification", true);
		byte[] transparentBlocks = this.generateTransparentBlocks(engineMode);

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
		this.orebfuscatorConfig.setTransparentBlocks(transparentBlocks);

		new WorldReader(this.plugin, this.logger, this.orebfuscatorConfig, this.materialReader).load();

		this.orebfuscatorConfig.setProximityHiderEnabled();
		this.logger.info(Globals.LogPrefix + "Proximity Hider is " + (this.orebfuscatorConfig.isProximityHiderEnabled() ? "Enabled" : "Disabled"));

		this.save();
	}

	public void setEngineMode(int value) {
		this.getConfig().set("Integers.EngineMode", value);
		this.save();
		this.orebfuscatorConfig.setEngineMode(value);
	}

	public void setUpdateRadius(int value) {
		this.getConfig().set("Integers.UpdateRadius", value);
		this.save();
		this.orebfuscatorConfig.setUpdateRadius(value);
	}

	public void setInitialRadius(int value) {
		this.getConfig().set("Integers.InitialRadius", value);
		this.save();
		this.orebfuscatorConfig.setInitialRadius(value);
	}

	public void setProximityHiderDistance(int value) {
		this.getConfig().set("Integers.ProximityHiderDistance", value);
		this.save();
		this.orebfuscatorConfig.getDefaultWorld().getProximityHiderConfig().setDistance(value);
	}

	public void setNoObfuscationForOps(boolean value) {
		this.getConfig().set("Booleans.NoObfuscationForOps", value);
		this.save();
		this.orebfuscatorConfig.setNoObfuscationForOps(value);
	}

	public void setNoObfuscationForPermission(boolean value) {
		this.getConfig().set("Booleans.NoObfuscationForPermission", value);
		this.save();
		this.orebfuscatorConfig.setNoObfuscationForPermission(value);
	}

	public void setLoginNotification(boolean value) {
		this.getConfig().set("Booleans.LoginNotification", value);
		this.save();
		this.orebfuscatorConfig.setLoginNotification(value);
	}

	public void setUseCache(boolean value) {
		this.getConfig().set("Booleans.UseCache", value);
		this.save();
		this.orebfuscatorConfig.setUseCache(value);
	}

	public void setEnabled(boolean value) {
		this.getConfig().set("Booleans.Enabled", value);
		this.save();
		this.orebfuscatorConfig.setEnabled(value);
	}

	private FileConfiguration getConfig() {
		return this.plugin.getConfig();
	}

	private void save() {
		this.plugin.saveConfig();
	}

	private String getString(String path, String defaultData, boolean withSave) {
		if (this.getConfig().get(path) == null) {
			if (!withSave) {
				return defaultData;
			}

			this.getConfig().set(path, defaultData);
		}

		return this.getConfig().getString(path, defaultData);
	}

	private String getString(String path, String defaultData) {
		return getString(path, defaultData, true);
	}

	private int getInt(String path, int defaultData) {
		return getInt(path, defaultData, true);
	}

	private int getInt(String path, int defaultData, boolean withSave) {
		if (this.getConfig().get(path) == null) {
			if (!withSave) {
				return defaultData;
			}

			this.getConfig().set(path, defaultData);
		}

		return this.getConfig().getInt(path, defaultData);
	}

	private int getInt(String path, int defaultData, int min, int max, boolean withSave) {
		if (this.getConfig().get(path) == null && withSave) {
			this.getConfig().set(path, defaultData);
		}

		int value = this.getConfig().get(path) != null ? this.getConfig().getInt(path, defaultData) : defaultData;

		if (value < min) {
			value = min;
		} else if (value > max) {
			value = max;
		}

		return value;
	}

	private int getInt(String path, int defaultData, int min, int max) {
		return getInt(path, defaultData, min, max, true);
	}

	private boolean getBoolean(String path, boolean defaultData, boolean withSave) {
		if (this.getConfig().get(path) == null) {
			if (!withSave) {
				return defaultData;
			}

			this.getConfig().set(path, defaultData);
		}

		return this.getConfig().getBoolean(path, defaultData);
	}

	private boolean getBoolean(String path, boolean defaultData) {
		return getBoolean(path, defaultData, true);
	}

	private byte[] generateTransparentBlocks(int engineMode) {
		byte[] transparentBlocks = new byte[this.materialHelper.getMaxId() + 1];
		INmsManager nmsManager = this.plugin.getNmsManager();

		Arrays.stream(Material.values())
			.filter(material -> material.isBlock() && !material.isOccluding())
			.forEach(material -> nmsManager.getMaterialIds(material).forEach(id -> transparentBlocks[id] = 1));

		byte status = (byte) (engineMode == 1 ? 0 : 1);
		nmsManager.getMaterialIds(Material.LAVA).stream().forEach(id -> transparentBlocks[id] = status);

		return transparentBlocks;
	}
}