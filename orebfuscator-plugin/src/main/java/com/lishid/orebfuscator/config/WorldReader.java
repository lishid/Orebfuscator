/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.orebfuscator.NmsInstance;
import com.lishid.orebfuscator.utils.Globals;

public class WorldReader {
	private enum WorldType {
		Default, Normal, TheEnd, Nether
	}

	private WorldConfig defaultWorld;
	private WorldConfig normalWorld;
	private WorldConfig endWorld;
	private WorldConfig netherWorld;
	private Map<String, WorldConfig> worlds;

	private JavaPlugin plugin;
	private Logger logger;
	private OrebfuscatorConfig orebfuscatorConfig;
	private MaterialReader materialReader;

	public WorldReader(JavaPlugin plugin, Logger logger, OrebfuscatorConfig orebfuscatorConfig,
			MaterialReader materialReader) {
		this.plugin = plugin;
		this.logger = logger;
		this.orebfuscatorConfig = orebfuscatorConfig;
		this.materialReader = materialReader;
	}

	public void load() {
		ConfigurationSection section = this.getConfig().getConfigurationSection("Worlds");
		Set<String> keys = section != null ? section.getKeys(false) : new HashSet<>();

		this.defaultWorld = this.readWorldByType(keys, WorldType.Default, null);
		this.normalWorld = this.readWorldByType(keys, WorldType.Normal, this.defaultWorld);
		this.endWorld = this.readWorldByType(keys, WorldType.TheEnd, this.defaultWorld);
		this.netherWorld = this.readWorldByType(keys, WorldType.Nether, this.defaultWorld);

		this.worlds = new HashMap<>();

		for (String key : keys) {
			this.readWorldsByName("Worlds." + key);
		}

		this.orebfuscatorConfig.setDefaultWorld(this.defaultWorld);
		this.orebfuscatorConfig.setNormalWorld(this.normalWorld);
		this.orebfuscatorConfig.setEndWorld(this.endWorld);
		this.orebfuscatorConfig.setNetherWorld(this.netherWorld);
		this.orebfuscatorConfig.setWorlds(this.worlds);
	}

	private FileConfiguration getConfig() {
		return this.plugin.getConfig();
	}

	public static String createPath(String path, String key, FileConfiguration config) {
		int counter = 1;
		String newPath = path + "." + key;

		while (config.get(newPath) != null) {
			newPath = path + "." + key + counter++;
		}

		return newPath;
	}

	private WorldConfig readWorldByType(Set<String> keys, WorldType worldType, WorldConfig baseWorld) {
		WorldConfig world = null;

		for (String key : keys) {
			String worldPath = "Worlds." + key;
			List<String> types = this.getStringList(worldPath + ".Types", null, false);

			if (types != null && types.size() > 0 && this.parseWorldTypes(types).contains(worldType)) {
				if (worldType == WorldType.Default) {
					world = new WorldConfig();
					world.setDefaults();
				}

				world = this.readWorld(worldPath, world, worldType, worldType == WorldType.Default);

				this.logger.log(Level.INFO, Globals.LogPrefix + "World type '" + worldType + "' has been read.");
				break;
			}
		}

		if (world == null) {
			switch (worldType) {
			case Default:
				world = this.createDefaultWorld(createPath("Worlds", "Default", this.getConfig()));
				break;
			case Normal:
				world = this.createNormalWorld(createPath("Worlds", "Normal", this.getConfig()));
				break;
			case TheEnd:
				world = this.createEndWorld(createPath("Worlds", "TheEnd", this.getConfig()));
				break;
			case Nether:
				world = this.createNetherWorld(createPath("Worlds", "Nether", this.getConfig()));
				break;
			}

			this.logger.log(Level.WARNING, Globals.LogPrefix + "World type '" + worldType + "' has been created.");
		}

		world.init(baseWorld);

		return world;
	}

	private void readWorldsByName(String worldPath) {
		List<String> names = this.getStringList(worldPath + ".Names", null, false);

		if (names == null || names.size() == 0) {
			return;
		}

		for (String name : names) {
			String key = name.toLowerCase();

			if (!this.worlds.containsKey(key)) {
				WorldConfig world = this.readWorld(worldPath, null, WorldType.Default, false);
				world.setName(name);

				this.worlds.put(key, world);

				this.logger.log(Level.INFO, Globals.LogPrefix + "World name '" + name + "' has been read.");
			}
		}
	}

