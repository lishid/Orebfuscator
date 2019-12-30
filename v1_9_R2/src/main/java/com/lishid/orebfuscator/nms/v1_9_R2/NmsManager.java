/**
 * @author lishid
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_9_R2;

import com.google.common.collect.ImmutableList;
import com.lishid.orebfuscator.api.nms.IBlockInfo;
import com.lishid.orebfuscator.api.nms.IChunkCache;
import com.lishid.orebfuscator.api.nms.INBT;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.types.BlockCoord;
import com.lishid.orebfuscator.api.types.ConfigDefaults;

import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.Chunk;
import net.minecraft.server.v1_9_R2.ChunkProviderServer;
import net.minecraft.server.v1_9_R2.IBlockData;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class NmsManager implements INmsManager {
	private ConfigDefaults configDefaults;
	private int maxLoadedCacheFiles;

	public NmsManager() {
		this.configDefaults = new ConfigDefaults();

		// Default World

		this.configDefaults.defaultProximityHiderBlockIds = convertMaterialsToIds(new Material[] { Material.DISPENSER,
				Material.MOB_SPAWNER, Material.CHEST, Material.HOPPER, Material.WORKBENCH, Material.FURNACE,
				Material.BURNING_FURNACE, Material.ENCHANTMENT_TABLE, Material.EMERALD_ORE, Material.ENDER_CHEST,
				Material.ANVIL, Material.TRAPPED_CHEST, Material.DIAMOND_ORE });

		this.configDefaults.defaultDarknessBlockIds = convertMaterialsToIds(
				new Material[] { Material.MOB_SPAWNER, Material.CHEST });

		this.configDefaults.defaultMode1BlockId = getMaterialIds(Material.STONE).iterator().next();
		this.configDefaults.defaultProximityHiderSpecialBlockId = getMaterialIds(Material.STONE).iterator().next();

		// The End

		this.configDefaults.endWorldRandomBlockIds = convertMaterialsToIds(new Material[] { Material.BEDROCK,
				Material.OBSIDIAN, Material.ENDER_STONE, Material.PURPUR_BLOCK, Material.END_BRICKS });

		this.configDefaults.endWorldObfuscateBlockIds = convertMaterialsToIds(new Material[] { Material.ENDER_STONE });

		this.configDefaults.endWorldMode1BlockId = getMaterialIds(Material.ENDER_STONE).iterator().next();
		this.configDefaults.endWorldRequiredObfuscateBlockIds = convertMaterialsToIds(
				new Material[] { Material.ENDER_STONE });

		// Nether World

		this.configDefaults.netherWorldRandomBlockIds = convertMaterialsToIds(new Material[] { Material.GRAVEL,
				Material.NETHERRACK, Material.SOUL_SAND, Material.NETHER_BRICK, Material.QUARTZ_ORE });

		this.configDefaults.netherWorldObfuscateBlockIds = convertMaterialsToIds(
				new Material[] { Material.NETHERRACK, Material.QUARTZ_ORE });

		this.configDefaults.netherWorldMode1BlockId = getMaterialIds(Material.NETHERRACK).iterator().next();

		this.configDefaults.netherWorldRequiredObfuscateBlockIds = convertMaterialsToIds(
				new Material[] { Material.NETHERRACK });

		// Normal World

		this.configDefaults.normalWorldRandomBlockIds = convertMaterialsToIds(new Material[] { Material.STONE,
				Material.COBBLESTONE, Material.WOOD, Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE,
				Material.LAPIS_ORE, Material.TNT, Material.MOSSY_COBBLESTONE, Material.OBSIDIAN, Material.DIAMOND_ORE,
				Material.REDSTONE_ORE, Material.CLAY, Material.EMERALD_ORE });

		this.configDefaults.normalWorldObfuscateBlockIds = convertMaterialsToIds(new Material[] { Material.GOLD_ORE,
				Material.IRON_ORE, Material.COAL_ORE, Material.LAPIS_ORE, Material.CHEST, Material.DIAMOND_ORE,
				Material.ENDER_CHEST, Material.REDSTONE_ORE, Material.CLAY, Material.EMERALD_ORE });

		this.configDefaults.normalWorldMode1BlockId = getMaterialIds(Material.STONE).iterator().next();

		this.configDefaults.normalWorldRequiredObfuscateBlockIds = convertMaterialsToIds(
				new Material[] { Material.STONE });
	}

	public ConfigDefaults getConfigDefaults() {
		return this.configDefaults;
	}

	public void setMaxLoadedCacheFiles(int value) {
		this.maxLoadedCacheFiles = value;
	}

	public INBT createNBT() {
		return new NBT();
	}

	public IChunkCache createChunkCache() {
		return new ChunkCache(this.maxLoadedCacheFiles);
	}

	public void updateBlockTileEntity(BlockCoord blockCoord, Player player) {
		CraftWorld world = (CraftWorld) player.getWorld();
		TileEntity tileEntity = world.getTileEntityAt(blockCoord.x, blockCoord.y, blockCoord.z);

		if (tileEntity == null) {
			return;
		}

		Packet<?> packet = tileEntity.getUpdatePacket();

		if (packet != null) {
			CraftPlayer player2 = (CraftPlayer) player;
			player2.getHandle().playerConnection.sendPacket(packet);
		}
	}

	public void notifyBlockChange(World world, IBlockInfo blockInfo) {
		BlockPosition blockPosition = new BlockPosition(blockInfo.getX(), blockInfo.getY(), blockInfo.getZ());
		IBlockData blockData = ((BlockInfo) blockInfo).getBlockData();

		((CraftWorld) world).getHandle().notify(blockPosition, blockData, blockData, 0);
	}

	public int getBlockLightLevel(World world, int x, int y, int z) {
		return ((CraftWorld) world).getHandle().getLightLevel(new BlockPosition(x, y, z));
	}

	public IBlockInfo getBlockInfo(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z, false);

		return blockData != null ? new BlockInfo(x, y, z, blockData) : null;
	}

	public int loadChunkAndGetBlockId(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z, true);
		return blockData != null ? Block.getId(blockData.getBlock()) : -1;
	}

	public String getTextFromChatComponent(String json) {
		IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(json);
		return CraftChatMessage.fromComponent(component);
	}

	public boolean isHoe(Material item) {
		return item == Material.WOOD_HOE || item == Material.IRON_HOE || item == Material.GOLD_HOE
				|| item == Material.DIAMOND_HOE;
	}

	@SuppressWarnings("deprecation")
	public boolean isSign(int combinedBlockId) {
		int typeId = combinedBlockId >> 4;

		return typeId == Material.WALL_SIGN.getId() || typeId == Material.SIGN_POST.getId();
	}

	public boolean isAir(int combinedBlockId) {
		return combinedBlockId == 0;
	}

	public boolean isTileEntity(int combinedBlockId) {
		return Block.getByCombinedId(combinedBlockId).getBlock().isTileEntity();
	}

	public int getCaveAirBlockId() {
		return 0;
	}

	public int getBitsPerBlock() {
		return 13;
	}

	public boolean canApplyPhysics(Material blockMaterial) {
		return blockMaterial == Material.AIR || blockMaterial == Material.FIRE || blockMaterial == Material.WATER
				|| blockMaterial == Material.STATIONARY_WATER || blockMaterial == Material.LAVA
				|| blockMaterial == Material.STATIONARY_LAVA;
	}

	@SuppressWarnings("deprecation")
	public Set<Integer> getMaterialIds(Material material) {
		Set<Integer> ids = new HashSet<>();
		int blockId = material.getId() << 4;
		Block block = Block.getById(material.getId());
		ImmutableList<IBlockData> blockDataList = block.t().a();

		for (IBlockData blockData : blockDataList) {
			ids.add(blockId | block.toLegacyData(blockData));
		}

		return ids;
	}

	@SuppressWarnings("deprecation")
	public boolean sendBlockChange(Player player, Location blockLocation) {
		IBlockData blockData = getBlockData(blockLocation.getWorld(), blockLocation.getBlockX(),
				blockLocation.getBlockY(), blockLocation.getBlockZ(), false);

		if (blockData == null)
			return false;

		Block block = blockData.getBlock();
		int blockId = Block.getId(block);
		byte meta = (byte) block.toLegacyData(blockData);

		player.sendBlockChange(blockLocation, blockId, meta);

		return true;
	}

	private static IBlockData getBlockData(World world, int x, int y, int z, boolean loadChunk) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		WorldServer worldServer = ((CraftWorld) world).getHandle();
		ChunkProviderServer chunkProviderServer = worldServer.getChunkProviderServer();

		if (!loadChunk && !chunkProviderServer.isLoaded(chunkX, chunkZ))
			return null;

		Chunk chunk = chunkProviderServer.getOrLoadChunkAt(chunkX, chunkZ);

		return chunk != null ? chunk.getBlockData(new BlockPosition(x, y, z)) : null;
	}

	private Set<Integer> convertMaterialsToSet(Material[] materials) {
		Set<Integer> ids = new HashSet<>();

		for (Material material : materials) {
			ids.addAll(getMaterialIds(material));
		}

		return ids;
	}

	private int[] convertMaterialsToIds(Material[] materials) {
		Set<Integer> ids = convertMaterialsToSet(materials);

		int[] result = new int[ids.size()];
		int index = 0;

		for (int id : ids) {
			result[index++] = id;
		}

		return result;
	}

	@Override
	public boolean hasLightArray() {
		return true;
	}

	@Override
	public boolean hasBlockCount() {
		return false;
	}

	@Override
	public boolean wasNmsFound() {
		return false;
	}

	@Override
	public String getServerVersion() {
		return null;
	}
}
