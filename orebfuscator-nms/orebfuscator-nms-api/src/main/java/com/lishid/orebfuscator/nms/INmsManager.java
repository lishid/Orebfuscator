/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.imprex.orebfuscator.nms.AbstractBlockState;
import net.imprex.orebfuscator.nms.AbstractRegionFileCache;
import net.imprex.orebfuscator.util.BlockCoords;

public interface INmsManager {

	AbstractRegionFileCache<?> getRegionFileCache();

	void updateBlockTileEntity(BlockCoords blockCoord, Player player);

	int getBlockLightLevel(World world, int x, int y, int z);

	AbstractBlockState<?> getBlockInfo(World world, int x, int y, int z);

	int loadChunkAndGetBlockId(World world, int x, int y, int z);

	boolean isHoe(Material item);

	boolean isAir(int combinedBlockId);

	boolean isTileEntity(int combinedBlockId);

	int getCaveAirBlockId();

	int getBitsPerBlock();

	boolean canApplyPhysics(Material blockMaterial);

	int getMaterialSize();

	Set<Integer> getMaterialIds(Material material);

	boolean sendBlockChange(Player player, Location blockLocation);

	boolean hasLightArray();

	boolean hasBlockCount();

	void close();
}