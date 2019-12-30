package com.lishid.orebfuscator.api.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.chunk.ChunkData;
import com.lishid.orebfuscator.api.chunk.IChunkMap;
import com.lishid.orebfuscator.api.config.IWorldConfig;
import com.lishid.orebfuscator.api.types.BlockCoord;

public interface ICalculations {

	public Result obfuscateOrUseCache(ChunkData chunkData, Player player, IWorldConfig worldConfig) throws Exception;

	public boolean areAjacentBlocksTransparent(IChunkMap manager, World world, boolean checkCurrentBlock, int x, int y, int z, int countdown) throws IOException;

	public boolean areAjacentBlocksBright(World world, int x, int y, int z, int countdown);

	public class Result {
		public byte[] output;
		public ArrayList<BlockCoord> removedEntities;
	}
}