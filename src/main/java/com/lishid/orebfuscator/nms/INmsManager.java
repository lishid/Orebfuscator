/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.types.BlockCoord;
import com.lishid.orebfuscator.types.BlockState;

public interface INmsManager {
	INBT createNBT();
	
	IChunkCache createChunkCache();
	
	IChunkManager getChunkManager(World world);
	
    void updateBlockTileEntity(BlockCoord blockCoord, Player player);

    void notifyBlockChange(World world, IBlockInfo blockInfo);
    
    int getBlockLightLevel(World world, int x, int y, int z);
    
	IBlockInfo getBlockInfo(World world, int x, int y, int z);
	
	BlockState getBlockState(World world, int x, int y, int z);
	
	int getBlockId(World world, int x, int y, int z);
}
