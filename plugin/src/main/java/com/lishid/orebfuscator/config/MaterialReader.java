/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.utils.Globals;
import com.lishid.orebfuscator.api.utils.IMaterialHelper;

public class MaterialReader {

	private static class MaterialResult {
		public Set<Integer> ids;
		public String name;

		public MaterialResult(Set<Integer> ids, String name) {
			this.ids = ids;
			this.name = name;
		}
	}

	private final Orebfuscator plugin;
	private final INmsManager nmsManager;
	private final IMaterialHelper materialHelper;
	private final Logger logger;

	public MaterialReader(Orebfuscator plugin, Logger logger) {
		this.plugin = plugin;
		this.logger = logger;

		this.nmsManager = this.plugin.getNmsManager();
		this.materialHelper = this.plugin.getMaterialHelper();
	}

	private FileConfiguration getConfig() {
		return this.plugin.getConfig();
	}

	public Set<Integer> getMaterialIds(String materialName) {
		return getMaterial(materialName, null).ids;
	}

	public Integer getMaterialIdByPath(String path, Integer defaultMaterialId, boolean withSave) {
		boolean hasKey = getConfig().get(path) != null;

		if (!hasKey && defaultMaterialId == null) {
			return null;
		}

		String materialName = hasKey ? getConfig().getString(path) : Integer.toString(defaultMaterialId);
		MaterialResult material = getMaterial(materialName, defaultMaterialId);

		if (withSave || hasKey) {
			getConfig().set(path, material.name);
		}

		return material.ids.iterator().next();
	}

	public Integer[] getMaterialIdsByPath(String path, Integer[] defaultMaterials, boolean withSave) {
		List<String> list;

		if (getConfig().get(path) != null) {
			list = getConfig().getStringList(path);
			withSave = true;
		} else {
			if (defaultMaterials != null) {
				list = Arrays.stream(defaultMaterials).map(id -> this.materialHelper.getById(id).name()).collect(Collectors.toList());
			} else {
				return null;
			}
		}

		List<Integer> result = new ArrayList<>();
		HashSet<String> uniqueNames = new HashSet<>();

		for (int i = 0; i < list.size(); i++) {
			MaterialResult material = getMaterial(list.get(i), null);

			if (material != null) {
				uniqueNames.add(material.name);
				result.addAll(material.ids);
			}
		}

		if (withSave) {
			list.clear();
			list.addAll(uniqueNames);
			Collections.sort(list);

			getConfig().set(path, list);
		}

		return result.toArray(new Integer[0]);
	}

	private MaterialResult getMaterial(String inputMaterialName, Integer defaultMaterialId) {
		Set<Integer> materialIds = null;
		String defaultMaterialName = defaultMaterialId != null ? this.materialHelper.getById(defaultMaterialId).name()
				: null;
		String materialName = inputMaterialName;

		try {
			if (Character.isDigit(materialName.charAt(0))) {
				int id = Integer.parseInt(materialName);

				Material obj = this.materialHelper.getById(id);

				if (obj != null) {
					materialName = obj.name();

					materialIds = new HashSet<>();
					materialIds.add(id);
				} else {
					if (defaultMaterialId != null) {
						this.logger.info(String.format("%sMaterial with ID = %d is not found. Will be used default material: %s", Globals.LogPrefix, id, defaultMaterialName));
						materialName = defaultMaterialName;

						materialIds = new HashSet<>();
						materialIds.add(defaultMaterialId);
					} else {
						this.logger.info(Globals.LogPrefix + "Material with ID = " + id + " is not found. Skipped.");
					}
				}
			} else {
				Material obj = Material.getMaterial(materialName.toUpperCase());

				if (obj != null) {
					materialIds = this.nmsManager.getMaterialIds(obj);
				} else {
					if (defaultMaterialId != null) {
						this.logger.info(String.format("%sMaterial %s is not found. Will be used default material: %s", Globals.LogPrefix, materialName, defaultMaterialName));
						materialName = defaultMaterialName;

						materialIds = new HashSet<>();
						materialIds.add(defaultMaterialId);
					} else {
						this.logger.info(Globals.LogPrefix + "Material " + materialName + " is not found. Skipped.");
					}
				}
			}
		} catch (Exception e) {
			if (defaultMaterialId != null) {
				this.logger.info(String.format("%sInvalid material ID or name: %s.  Will be used default material: %s", Globals.LogPrefix, materialName, defaultMaterialName));
				materialName = defaultMaterialName;

				materialIds = new HashSet<>();
				materialIds.add(defaultMaterialId);
			} else {
				this.logger.info(Globals.LogPrefix + "Invalid material ID or name: " + materialName + ". Skipped.");
			}
		}

		return materialIds != null ? new MaterialResult(materialIds, materialName) : null;
	}
}
