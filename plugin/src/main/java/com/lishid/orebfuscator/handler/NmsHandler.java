package com.lishid.orebfuscator.handler;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.logger.OFCLogger;
import com.lishid.orebfuscator.api.nms.IBlockInfo;
import com.lishid.orebfuscator.api.nms.IChunkCache;
import com.lishid.orebfuscator.api.nms.INBT;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.types.BlockCoord;
import com.lishid.orebfuscator.api.types.ConfigDefaults;

public class NmsHandler extends CraftHandler implements INmsManager {

	private String serverVersion;
	private INmsManager nmsWrapper;

	public NmsHandler(Orebfuscator core) {
		super(core);
	}

	@Override
	public void onInit() {
		this.serverVersion = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		OFCLogger.log("Searching NMS protocol for server version \"" + this.serverVersion + "\"!");

		switch (this.serverVersion) {
		case "v1_15_R1":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_15_R1.NmsManager();
			break;

		case "v1_14_R1":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_14_R1.NmsManager();
			break;

		case "v1_13_R2":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_13_R2.NmsManager();
			break;

		case "v1_13_R1":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_13_R1.NmsManager();
			break;

		case "v1_12_R1":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_12_R1.NmsManager();
			break;

		case "v1_11_R1":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_11_R1.NmsManager();
			break;

		case "v1_10_R1":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_10_R1.NmsManager();
			break;

		case "v1_9_R2":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_9_R2.NmsManager();
			break;

		case "v1_9_R1":
			this.nmsWrapper = new com.lishid.orebfuscator.nms.v1_9_R1.NmsManager();
			break;
		}

		if (this.nmsWrapper != null) {
			OFCLogger.log("NMS protocol for server version \"" + this.serverVersion + "\" found!");
		} else {
			OFCLogger.log("Server version \"" + this.serverVersion + "\" is currently not supported!");
			OFCLogger.log("[OFC] Plugin was disabled!");
			Bukkit.getPluginManager().disablePlugin(this.plugin);
		}
	}

	@Override
	public ConfigDefaults getConfigDefaults() {
		return this.nmsWrapper.getConfigDefaults();
	}

	@Override
	public void setMaxLoadedCacheFiles(int value) {
		this.nmsWrapper.setMaxLoadedCacheFiles(value);
	}

	@Override
	public INBT createNBT() {
		return this.nmsWrapper.createNBT();
	}

	@Override
	public IChunkCache createChunkCache() {
		return this.nmsWrapper.createChunkCache();
	}

	@Override
	public void updateBlockTileEntity(BlockCoord blockCoord, Player player) {
		this.nmsWrapper.updateBlockTileEntity(blockCoord, player);
	}

	@Override
	public void notifyBlockChange(World world, IBlockInfo blockInfo) {
		this.nmsWrapper.notifyBlockChange(world, blockInfo);
	}

	@Override
	public int getBlockLightLevel(World world, int x, int y, int z) {
		return this.nmsWrapper.getBlockLightLevel(world, x, y, z);
	}

	@Override
	public IBlockInfo getBlockInfo(World world, int x, int y, int z) {
		return this.nmsWrapper.getBlockInfo(world, x, y, z);
	}

	@Override
	public int loadChunkAndGetBlockId(World world, int x, int y, int z) {
		return this.nmsWrapper.loadChunkAndGetBlockId(world, x, y, z);
	}

	@Override
	public String getTextFromChatComponent(String json) {
		return this.nmsWrapper.getTextFromChatComponent(json);
	}

	@Override
	public boolean isHoe(Material item) {
		return this.nmsWrapper.isHoe(item);
	}

	@Override
	public boolean isSign(int combinedBlockId) {
		return this.nmsWrapper.isSign(combinedBlockId);
	}

	@Override
	public boolean isAir(int combinedBlockId) {
		return this.nmsWrapper.isAir(combinedBlockId);
	}

	@Override
	public boolean isTileEntity(int combinedBlockId) {
		return this.nmsWrapper.isTileEntity(combinedBlockId);
	}

	@Override
	public int getCaveAirBlockId() {
		return this.nmsWrapper.getCaveAirBlockId();
	}

	@Override
	public int getBitsPerBlock() {
		return this.nmsWrapper.getBitsPerBlock();
	}

	@Override
	public boolean canApplyPhysics(Material blockMaterial) {
		return this.nmsWrapper.canApplyPhysics(blockMaterial);
	}

	@Override
	public Set<Integer> getMaterialIds(Material material) {
		return this.nmsWrapper.getMaterialIds(material);
	}

	@Override
	public boolean sendBlockChange(Player player, Location blockLocation) {
		return this.nmsWrapper.sendBlockChange(player, blockLocation);
	}

	@Override
	public boolean hasLightArray() {
		return this.nmsWrapper.hasLightArray();
	}

	@Override
	public boolean hasBlockCount() {
		return this.nmsWrapper.hasBlockCount();
	}

	@Override
	public String getServerVersion() {
		return this.serverVersion;
	}

	@Override
	public boolean wasNmsFound() {
		return this.nmsWrapper != null;
	}
}