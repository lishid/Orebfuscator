/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.types.BlockCoord;
import com.lishid.orebfuscator.types.BlockState;

public interface INmsManager {
	void setMaxLoadedCacheFiles(int value);
	
	INBT createNBT();
	
	IChunkCache createChunkCache();
	
    void updateBlockTileEntity(BlockCoord blockCoord, Player player);

    void notifyBlockChange(World world, IBlockInfo blockInfo);
    
    int getBlockLightLevel(World world, int x, int y, int z);
    
	IBlockInfo getBlockInfo(World world, int x, int y, int z);
	
	BlockState getBlockState(World world, int x, int y, int z);
	
	int getBlockId(World world, int x, int y, int z);

	int loadChunkAndGetBlockId(World world, int x, int y, int z);
	
	String getTextFromChatComponent(String json);

	void setBlockStateFromID(int id, BlockState blockState);
	
	void setBlockStateFromMaterial(Material type, BlockState blockState);
	
	BlockData getBlockDataFromBlockState(BlockState blockState);
}
