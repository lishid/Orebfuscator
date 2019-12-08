/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.chunkmap;

public class ChunkMapBuffer {

	private static int _bitsPerBlock;

	public static int getBitsPerBlock() {
		return ChunkMapBuffer._bitsPerBlock;
	}

	private static final int BITS_PER_BLOCK_SIZE = 1;
	private static final int PALETTE_LENGTH_SIZE = 5;
	private static final int DATA_ARRAY_LENGTH_SIZE = 5;
	private static final int BLOCKS_PER_CHUNK_SECTION = 16 * 16 * 16;
	private static final int BLOCK_LIGHT_SIZE = BLOCKS_PER_CHUNK_SECTION / 2;
	private static final int SKY_LIGHT_SIZE = BLOCKS_PER_CHUNK_SECTION / 2;
	private static final int COLUMNS_PER_CHUNK = 16;

	private static int MAX_BYTES_PER_CHUNK;

	public static void init(int bitsPerBlock) {
		ChunkMapBuffer._bitsPerBlock = bitsPerBlock;

		ChunkMapBuffer.MAX_BYTES_PER_CHUNK = ChunkMapBuffer.COLUMNS_PER_CHUNK * (
				ChunkMapBuffer.BITS_PER_BLOCK_SIZE
				+ ChunkMapBuffer.PALETTE_LENGTH_SIZE
				+ ChunkMapBuffer.DATA_ARRAY_LENGTH_SIZE
				+ (ChunkMapBuffer.BLOCKS_PER_CHUNK_SECTION * _bitsPerBlock / 8)
				+ ChunkMapBuffer.BLOCK_LIGHT_SIZE + ChunkMapBuffer.SKY_LIGHT_SIZE
			);
	}

	public int[] palette;
	public byte[] output;
	public int[] outputPalette;
	public byte[] outputPaletteMap;
	public ChunkWriter writer;
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

	public ChunkMapBuffer() {
		this.palette = new int[256];
		this.output = new byte[MAX_BYTES_PER_CHUNK];
		this.outputPalette = new int[256];
		this.outputPaletteMap = new byte[65536];
		this.writer = new ChunkWriter(this.output);
		this.prevLayer = new ChunkLayer();
		this.prevLayer.map = new int[16 * 16];
		this.curLayer = new ChunkLayer();
		this.curLayer.map = new int[16 * 16];
		this.nextLayer = new ChunkLayer();
		this.nextLayer.map = new int[16 * 16];
	}

	public void clearLayers() {
		this.prevLayer.hasData = false;
		this.curLayer.hasData = false;
		this.nextLayer.hasData = false;
	}
}