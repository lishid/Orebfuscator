/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.utils.IMaterialHelper;
import com.lishid.orebfuscator.handler.CraftHandler;

public class MaterialHelper extends CraftHandler implements IMaterialHelper {

	private INmsManager nmsManager;

	private Map<Integer, Material> blocks;
	private int highestBlockId;

	public MaterialHelper(Orebfuscator plugin) {
		super(plugin);
	}

	@Override
	public void onInit() {
		this.nmsManager = this.plugin.getNmsManager();

		this.blocks = this.getAllMaterials();
		this.highestBlockId = this.getHighestBlockId();
	}

	private Map<Integer, Material> getAllMaterials() {
		Map<Integer, Material> blocks = new HashMap<Integer, Material>();

		Arrays.stream(Material.values())
			.filter(material -> material.isBlock())
			.forEach(material -> this.nmsManager.getMaterialIds(material).forEach(id -> blocks.put(id, material)));

		return blocks;
	}

	private int getHighestBlockId() {
		int maxId = -1;

		for (int id : this.blocks.keySet()) {
			if (id > maxId)
				maxId = id;
		}

		return maxId;
	}

	public Material getById(int combinedBlockId) {
		return this.blocks.get(combinedBlockId);
	}

	public int getMaxId() {
		return this.highestBlockId;
	}
}
