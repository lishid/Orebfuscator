/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.chunkmap;

import java.io.IOException;

public class ChunkWriter {
	private byte[] data;
	private int bitsPerBlock;
	private int byteIndex;
	private int bitIndex;
	private long buffer;

	public ChunkWriter(byte[] data) {
		this.data = data;
	}

	public int getByteIndex() {
		return this.byteIndex;
	}

	public void init() {
		this.byteIndex = 0;
		this.bitIndex = 0;
		this.buffer = 0;
	}

	public void setBitsPerBlock(int bitsPerBlock) {
		this.bitsPerBlock = bitsPerBlock;
	}

	public void save() throws IOException {
		this.writeLong();
	}

	public void skip(int count) {
		this.byteIndex += count;
		this.bitIndex = 0;
	}

	public void writeBytes(byte[] source, int index, int length) throws IOException {
		if (this.byteIndex + length > this.data.length) {
			throw new IOException("No space to write.");
		}

		System.arraycopy(source, index, this.data, this.byteIndex, length);

		this.byteIndex += length;
	}

	public void writeBlockBits(long bits) throws IOException {
		if (this.bitIndex >= 64) {
			this.writeLong();
			this.bitIndex = 0;
		}

		int leftBits = 64 - this.bitIndex;

		this.buffer |= bits << this.bitIndex;

		if (leftBits >= this.bitsPerBlock) {
			this.bitIndex += this.bitsPerBlock;
		} else {
			this.writeLong();

			this.buffer = bits >>> leftBits;

			this.bitIndex = this.bitsPerBlock - leftBits;
		}
	}

	private void writeLong() throws IOException {
		if (this.byteIndex + 7 >= this.data.length) {
			throw new IOException("No space to write.");
		}

		this.data[this.byteIndex++] = (byte) (this.buffer >> 56);
		this.data[this.byteIndex++] = (byte) (this.buffer >> 48);
		this.data[this.byteIndex++] = (byte) (this.buffer >> 40);
		this.data[this.byteIndex++] = (byte) (this.buffer >> 32);
		this.data[this.byteIndex++] = (byte) (this.buffer >> 24);
		this.data[this.byteIndex++] = (byte) (this.buffer >> 16);
		this.data[this.byteIndex++] = (byte) (this.buffer >> 8);
		this.data[this.byteIndex++] = (byte) this.buffer;

		this.buffer = 0;
	}

	public void writeVarInt(int value) throws IOException {
		while ((value & ~0x7F) != 0) {
			this.writeByte(value & 0x7F | 0x80);
			value >>>= 7;
		}

		this.writeByte(value);
	}

	public void writeByte(int value) throws IOException {
		if (this.byteIndex >= this.data.length) {
			throw new IOException("No space to write.");
		}

		this.data[this.byteIndex++] = (byte) value;
	}

	public void writeShort(int blockCount) throws IOException {
		if (this.byteIndex + 1 >= this.data.length) {
			throw new IOException("No space to write.");
		}

		this.data[this.byteIndex++] = (byte) (blockCount >> 8);
		this.data[this.byteIndex++] = (byte) (blockCount >> 0);
	}
}
