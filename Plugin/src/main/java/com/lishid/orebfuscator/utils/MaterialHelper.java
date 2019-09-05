/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.utils;

import com.lishid.orebfuscator.NmsInstance;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Set;

public class MaterialHelper {

	private static HashMap<Integer, Material> _blocks;

	public static void init() {
		MaterialHelper._blocks = new HashMap<>();

		Material[] allMaterials = Material.values();

		for (Material material : allMaterials) {
			if (material.isBlock()) {
				Set<Integer> ids = NmsInstance.current.getMaterialIds(material);

				for (int id : ids) {
					MaterialHelper._blocks.put(id, material);
				}
			}
		}
	}

	public static Material getById(int combinedBlockId) {
		return MaterialHelper._blocks.get(combinedBlockId);
	}

	public static int getMaxId() {
		int maxId = -1;

		for (int id : MaterialHelper._blocks.keySet()) {
			if (id > maxId) {
				maxId = id;
			}
		}

		return maxId;
	}
}
