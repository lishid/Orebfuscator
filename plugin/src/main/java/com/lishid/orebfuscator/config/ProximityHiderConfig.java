/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.config.IProximityHiderConfig;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.utils.IMaterialHelper;

public class ProximityHiderConfig implements IProximityHiderConfig {

	public static class BlockSetting implements IBlockSetting {

		public int blockId;
		public int y;
		public boolean obfuscateAboveY;

		public BlockSetting(int blockId, int y, boolean obfuscateAboveY) {
			this.blockId = blockId;
			this.y = y;
			this.obfuscateAboveY = obfuscateAboveY;
		}

		public BlockSetting clone() throws CloneNotSupportedException {
			return new BlockSetting(this.blockId, this.y, this.obfuscateAboveY);
		}

		@Override
		public int getBlockId() {
			return this.blockId;
		}

		@Override
		public int getY() {
			return this.y;
		}

		@Override
		public boolean obfuscateAboveY() {
			return this.obfuscateAboveY;
		}
	}

	private final Orebfuscator plugin;
	private final INmsManager nmsManager;
	private final IMaterialHelper materialHelper;

	private Boolean enabled;
	private Integer distance;
	private int distanceSquared;
	private Integer specialBlockID;
	private Integer y;
	private Boolean useSpecialBlock;
	private Boolean obfuscateAboveY;
	private Boolean useFastGazeCheck;
	private HashSet<Integer> proximityHiderBlocks;
	private IBlockSetting[] proximityHiderBlockSettings;
	private short[] proximityHiderBlocksAndY;

	public ProximityHiderConfig(Orebfuscator plugin) {
		this.plugin = plugin;

		this.nmsManager = this.plugin.getNmsManager();
		this.materialHelper = this.plugin.getMaterialHelper();
	}

	public void setDefaults() {
		this.enabled = true;
		this.distance = 8;
		this.distanceSquared = this.distance * this.distance;
		this.specialBlockID = this.nmsManager.getConfigDefaults().defaultProximityHiderSpecialBlockId;
		this.y = 255;
		this.useSpecialBlock = true;
		this.obfuscateAboveY = false;
		this.useFastGazeCheck = true;

		this.proximityHiderBlocks = Arrays.stream(this.nmsManager.getConfigDefaults().defaultProximityHiderBlockIds).boxed().collect(Collectors.toCollection(HashSet::new));
	}

	public void init(IProximityHiderConfig baseCfg) {
		if (this.enabled == null)
			this.enabled = baseCfg.isEnabled();

		if (this.distance == null) {
			this.distance = baseCfg.getDistance();
			this.distanceSquared = baseCfg.getDistanceSquared();
		}

		if (this.specialBlockID == null)
			this.specialBlockID = baseCfg.getSpecialBlockID();

		if (this.y == null)
			this.y = baseCfg.getY();

		if (this.useSpecialBlock == null)
			this.useSpecialBlock = baseCfg.isUseSpecialBlock();

		if (this.obfuscateAboveY == null)
			this.obfuscateAboveY = baseCfg.isObfuscateAboveY();

		if (this.proximityHiderBlocks == null && baseCfg.getProximityHiderBlocks() != null) {
			this.proximityHiderBlocks = new HashSet<>();
			this.proximityHiderBlocks.addAll(baseCfg.getProximityHiderBlocks());
		}

		if (this.proximityHiderBlockSettings == null && baseCfg.getProximityHiderBlockSettings() != null)
			this.proximityHiderBlockSettings = baseCfg.getProximityHiderBlockSettings().clone();

		if (this.useFastGazeCheck == null)
			this.useFastGazeCheck = baseCfg.isUseFastGazeCheck();

		setProximityHiderBlockMatrix();
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
		if (blockIds != null)
			this.proximityHiderBlocks = Arrays.stream(blockIds).collect(Collectors.toCollection(HashSet::new));
	}

	public HashSet<Integer> getProximityHiderBlocks() {
		return this.proximityHiderBlocks;
	}

	public IBlockSetting[] getProximityHiderBlockSettings() {
		return this.proximityHiderBlockSettings;
	}

	public void setProximityHiderBlockSettings(IBlockSetting[] value) {
		this.proximityHiderBlockSettings = value;
	}

	private void setProximityHiderBlockMatrix() {
		this.proximityHiderBlocksAndY = new short[this.materialHelper.getMaxId() + 1];

		if (this.proximityHiderBlocks == null) {
			return;
		}

		this.proximityHiderBlocks.forEach(blockId -> this.proximityHiderBlocksAndY[blockId] = (short) (this.obfuscateAboveY ? -this.y : this.y));

		if (this.proximityHiderBlockSettings != null) {
			for (IBlockSetting block : this.proximityHiderBlockSettings) {
				this.proximityHiderBlocksAndY[block.getBlockId()] = (short) (block.obfuscateAboveY() ? -block.getY() : block.getY());
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

		if (proximityY > 0)
			return y <= proximityY;
		return y >= (proximityY & 0xFFF);
	}

	public Orebfuscator getPlugin() {
		return this.plugin;
	}

	public INmsManager getNmsManager() {
		return this.nmsManager;
	}
}
