package com.lishid.orebfuscator.api.chunk;

public abstract class IChunkMapBuffer {

	public int[] palette;
	public byte[] output;
	public int[] outputPalette;
	public byte[] outputPaletteMap;
	public IChunkWriter writer;
	public ChunkLayer prevLayer;
	public ChunkLayer curLayer;
	public ChunkLayer nextLayer;

    public int blockCount;
	public int bitsPerBlock;
	public int paletteLength;
	public int dataArrayLength;
	public int lightArrayLength;
	public int dataArrayStartIndex;
	public int outputPaletteLength;
	public int outputBitsPerBlock;

	public abstract void clearLayers();
}