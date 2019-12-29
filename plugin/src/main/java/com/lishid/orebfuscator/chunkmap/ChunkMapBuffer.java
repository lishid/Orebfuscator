/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.chunkmap;

import com.lishid.orebfuscator.api.chunk.ChunkLayer;
import com.lishid.orebfuscator.api.chunk.IChunkMapBuffer;

public class ChunkMapBuffer extends IChunkMapBuffer {

	public ChunkMapBuffer(int maxBytesPerChunk) {
		this.palette = new int[256];
		this.output = new byte[maxBytesPerChunk];
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

	@Override
	public void clearLayers() {
		this.prevLayer.hasData = false;
		this.curLayer.hasData = false;
		this.nextLayer.hasData = false;
	}
}