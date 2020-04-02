package net.imprex.orebfuscator.nms;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.imprex.orebfuscator.util.BlockCoords;

public interface NmsManager {

	AbstractRegionFileCache<?> getRegionFileCache();

	int getBitsPerBlock();

	int getMaterialSize();

	Set<Integer> getMaterialIds(Material material);

	int getCaveAirBlockId();

	boolean isHoe(Material material);

	boolean isAir(int blockId);

	boolean isTileEntity(int blockId);

	boolean canApplyPhysics(Material material);

	void updateBlockTileEntity(BlockCoords blockCoord, Player player);

	int getBlockLightLevel(World world, int x, int y, int z);

	BlockStateHolder getBlockState(World world, int x, int y, int z);

	int loadChunkAndGetBlockId(World world, int x, int y, int z);

	boolean sendBlockChange(Player player, Location blockLocation);

	void close();
}