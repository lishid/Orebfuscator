package com.lishid.orebfuscator.chunkmap;

import java.io.IOException;
import java.util.Stack;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.chunk.ChunkData;
import com.lishid.orebfuscator.api.chunk.IChunkMapBuffer;
import com.lishid.orebfuscator.api.chunk.IChunkMapHandler;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.handler.CraftHandler;

public class ChunkMapHandler extends CraftHandler implements IChunkMapHandler {

	private static final int BITS_PER_BLOCK_SIZE = 1;
	private static final int PALETTE_LENGTH_SIZE = 5;
	private static final int DATA_ARRAY_LENGTH_SIZE = 5;
	private static final int BLOCKS_PER_CHUNK_SECTION = 16 * 16 * 16;
	private static final int BLOCK_LIGHT_SIZE = BLOCKS_PER_CHUNK_SECTION / 2;
	private static final int SKY_LIGHT_SIZE = BLOCKS_PER_CHUNK_SECTION / 2;
	private static final int COLUMNS_PER_CHUNK = 16;

	private final Object _lock = new Object();
	private final Stack<IChunkMapBuffer> _bufferStack = new Stack<>();

	private INmsManager nmsHandler;

	private int bitsPerBlock;
	private int maxBytesPerChunk;

	public ChunkMapHandler(Orebfuscator core) {
		super(core);
	}

	@Override
	public void onInit() {
		this.nmsHandler = this.plugin.getNmsManager();

		this.bitsPerBlock = this.nmsHandler.getBitsPerBlock();
		this.maxBytesPerChunk = ChunkMapHandler.COLUMNS_PER_CHUNK * (
				ChunkMapHandler.BITS_PER_BLOCK_SIZE
				+ ChunkMapHandler.PALETTE_LENGTH_SIZE
				+ ChunkMapHandler.DATA_ARRAY_LENGTH_SIZE
				+ (ChunkMapHandler.BLOCKS_PER_CHUNK_SECTION * this.bitsPerBlock / 8)
				+ ChunkMapHandler.BLOCK_LIGHT_SIZE + ChunkMapHandler.SKY_LIGHT_SIZE
			);
	}

	private IChunkMapBuffer popBuffer() {
		synchronized (this._lock) {
			return this._bufferStack.isEmpty() ? new ChunkMapBuffer(this.maxBytesPerChunk) : this._bufferStack.pop();
		}
	}

	public void pushBuffer(IChunkMapBuffer buffer) {
		synchronized (this._lock) {
			this._bufferStack.push(buffer);
		}
	}

	public ChunkMap create(ChunkData chunkData) throws IOException {
		ChunkMap manager = new ChunkMap(this.plugin);
		manager.chunkData = chunkData;
		manager.buffer = this.popBuffer();
		manager.reader = new ChunkReader(chunkData.data);
		manager.sectionCount = 0;
		manager.sectionIndex = -1;
		manager.minX = chunkData.chunkX << 4;
		manager.maxX = manager.minX + 15;
		manager.minZ = chunkData.chunkZ << 4;
		manager.maxZ = manager.minZ + 15;

		manager.buffer.lightArrayLength = this.nmsHandler.hasLightArray() ? 2048 : 0;

		if (chunkData.isOverworld) {
			manager.buffer.lightArrayLength <<= 1;
		}

		manager.buffer.writer.init();

		int mask = chunkData.primaryBitMask;

		while (mask != 0) {
			if ((mask & 0x1) != 0) {
				manager.sectionCount++;
			}

			mask >>>= 1;
		}

		manager.buffer.clearLayers();
		manager.moveToNextLayer();

		return manager;
	}

	public int getBitsPerBlock() {
		return this.bitsPerBlock;
	}

	public int getMaxBytesPerChunk() {
		return this.maxBytesPerChunk;
	}
}
