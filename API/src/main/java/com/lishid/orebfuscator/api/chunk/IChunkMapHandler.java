package com.lishid.orebfuscator.api.chunk;

import java.io.IOException;

public interface IChunkMapHandler {

	public void pushBuffer(IChunkMapBuffer buffer);

	public IChunkMap create(ChunkData chunkData) throws IOException;

	public int getBitsPerBlock();

	public int getMaxBytesPerChunk();
}