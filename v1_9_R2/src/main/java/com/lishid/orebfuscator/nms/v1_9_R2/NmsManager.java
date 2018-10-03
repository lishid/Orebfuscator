/**
 * @author lishid
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_9_R2;

import com.lishid.orebfuscator.types.ConfigDefaults;
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

import com.lishid.orebfuscator.nms.IBlockInfo;
import com.lishid.orebfuscator.nms.IChunkCache;
import com.lishid.orebfuscator.nms.INBT;
import com.lishid.orebfuscator.nms.INmsManager;
import com.lishid.orebfuscator.types.BlockCoord;

import java.util.Set;

public class NmsManager implements INmsManager {
	private ConfigDefaults configDefaults;
	private int maxLoadedCacheFiles;

	public NmsManager() {
		this.configDefaults = new ConfigDefaults();

		// Default World

		this.configDefaults.defaultProximityHiderBlockIds = new int[] {
				getMaterialId(Material.DISPENSER),
				getMaterialId(Material.MOB_SPAWNER),
				getMaterialId(Material.CHEST),
				getMaterialId(Material.HOPPER),
				getMaterialId(Material.WORKBENCH),
				getMaterialId(Material.FURNACE),
				getMaterialId(Material.BURNING_FURNACE),
				getMaterialId(Material.ENCHANTMENT_TABLE),
				getMaterialId(Material.EMERALD_ORE),
				getMaterialId(Material.ENDER_CHEST),
				getMaterialId(Material.ANVIL),
				getMaterialId(Material.TRAPPED_CHEST),
				getMaterialId(Material.DIAMOND_ORE)
		};

		this.configDefaults.defaultDarknessBlockIds = new int[] {
				getMaterialId(Material.MOB_SPAWNER),
				getMaterialId(Material.CHEST)
		};

		this.configDefaults.defaultMode1BlockId = getMaterialId(Material.STONE);
		this.configDefaults.defaultProximityHiderSpecialBlockId = getMaterialId(Material.STONE);

		// The End

		this.configDefaults.endWorldRandomBlockIds = new int[] {
				getMaterialId(Material.BEDROCK),
				getMaterialId(Material.OBSIDIAN),
				getMaterialId(Material.ENDER_STONE),
				getMaterialId(Material.PURPUR_BLOCK),
				getMaterialId(Material.END_BRICKS)
		};

		this.configDefaults.endWorldObfuscateBlockIds = new int[] {
				getMaterialId(Material.ENDER_STONE)
		};

		this.configDefaults.endWorldMode1BlockId = getMaterialId(Material.ENDER_STONE);
		this.configDefaults.endWorldRequiredObfuscateBlockIds = new int[] { getMaterialId(Material.ENDER_STONE) };

		// Nether World

		this.configDefaults.netherWorldRandomBlockIds = new int[] {
				getMaterialId(Material.GRAVEL),
				getMaterialId(Material.NETHERRACK),
				getMaterialId(Material.SOUL_SAND),
				getMaterialId(Material.NETHER_BRICK),
				getMaterialId(Material.QUARTZ_ORE)
		};

		this.configDefaults.netherWorldObfuscateBlockIds = new int[] {
				getMaterialId(Material.NETHERRACK),
				getMaterialId(Material.QUARTZ_ORE)
		};

		this.configDefaults.netherWorldMode1BlockId = getMaterialId(Material.NETHERRACK);

		this.configDefaults.netherWorldRequiredObfuscateBlockIds = new int[] {
				getMaterialId(Material.NETHERRACK)
		};

		// Normal World

		this.configDefaults.normalWorldRandomBlockIds = new int[] {
				getMaterialId(Material.STONE),
				getMaterialId(Material.COBBLESTONE),
				getMaterialId(Material.WOOD),
				getMaterialId(Material.GOLD_ORE),
				getMaterialId(Material.IRON_ORE),
				getMaterialId(Material.COAL_ORE),
				getMaterialId(Material.LAPIS_ORE),
				getMaterialId(Material.TNT),
				getMaterialId(Material.MOSSY_COBBLESTONE),
				getMaterialId(Material.OBSIDIAN),
				getMaterialId(Material.DIAMOND_ORE),
				getMaterialId(Material.REDSTONE_ORE),
				getMaterialId(Material.CLAY),
				getMaterialId(Material.EMERALD_ORE)
		};

		this.configDefaults.normalWorldObfuscateBlockIds = new int[] {
				getMaterialId(Material.GOLD_ORE),
				getMaterialId(Material.IRON_ORE),
				getMaterialId(Material.COAL_ORE),
				getMaterialId(Material.LAPIS_ORE),
				getMaterialId(Material.CHEST),
				getMaterialId(Material.DIAMOND_ORE),
				getMaterialId(Material.ENDER_CHEST),
				getMaterialId(Material.REDSTONE_ORE),
				getMaterialId(Material.CLAY),
				getMaterialId(Material.EMERALD_ORE)
		};

		this.configDefaults.normalWorldMode1BlockId = getMaterialId(Material.STONE);

		this.configDefaults.normalWorldRequiredObfuscateBlockIds = new int[] {
				getMaterialId(Material.STONE)
		};
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
		return blockData != null ? Block.getId(blockData.getBlock()): -1;
	}
	
	public String getTextFromChatComponent(String json) {
		IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(json);
		return CraftChatMessage.fromComponent(component);
	}

	public boolean isHoe(Material item) {
		return item == Material.WOOD_HOE
				|| item == Material.IRON_HOE
				|| item == Material.GOLD_HOE
				|| item == Material.DIAMOND_HOE;
	}

	public boolean isSign(int combinedBlockId) {
		return combinedBlockId == getMaterialId(Material.WALL_SIGN)
				|| combinedBlockId == getMaterialId(Material.SIGN_POST);
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
		return blockMaterial == Material.AIR
				|| blockMaterial == Material.FIRE
				|| blockMaterial == Material.WATER
				|| blockMaterial == Material.STATIONARY_WATER
				|| blockMaterial == Material.LAVA
				|| blockMaterial == Material.STATIONARY_LAVA;
	}

	@SuppressWarnings("deprecation")
	public int getMaterialId(Material material) {
		return material.getId() << 4;
	}

	public Set<Integer> getMaterialIds(Material material) {
		return null;
	}

	public int getTypeId(int combinedBlockId) {
		return combinedBlockId & ~(0x0F);
	}

	@SuppressWarnings("deprecation")
	public boolean sendBlockChange(Player player, Location blockLocation) {
		IBlockData blockData = getBlockData(blockLocation.getWorld(), blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ(), false);

		if(blockData == null) return false;

		Block block = blockData.getBlock();
		int blockId = Block.getId(block);
		byte meta = (byte)block.toLegacyData(blockData);

		player.sendBlockChange(blockLocation, blockId, meta);

		return true;
	}

	private static IBlockData getBlockData(World world, int x, int y, int z, boolean loadChunk) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		WorldServer worldServer = ((CraftWorld)world).getHandle();
		ChunkProviderServer chunkProviderServer = worldServer.getChunkProviderServer();

		if(!loadChunk && !chunkProviderServer.isLoaded(chunkX, chunkZ)) return null;
		
		Chunk chunk = chunkProviderServer.getOrLoadChunkAt(chunkX, chunkZ);

		return chunk != null ? chunk.getBlockData(new BlockPosition(x, y, z)) : null;
	}
}
