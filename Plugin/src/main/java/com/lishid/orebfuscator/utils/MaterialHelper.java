/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import com.lishid.orebfuscator.NmsInstance;

public class MaterialHelper {

	private static final Map<Integer, Material> blocks = MaterialHelper.getAllMaterials();
	private static final int highestBlockId = MaterialHelper.getHighestBlockId();

	private static Map<Integer, Material> getAllMaterials() {
		Map<Integer, Material> blocks = new HashMap<Integer, Material>();

		Arrays.stream(Material.values())
			.filter(material -> material.isBlock())
			.forEach(material -> NmsInstance.get().getMaterialIds(material).forEach(id -> blocks.put(id, material)));

		return blocks;
	}

	private static int getHighestBlockId() {
		int maxId = -1;

		for (int id : MaterialHelper.blocks.keySet()) {
			if (id > maxId)
				maxId = id;
		}

		return maxId;
	}

	public static Material getById(int combinedBlockId) {
		return MaterialHelper.blocks.get(combinedBlockId);
	}

	public static int getMaxId() {
		return MaterialHelper.highestBlockId;
	}
}
