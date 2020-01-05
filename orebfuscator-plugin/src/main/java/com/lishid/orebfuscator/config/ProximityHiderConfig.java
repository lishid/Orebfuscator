/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.HashSet;

import com.lishid.orebfuscator.NmsInstance;
import com.lishid.orebfuscator.utils.MaterialHelper;

public class ProximityHiderConfig {
	public static class BlockSetting implements Cloneable {
		public int blockId;
		public int y;
		public boolean obfuscateAboveY;

		@Override
		public BlockSetting clone() throws CloneNotSupportedException {
			BlockSetting clone = new BlockSetting();
			clone.blockId = this.blockId;
			clone.y = this.y;
			clone.obfuscateAboveY = this.obfuscateAboveY;

			return clone;
		}
	}

	private Boolean enabled;
	private Integer distance;
	private int distanceSquared;
	private Integer specialBlockID;
	private Integer y;
	private Boolean useSpecialBlock;
	private Boolean obfuscateAboveY;
	private Boolean useFastGazeCheck;
	private HashSet<Integer> proximityHiderBlocks;
	private BlockSetting[] proximityHiderBlockSettings;
	private short[] proximityHiderBlocksAndY;

	public void setDefaults() {
		this.enabled = true;
		this.distance = 8;
		this.distanceSquared = this.distance * this.distance;
		this.specialBlockID = NmsInstance.current.getConfigDefaults().defaultProximityHiderSpecialBlockId;
		this.y = 255;
		this.useSpecialBlock = true;
		this.obfuscateAboveY = false;
		this.useFastGazeCheck = true;

		this.proximityHiderBlocks = new HashSet<>();
		for (int blockId : NmsInstance.current.getConfigDefaults().defaultProximityHiderBlockIds) {
			this.proximityHiderBlocks.add(blockId);
		}
	}

	public void init(ProximityHiderConfig baseCfg) {
		if (this.enabled == null) {
			this.enabled = baseCfg.enabled;
		}

		if (this.distance == null) {
			this.distance = baseCfg.distance;
			this.distanceSquared = baseCfg.distanceSquared;
		}

		if (this.specialBlockID == null) {
			this.specialBlockID = baseCfg.specialBlockID;
		}

		if (this.y == null) {
			this.y = baseCfg.y;
		}

		if (this.useSpecialBlock == null) {
			this.useSpecialBlock = baseCfg.useSpecialBlock;
		}

		if (this.obfuscateAboveY == null) {
			this.obfuscateAboveY = baseCfg.obfuscateAboveY;
		}

		if (this.proximityHiderBlocks == null && baseCfg.proximityHiderBlocks != null) {
			this.proximityHiderBlocks = new HashSet<>();
			this.proximityHiderBlocks.addAll(baseCfg.proximityHiderBlocks);
		}

		if (this.proximityHiderBlockSettings == null && baseCfg.proximityHiderBlockSettings != null) {
			this.proximityHiderBlockSettings = baseCfg.proximityHiderBlockSettings.clone();
		}

		if (this.useFastGazeCheck == null) {
			this.useFastGazeCheck = baseCfg.useFastGazeCheck;
		}

		this.setProximityHiderBlockMatrix();
	}

	public Boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(Boolean value) {
		this.enabled = value;
	}

	public Integer getDistance() {
		return this.distance;
	}

	public void setDistance(Integer value) {
		this.distance = value;
		this.distanceSquared = this.distance != null ? this.distance * this.distance : 0;
	}

	public int getDistanceSquared() {
		return this.distanceSquared;
	}

	public Integer getSpecialBlockID() {
		return this.specialBlockID;
	}

	public void setSpecialBlockID(Integer value) {
		this.specialBlockID = value;
	}

	public Integer getY() {
		return this.y;
	}

	public void setY(Integer value) {
		this.y = value;
	}

	public Boolean isUseSpecialBlock() {
		return this.useSpecialBlock;
	}

	public void setUseSpecialBlock(Boolean value) {
		this.useSpecialBlock = value;
	}

	public Boolean isObfuscateAboveY() {
		return this.obfuscateAboveY;
	}

	public void setObfuscateAboveY(Boolean value) {
		this.obfuscateAboveY = value;
	}

	public void setProximityHiderBlocks(Integer[] blockIds) {
		if (blockIds != null) {
			this.proximityHiderBlocks = new HashSet<>();

			for (Integer id : blockIds) {
				this.proximityHiderBlocks.add(id);
			}
		}
	}

	public HashSet<Integer> getProximityHiderBlocks() {
		return this.proximityHiderBlocks;
	}

	public BlockSetting[] getProximityHiderBlockSettings() {
		return this.proximityHiderBlockSettings;
	}

	public void setProximityHiderBlockSettings(BlockSetting[] value) {
		this.proximityHiderBlockSettings = value;
	}

	private void setProximityHiderBlockMatrix() {
		this.proximityHiderBlocksAndY = new short[MaterialHelper.getMaxId() + 1];

		if (this.proximityHiderBlocks == null) {
			return;
		}

		for (int blockId : this.proximityHiderBlocks) {
			this.proximityHiderBlocksAndY[blockId] = (short) (this.obfuscateAboveY ? -this.y : this.y);
		}

		if (this.proximityHiderBlockSettings != null) {
			for (BlockSetting block : this.proximityHiderBlockSettings) {
				this.proximityHiderBlocksAndY[block.blockId] = (short) (block.obfuscateAboveY ? -block.y : block.y);
			}
		}
	}

	public Boolean isUseFastGazeCheck() {
		return this.useFastGazeCheck;
	}

	public void setUseFastGazeCheck(Boolean value) {
		this.useFastGazeCheck = value;
	}

	// Help methods

	public boolean isProximityObfuscated(int y, int id) {
		int proximityY = this.proximityHiderBlocksAndY[id];

		if (proximityY > 0) {
			return y <= proximityY;
		}

		return y >= (proximityY & 0xFFF);
	}
}