	private List<WorldType> parseWorldTypes(List<String> types) {
		List<WorldType> parsedTypes = new ArrayList<>();

		for (String type : types) {
			WorldType worldType;

			if (type.equalsIgnoreCase("DEFAULT")) {
				worldType = WorldType.Default;
			} else if (type.equalsIgnoreCase("NORMAL")) {
				worldType = WorldType.Normal;
			} else if (type.equalsIgnoreCase("THE_END")) {
				worldType = WorldType.TheEnd;
			} else if (type.equalsIgnoreCase("NETHER")) {
				worldType = WorldType.Nether;
			} else {
				this.logger.log(Level.WARNING, Globals.LogPrefix + "World type '" + type + "' is not supported.");
				continue;
			}

			parsedTypes.add(worldType);
		}

		return parsedTypes;
	}

	private WorldConfig readWorld(String worldPath, WorldConfig cfg, WorldType worldType, boolean withSave) {
		if (cfg == null) {
			cfg = new WorldConfig();
		}

		Boolean enabled = this.getBoolean(worldPath + ".Enabled", cfg.isEnabled(), withSave);
		Boolean antiTexturePackAndFreecam = this.getBoolean(worldPath + ".AntiTexturePackAndFreecam",
				cfg.isAntiTexturePackAndFreecam(), withSave);
		Integer airGeneratorMaxChance = this.getInt(worldPath + ".AirGeneratorMaxChance",
				cfg.getAirGeneratorMaxChance(), 40, 100, withSave);
		Boolean darknessHideBlocks = this.getBoolean(worldPath + ".DarknessHideBlocks", cfg.isDarknessHideBlocks(),
				withSave);
		Boolean bypassObfuscationForSignsWithText = this.getBoolean(worldPath + ".BypassObfuscationForSignsWithText",
				cfg.isBypassObfuscationForSignsWithText(), withSave);
		HashSet<Integer> darknessBlocks = this.readBlockMatrix(cfg.getDarknessBlocks(), worldPath + ".DarknessBlocks",
				withSave);
		Integer mode1Block = this.materialReader.getMaterialIdByPath(worldPath + ".Mode1Block", cfg.getMode1BlockId(),
				withSave);
		Integer[] randomBlocks = this.materialReader.getMaterialIdsByPath(worldPath + ".RandomBlocks",
				cfg.getRandomBlocks(), withSave);
		HashSet<Integer> obfuscateBlocks = this.readBlockMatrix(cfg.getObfuscateBlocks(),
				worldPath + ".ObfuscateBlocks", withSave);

		int[] requiredObfuscateBlockIds;

		switch (worldType) {
		case Normal:
			requiredObfuscateBlockIds = NmsInstance.current.getConfigDefaults().normalWorldRequiredObfuscateBlockIds;
			break;
		case TheEnd:
			requiredObfuscateBlockIds = NmsInstance.current.getConfigDefaults().endWorldRequiredObfuscateBlockIds;
			break;
		case Nether:
			requiredObfuscateBlockIds = NmsInstance.current.getConfigDefaults().netherWorldRequiredObfuscateBlockIds;
			break;
		default:
			requiredObfuscateBlockIds = null;
			break;
		}

		if (requiredObfuscateBlockIds != null) {
			for (int blockId : requiredObfuscateBlockIds) {
				obfuscateBlocks.add(blockId);
			}
		}

		this.readProximityHider(worldPath, cfg, withSave);

		cfg.setEnabled(enabled);
		cfg.setAntiTexturePackAndFreecam(antiTexturePackAndFreecam);
		cfg.setBypassObfuscationForSignsWithText(bypassObfuscationForSignsWithText);
		cfg.setAirGeneratorMaxChance(airGeneratorMaxChance);
		cfg.setDarknessHideBlocks(darknessHideBlocks);
		cfg.setDarknessBlocks(darknessBlocks);
		cfg.setMode1BlockId(mode1Block);
		cfg.setRandomBlocks(randomBlocks);
		cfg.setObfuscateBlocks(obfuscateBlocks);

		return cfg;
	}

