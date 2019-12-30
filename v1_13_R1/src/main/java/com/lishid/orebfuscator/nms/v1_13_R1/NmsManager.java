/**
 * @author lishid
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_13_R1;

import net.minecraft.server.v1_13_R1.Block;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.Chunk;
import net.minecraft.server.v1_13_R1.ChunkProviderServer;
import net.minecraft.server.v1_13_R1.IBlockData;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.Packet;
import net.minecraft.server.v1_13_R1.TileEntity;
import net.minecraft.server.v1_13_R1.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.nms.IBlockInfo;
import com.lishid.orebfuscator.api.nms.IChunkCache;
import com.lishid.orebfuscator.api.nms.INBT;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.types.BlockCoord;
import com.lishid.orebfuscator.api.types.ConfigDefaults;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class NmsManager implements INmsManager {
	private static final int BITS_PER_BLOCK = 14;

	private int BLOCK_ID_CAVE_AIR;
	private Set<Integer> BLOCK_ID_AIRS;
	private Set<Integer> BLOCK_ID_SIGNS;

	private ConfigDefaults configDefaults;
	private int maxLoadedCacheFiles;
	private HashMap<Material, Set<Integer>> materialIds;

	public NmsManager() {
		initBlockIds();

		this.BLOCK_ID_CAVE_AIR = getMaterialIds(Material.CAVE_AIR).iterator().next();
		this.BLOCK_ID_AIRS = convertMaterialsToSet(new Material[] { Material.AIR, Material.CAVE_AIR, Material.VOID_AIR });
		this.BLOCK_ID_SIGNS = convertMaterialsToSet(new Material[] { Material.SIGN, Material.WALL_SIGN });

		this.configDefaults = new ConfigDefaults();

		// Default World

		this.configDefaults.defaultProximityHiderBlockIds = convertMaterialsToIds(new Material[] {
				Material.DISPENSER,
				Material.SPAWNER,
				Material.CHEST,
				Material.HOPPER,
				Material.CRAFTING_TABLE,
				Material.FURNACE,
				Material.ENCHANTING_TABLE,
				Material.EMERALD_ORE,
				Material.ENDER_CHEST,
				Material.ANVIL,
				Material.CHIPPED_ANVIL,
				Material.DAMAGED_ANVIL,
				Material.TRAPPED_CHEST,
				Material.DIAMOND_ORE
		});

		this.configDefaults.defaultDarknessBlockIds = convertMaterialsToIds(new Material[] {
				Material.SPAWNER,
				Material.CHEST
		});

		this.configDefaults.defaultMode1BlockId = getMaterialIds(Material.STONE).iterator().next();
		this.configDefaults.defaultProximityHiderSpecialBlockId = getMaterialIds(Material.STONE).iterator().next();

		// The End

		this.configDefaults.endWorldRandomBlockIds = convertMaterialsToIds(new Material[] {
				Material.BEDROCK,
				Material.OBSIDIAN,
				Material.END_STONE,
				Material.PURPUR_BLOCK,
				Material.END_STONE_BRICKS
		});

		this.configDefaults.endWorldObfuscateBlockIds = convertMaterialsToIds(new Material[] {
				Material.END_STONE
		});

		this.configDefaults.endWorldMode1BlockId = getMaterialIds(Material.END_STONE).iterator().next();
		this.configDefaults.endWorldRequiredObfuscateBlockIds = convertMaterialsToIds(new Material[] { Material.END_STONE });

		// Nether World

		this.configDefaults.netherWorldRandomBlockIds = convertMaterialsToIds(new Material[] {
				Material.GRAVEL,
				Material.NETHERRACK,
				Material.SOUL_SAND,
				Material.NETHER_BRICKS,
				Material.NETHER_QUARTZ_ORE
		});

		this.configDefaults.netherWorldObfuscateBlockIds = convertMaterialsToIds(new Material[] {
				Material.NETHERRACK,
				Material.NETHER_QUARTZ_ORE
		});

		this.configDefaults.netherWorldMode1BlockId = getMaterialIds(Material.NETHERRACK).iterator().next();

		this.configDefaults.netherWorldRequiredObfuscateBlockIds = convertMaterialsToIds(new Material[] {
				Material.NETHERRACK
		});

		// Normal World

		this.configDefaults.normalWorldRandomBlockIds = convertMaterialsToIds(new Material[] {
				Material.STONE,
				Material.COBBLESTONE,
				Material.OAK_PLANKS,
				Material.GOLD_ORE,
				Material.IRON_ORE,
				Material.COAL_ORE,
				Material.LAPIS_ORE,
				Material.TNT,
				Material.MOSSY_COBBLESTONE,
				Material.OBSIDIAN,
				Material.DIAMOND_ORE,
				Material.REDSTONE_ORE,
				Material.CLAY,
				Material.EMERALD_ORE
		});

		this.configDefaults.normalWorldObfuscateBlockIds = convertMaterialsToIds(new Material[] {
				Material.GOLD_ORE,
				Material.IRON_ORE,
				Material.COAL_ORE,
				Material.LAPIS_ORE,
				Material.CHEST,
				Material.DIAMOND_ORE,
				Material.ENDER_CHEST,
				Material.REDSTONE_ORE,
				Material.CLAY,
				Material.EMERALD_ORE
		});

		this.configDefaults.normalWorldMode1BlockId = getMaterialIds(Material.STONE).iterator().next();

		this.configDefaults.normalWorldRequiredObfuscateBlockIds = convertMaterialsToIds(new Material[] {
				Material.STONE
		});
	}

	private void initBlockIds() {
		this.materialIds = new HashMap<>();

		Block.REGISTRY_ID.iterator().forEachRemaining(blockData -> {
			Material material = CraftBlockData.fromData(blockData).getMaterial();

			if(material.isBlock()) {
				int materialId = Block.REGISTRY_ID.getId(blockData);

				Set<Integer> ids = this.materialIds.get(material);

				if (ids == null) {
					this.materialIds.put(material, ids = new HashSet<>());
				}

				ids.add(materialId);
			}
		});
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
		CraftWorld world = (CraftWorld)player.getWorld();
		TileEntity tileEntity = world.getTileEntityAt(blockCoord.x, blockCoord.y, blockCoord.z);

		if (tileEntity == null) {
			return;
		}

		Packet<?> packet = tileEntity.getUpdatePacket();

		if (packet != null) {
			CraftPlayer player2 = (CraftPlayer)player;
			player2.getHandle().playerConnection.sendPacket(packet);
		}
	}

	public void notifyBlockChange(World world, IBlockInfo blockInfo) {
		BlockPosition blockPosition = new BlockPosition(blockInfo.getX(), blockInfo.getY(), blockInfo.getZ());
		IBlockData blockData = ((BlockInfo)blockInfo).getBlockData();

		((CraftWorld)world).getHandle().notify(blockPosition, blockData, blockData, 0);
	}

	public int getBlockLightLevel(World world, int x, int y, int z) {
		return ((CraftWorld)world).getHandle().getLightLevel(new BlockPosition(x, y, z));
	}

	public IBlockInfo getBlockInfo(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z, false);

		return blockData != null
				? new BlockInfo(x, y, z, blockData)
				: null;
	}

	public int loadChunkAndGetBlockId(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z, true);
		return blockData != null ? Block.getCombinedId(blockData): -1;
	}

	public String getTextFromChatComponent(String json) {
		IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(json);
		return CraftChatMessage.fromComponent(component);
	}

	public boolean isHoe(Material item) {
		return item == Material.WOODEN_HOE
				|| item == Material.IRON_HOE
				|| item == Material.GOLDEN_HOE
				|| item == Material.DIAMOND_HOE;
	}

	public boolean isSign(int combinedBlockId) {
		return BLOCK_ID_SIGNS.contains(combinedBlockId);
	}

	public boolean isAir(int combinedBlockId) {
		return BLOCK_ID_AIRS.contains(combinedBlockId);
	}

	public boolean isTileEntity(int combinedBlockId) {
		return Block.getByCombinedId(combinedBlockId).getBlock().isTileEntity();
	}

	public int getCaveAirBlockId() {
		return BLOCK_ID_CAVE_AIR;
	}

	public int getBitsPerBlock() {
		return BITS_PER_BLOCK;
	}

	public boolean canApplyPhysics(Material blockMaterial) {
		return blockMaterial == Material.AIR
				|| blockMaterial == Material.CAVE_AIR
				|| blockMaterial == Material.VOID_AIR
				|| blockMaterial == Material.FIRE
				|| blockMaterial == Material.WATER
				|| blockMaterial == Material.LAVA;
	}

	public Set<Integer> getMaterialIds(Material material) {
		return this.materialIds.get(material);
	}

	public boolean sendBlockChange(Player player, Location blockLocation) {
		IBlockData blockData = getBlockData(blockLocation.getWorld(), blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ(), false);

		if(blockData == null) return false;

		CraftBlockData craftBlockData = CraftBlockData.fromData(blockData);

		player.sendBlockChange(blockLocation, craftBlockData);

		return true;
	}

	private static IBlockData getBlockData(World world, int x, int y, int z, boolean loadChunk) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		WorldServer worldServer = ((CraftWorld)world).getHandle();
		ChunkProviderServer chunkProviderServer = worldServer.getChunkProviderServer();

		if(!loadChunk && !chunkProviderServer.isLoaded(chunkX, chunkZ)) return null;

		Chunk chunk = chunkProviderServer.getOrLoadChunkAt(chunkX, chunkZ);

		return chunk != null ? chunk.getBlockData(x, y, z) : null;
	}

	private Set<Integer> convertMaterialsToSet(Material[] materials) {
		Set<Integer> ids = new HashSet<>();

		for(Material material : materials) {
			ids.addAll(getMaterialIds(material));
		}

		return ids;
	}

	private int[] convertMaterialsToIds(Material[] materials) {
		Set<Integer> ids = convertMaterialsToSet(materials);

		int[] result = new int[ids.size()];
		int index = 0;

		for(int id : ids) {
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