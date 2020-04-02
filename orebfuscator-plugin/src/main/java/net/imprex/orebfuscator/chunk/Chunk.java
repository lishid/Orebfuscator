package net.imprex.orebfuscator.chunk;

import java.util.Arrays;

import com.lishid.orebfuscator.chunkmap.ChunkData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

public class Chunk implements AutoCloseable {

	public static Chunk fromChunkData(ChunkData chunkData) {
		int sectionCount;
		for (sectionCount = 0; (chunkData.primaryBitMask & (1 << sectionCount)) != 0; sectionCount++) {}

		int extraBytes = ChunkCapabilities.hasLightArray ? 2048 : 0;
		if (chunkData.isOverworld) {
			extraBytes *= 2;
		}

		return new Chunk(sectionCount, extraBytes, chunkData.data);
	}

	private final int sectionCount;
	private final int extraBytes;

	private final ByteBuf inputBuffer;
	private final ByteBuf outputBuffer;

	private Chunk(int sectionCount, int extraBytes, byte[] data) {
		this.sectionCount = sectionCount;
		this.extraBytes = extraBytes;
		this.inputBuffer = Unpooled.wrappedBuffer(data);
		this.outputBuffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
	}

	public int getSectionCount() {
		return sectionCount;
	}

	public ChunkSection nextChunkSection() {
		ChunkSection chunkSection = new ChunkSection();
		chunkSection.read(new ChunkBuffer(inputBuffer));
		return chunkSection;
	}

	public void writeChunkSection(ChunkSection chunkSection) {
		chunkSection.write(new ChunkBuffer(this.outputBuffer));
		this.outputBuffer.writeBytes(this.inputBuffer, this.extraBytes);
	}

	public byte[] finalizeOutput() {
		this.outputBuffer.writeBytes(this.inputBuffer);
		return Arrays.copyOf(this.outputBuffer.array(), this.outputBuffer.readableBytes());
	}

	@Override
	public void close() throws Exception {
		this.inputBuffer.release();
		this.outputBuffer.release();
	}
}