	private void readProximityHider(String worldPath, WorldConfig worldConfig, boolean withSave) {
		ProximityHiderConfig cfg = worldConfig.getProximityHiderConfig();
		Integer[] defaultProximityHiderBlockIds = cfg.getProximityHiderBlocks() != null
				? cfg.getProximityHiderBlocks().toArray(new Integer[0])
				: null;

		String sectionPath = worldPath + ".ProximityHider";
		Boolean enabled = this.getBoolean(sectionPath + ".Enabled", cfg.isEnabled(), withSave);
		Integer distance = this.getInt(sectionPath + ".Distance", cfg.getDistance(), 2, 64, withSave);
		Integer specialBlockID = this.materialReader.getMaterialIdByPath(sectionPath + ".SpecialBlock",
				cfg.getSpecialBlockID(), withSave);
		Integer y = this.getInt(sectionPath + ".Y", cfg.getY(), 0, 255, withSave);
		Boolean useSpecialBlock = this.getBoolean(sectionPath + ".UseSpecialBlock", cfg.isUseSpecialBlock(), withSave);
		Boolean useYLocationProximity = this.getBoolean(sectionPath + ".ObfuscateAboveY", cfg.isObfuscateAboveY(),
				withSave);
		Integer[] proximityHiderBlockIds = this.materialReader
				.getMaterialIdsByPath(sectionPath + ".ProximityHiderBlocks", defaultProximityHiderBlockIds, withSave);
		ProximityHiderConfig.BlockSetting[] proximityHiderBlockSettings = this.readProximityHiderBlockSettings(
				sectionPath + ".ProximityHiderBlockSettings", cfg.getProximityHiderBlockSettings());
		Boolean useFastGazeCheck = this.getBoolean(sectionPath + ".UseFastGazeCheck", cfg.isUseFastGazeCheck(),
				withSave);

		cfg.setEnabled(enabled);
		cfg.setDistance(distance);
		cfg.setSpecialBlockID(specialBlockID);
		cfg.setY(y);
		cfg.setUseSpecialBlock(useSpecialBlock);
		cfg.setObfuscateAboveY(useYLocationProximity);
		cfg.setProximityHiderBlocks(proximityHiderBlockIds);
		cfg.setProximityHiderBlockSettings(proximityHiderBlockSettings);
		cfg.setUseFastGazeCheck(useFastGazeCheck);
	}

	private ProximityHiderConfig.BlockSetting[] readProximityHiderBlockSettings(String configKey,
			ProximityHiderConfig.BlockSetting[] defaultBlocks) {
		ConfigurationSection section = this.getConfig().getConfigurationSection(configKey);

		if (section == null) {
			return defaultBlocks;
		}

		Set<String> keys = section.getKeys(false);

		List<ProximityHiderConfig.BlockSetting> list = new ArrayList<>();

		for (String key : keys) {
			Set<Integer> blockIds = this.materialReader.getMaterialIds(key);

			if (blockIds == null) {
				continue;
			}

			String blockPath = configKey + "." + key;
			int blockY = this.getConfig().getInt(blockPath + ".Y", 255);
			boolean blockObfuscateAboveY = this.getConfig().getBoolean(blockPath + ".ObfuscateAboveY", false);

			for (int blockId : blockIds) {
				ProximityHiderConfig.BlockSetting block = new ProximityHiderConfig.BlockSetting();
				block.blockId = blockId;
				block.y = blockY;
				block.obfuscateAboveY = blockObfuscateAboveY;

				list.add(block);
			}
		}

		return list.toArray(new ProximityHiderConfig.BlockSetting[0]);
	}

	private HashSet<Integer> readBlockMatrix(HashSet<Integer> original, String configKey, boolean withSave) {
		Integer[] defaultBlockIds;

		if (original != null && original.size() != 0) {
			defaultBlockIds = new Integer[original.size()];

			int index = 0;

			for (Integer id : original) {
				defaultBlockIds[index++] = id;
			}
		} else {
			defaultBlockIds = null;
		}

		Integer[] blockIds = this.materialReader.getMaterialIdsByPath(configKey, defaultBlockIds, withSave);

		HashSet<Integer> blocks;

		if (blockIds != null) {
			blocks = new HashSet<>();

			for (int id : blockIds) {
				blocks.add(id);
			}
		} else {
			blocks = original;
		}

		return blocks;
	}

	private WorldConfig createDefaultWorld(String worldPath) {
		this.getConfig().set(worldPath + ".Types", new String[] { "DEFAULT" });

		WorldConfig world = new WorldConfig();
		world.setDefaults();

		return this.readWorld(worldPath, world, WorldType.Default, true);
	}

	private WorldConfig createNormalWorld(String worldPath) {
		Integer[] randomBlocks = cloneIntArray(NmsInstance.current.getConfigDefaults().normalWorldRandomBlockIds);
		Integer[] obfuscateBlockIds = mergeIntArrays(
				NmsInstance.current.getConfigDefaults().normalWorldObfuscateBlockIds,
				NmsInstance.current.getConfigDefaults().normalWorldRequiredObfuscateBlockIds);

		this.getConfig().set(worldPath + ".Types", new String[] { "NORMAL" });

		int mode1BlockId = NmsInstance.current.getConfigDefaults().normalWorldMode1BlockId;

		this.materialReader.getMaterialIdByPath(worldPath + ".Mode1Block", mode1BlockId, true);
		this.materialReader.getMaterialIdsByPath(worldPath + ".RandomBlocks", randomBlocks, true);
		this.materialReader.getMaterialIdsByPath(worldPath + ".ObfuscateBlocks", obfuscateBlockIds, true);

		WorldConfig cfg = new WorldConfig();
		cfg.setObfuscateBlocks(obfuscateBlockIds);
		cfg.setRandomBlocks(randomBlocks);
		cfg.setMode1BlockId(mode1BlockId);

		return cfg;
	}

