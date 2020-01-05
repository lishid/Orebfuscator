/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.utils;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Material;

import com.lishid.orebfuscator.NmsInstance;

public class MaterialHelper {
	private static HashMap<Integer, Material> _blocks;

	public static void init() {
		_blocks = new HashMap<>();

		Material[] allMaterials = Material.values();

		for (Material material : allMaterials) {
			if (material.isBlock()) {
				Set<Integer> ids = NmsInstance.current.getMaterialIds(material);

				for (int id : ids) {
					_blocks.put(id, material);
				}
			}
		}
	}

	public static Material getById(int combinedBlockId) {
		return _blocks.get(combinedBlockId);
	}

	public static int getMaxId() {
		int maxId = -1;

		for (int id : _blocks.keySet()) {
			if (id > maxId) {
				maxId = id;
			}
		}

		return maxId;
	}
}
