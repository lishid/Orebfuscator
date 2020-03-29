package net.imprex.orebfuscator.nms.v1_15_R1;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.imprex.orebfuscator.config.CacheConfig;
import net.imprex.orebfuscator.config.Config;
import net.imprex.orebfuscator.nms.AbstractBlockState;
import net.imprex.orebfuscator.nms.AbstractNmsManager;
import net.imprex.orebfuscator.nms.AbstractRegionFileCache;
import net.imprex.orebfuscator.util.BlockCoords;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ChunkProviderServer;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.WorldServer;

public class NmsManager extends AbstractNmsManager {

	private final int blockIdCaveAir;
	private final Set<Integer> blockIdAir;

	public NmsManager(Config config) {
		super(config);

		for (IBlockData blockData : Block.REGISTRY_ID) {
			Material material = CraftBlockData.fromData(blockData).getMaterial();
			int id = Block.getCombinedId(blockData);
			this.registerMaterialId(material, id);
		}

		this.blockIdCaveAir = this.getMaterialIds(Material.CAVE_AIR).iterator().next();
		this.blockIdAir = this.mergeMaterialIds(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);
	}

	@Override
	protected AbstractRegionFileCache<?> createRegionFileCache(CacheConfig cacheConfig) {
		return new RegionFileCache(cacheConfig);
	}

	@Override
	public int getMaterialSize() {
		return Block.REGISTRY_ID.a();
	}

	@Override
	public void updateBlockTileEntity(BlockCoords blockCoord, Player player) {
		try {
			EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
			WorldServer world = entityPlayer.getWorldServer();

			TileEntity tileEntity = world.getTileEntity(new BlockPosition(blockCoord.x, blockCoord.y, blockCoord.z));
			if (tileEntity == null) {
				return;
			}

			Packet<?> packet = tileEntity.getUpdatePacket();
			if (packet != null) {
				entityPlayer.playerConnection.sendPacket(packet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public AbstractBlockState<?> getBlockInfo(World world, int x, int y, int z) {
		IBlockData blockData = this.getBlockData(world, x, y, z, false);
		return blockData != null ? new BlockState(x, y, z, world, blockData) : null;
	}

	@Override
	public int getBlockLightLevel(World world, int x, int y, int z) {
		return ((CraftWorld) world).getHandle().getLightLevel(new BlockPosition(x, y, z));
	}

	@Override
	public int loadChunkAndGetBlockId(World world, int x, int y, int z) {
		IBlockData blockData = this.getBlockData(world, x, y, z, true);
		return blockData != null ? Block.getCombinedId(blockData) : -1;
	}

	@Override
	public boolean isHoe(Material material) {
		switch (material) {
		case WOODEN_HOE:
		case STONE_HOE:
		case IRON_HOE:
		case GOLDEN_HOE:
		case DIAMOND_HOE:
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean isAir(int blockId) {
		return this.blockIdAir.contains(blockId);
	}

	@Override
	public boolean isTileEntity(int combinedBlockId) {
		return Block.getByCombinedId(combinedBlockId).getBlock().isTileEntity();
	}

	@Override
	public int getCaveAirBlockId() {
		return this.blockIdCaveAir;
	}

	@Override
	public int getBitsPerBlock() {
		return 14;
	}

	@Override
	public boolean canApplyPhysics(Material material) {
		switch (material) {
		case AIR:
		case CAVE_AIR:
		case VOID_AIR:
		case FIRE:
		case WATER:
		case LAVA:
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean sendBlockChange(Player player, Location location) {
		IBlockData blockData = this.getBlockData(location.getWorld(), location.getBlockX(), location.getBlockY(),
				location.getBlockZ(), false);

		if (blockData == null) {
			return false;
		}

		PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(((CraftWorld) location.getWorld()).getHandle(),
				new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
		packet.block = blockData;

		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		return true;
	}

	@Override
	public boolean hasLightArray() {
		return false;
	}

	@Override
	public boolean hasBlockCount() {
		return true;
	}

	private IBlockData getBlockData(World world, int x, int y, int z, boolean loadChunk) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		ChunkProviderServer chunkProviderServer = ((CraftWorld) world).getHandle().getChunkProvider();

		if (!loadChunk && !chunkProviderServer.isLoaded(chunkX, chunkZ)) {
			return null;
		}

		Chunk chunk = chunkProviderServer.getChunkAt(chunkX, chunkZ, true);
		return chunk != null ? chunk.getType(new BlockPosition(x, y, z)) : null;
	}
}