	private WorldConfig createEndWorld(String worldPath) {
		Integer[] randomBlocks = cloneIntArray(NmsInstance.current.getConfigDefaults().endWorldRandomBlockIds);
		Integer[] obfuscateBlockIds = mergeIntArrays(NmsInstance.current.getConfigDefaults().endWorldObfuscateBlockIds,
				NmsInstance.current.getConfigDefaults().endWorldRequiredObfuscateBlockIds);

		this.getConfig().set(worldPath + ".Types", new String[] { "THE_END" });

		int mode1BlockId = NmsInstance.current.getConfigDefaults().endWorldMode1BlockId;

		this.materialReader.getMaterialIdByPath(worldPath + ".Mode1Block", mode1BlockId, true);
		this.materialReader.getMaterialIdsByPath(worldPath + ".RandomBlocks", randomBlocks, true);
		this.materialReader.getMaterialIdsByPath(worldPath + ".ObfuscateBlocks", obfuscateBlockIds, true);

		WorldConfig cfg = new WorldConfig();
		cfg.setRandomBlocks(randomBlocks);
		cfg.setObfuscateBlocks(obfuscateBlockIds);
		cfg.setMode1BlockId(mode1BlockId);

		return cfg;
	}

	private WorldConfig createNetherWorld(String worldPath) {
		Integer[] randomBlocks = cloneIntArray(NmsInstance.current.getConfigDefaults().netherWorldRandomBlockIds);
		Integer[] obfuscateBlockIds = mergeIntArrays(
				NmsInstance.current.getConfigDefaults().netherWorldObfuscateBlockIds,
				NmsInstance.current.getConfigDefaults().netherWorldRequiredObfuscateBlockIds);

		this.getConfig().set(worldPath + ".Types", new String[] { "NETHER" });

		int mode1BlockId = NmsInstance.current.getConfigDefaults().netherWorldMode1BlockId;

		this.materialReader.getMaterialIdByPath(worldPath + ".Mode1Block", mode1BlockId, true);
		this.materialReader.getMaterialIdsByPath(worldPath + ".RandomBlocks", randomBlocks, true);
		this.materialReader.getMaterialIdsByPath(worldPath + ".ObfuscateBlocks", obfuscateBlockIds, true);

		WorldConfig cfg = new WorldConfig();
		cfg.setRandomBlocks(randomBlocks);
		cfg.setObfuscateBlocks(obfuscateBlockIds);
		cfg.setMode1BlockId(mode1BlockId);

		return cfg;
	}

	private Integer getInt(String path, Integer defaultData, int min, int max, boolean withSave) {
		if (this.getConfig().get(path) == null && withSave) {
			this.getConfig().set(path, defaultData);
		}

		Integer value = this.getConfig().get(path) != null ? (Integer) this.getConfig().getInt(path) : defaultData;

		if (value != null) {
			if (value < min) {
				value = min;
			} else if (value > max) {
				value = max;
			}
		}

		return value;
	}

	private Boolean getBoolean(String path, Boolean defaultData, boolean withSave) {
		if (this.getConfig().get(path) == null) {
			if (!withSave) {
				return defaultData;
			}

			this.getConfig().set(path, defaultData);
		}

		return this.getConfig().get(path) != null ? (Boolean) this.getConfig().getBoolean(path) : defaultData;
	}

	private List<String> getStringList(String path, List<String> defaultData, boolean withSave) {
		if (this.getConfig().get(path) == null) {
			if (!withSave) {
				return defaultData;
			}

			this.getConfig().set(path, defaultData);
		}

		return this.getConfig().getStringList(path);
	}

	private static Integer[] cloneIntArray(int[] array) {
		Integer[] result = new Integer[array.length];

		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}

		return result;
	}

	private static Integer[] mergeIntArrays(int[] array1, int[] array2) {
		Integer[] result = new Integer[array1.length + array2.length];

		for (int i = 0; i < array1.length; i++) {
			result[i] = array1[i];
		}

		for (int i = 0; i < array2.length; i++) {
			result[array1.length + i] = array2[i];
		}

		return result;
	}
}