/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms;

import com.lishid.orebfuscator.types.ConfigDefaults;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.types.BlockCoord;

import java.util.Set;

public interface INmsManager {
    ConfigDefaults getConfigDefaults();

    void setMaxLoadedCacheFiles(int value);
	
	INBT createNBT();
	
	IChunkCache createChunkCache();
	
    void updateBlockTileEntity(BlockCoord blockCoord, Player player);

    void notifyBlockChange(World world, IBlockInfo blockInfo);
    
    int getBlockLightLevel(World world, int x, int y, int z);
    
	IBlockInfo getBlockInfo(World world, int x, int y, int z);

	int loadChunkAndGetBlockId(World world, int x, int y, int z);
	
	String getTextFromChatComponent(String json);

	boolean isHoe(Material item);

	boolean isSign(int combinedBlockId);

    boolean isAir(int combinedBlockId);

	boolean isTileEntity(int combinedBlockId);

	int getCaveAirBlockId();

	int getBitsPerBlock();

	boolean canApplyPhysics(Material blockMaterial);

	Set<Integer> getMaterialIds(Material material);

	int getTypeId(int combinedBlockId);

	boolean sendBlockChange(Player player, Location blockLocation);
}