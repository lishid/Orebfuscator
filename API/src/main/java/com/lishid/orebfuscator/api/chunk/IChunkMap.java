package com.lishid.orebfuscator.api.chunk;

import java.io.IOException;

public interface IChunkMap extends AutoCloseable {

	public int getSectionCount();

	public int getY();

	public ChunkData getChunkData();

	public boolean inputHasNonAirBlock();

	public boolean initOutputPalette();

	public boolean addToOutputPalette(int blockData);

	public void initOutputSection() throws IOException;

	public void writeOutputBlock(int blockData) throws IOException;

	public void finalizeOutput() throws IOException;

	public byte[] createOutput();

	public int readNextBlock() throws IOException;

	public int get(int x, int y, int z) throws IOException;
}