/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.chunkmap;

import java.io.IOException;

public class ChunkReader {

	private byte[] data;
	private int bitsPerBlock;
	private long maxValueMask;
	private int byteIndex;
	private int bitIndex;
	private long buffer;

	public ChunkReader(byte[] data) {
		this.data = data;
		this.byteIndex = 0;
		this.bitIndex = 0;
	}

	public int getByteIndex() {
		return this.byteIndex;
	}

	public void skip(int count) {
		this.byteIndex += count;
		this.bitIndex = 0;
	}

	public void setBitsPerBlock(int bitsPerBlock) {
		this.bitsPerBlock = bitsPerBlock;
		this.maxValueMask = (1L << this.bitsPerBlock) - 1;
	}

	public int readBlockBits() throws IOException {
		if (this.bitIndex == 0 || this.bitIndex >= 64) {
			readLong();
			this.bitIndex = 0;
		}

		int leftBits = 64 - this.bitIndex;
		long result = this.buffer >>> this.bitIndex;

		if (leftBits >= this.bitsPerBlock) {
			this.bitIndex += this.bitsPerBlock;
		} else {
			readLong();

			result |= this.buffer << leftBits;

			this.bitIndex = this.bitsPerBlock - leftBits;
		}

		return (int) (result & this.maxValueMask);
	}

	private void readLong() throws IOException {
		if (this.byteIndex + 7 >= this.data.length) {
			throw new IOException("No data to read. byteIndex = " + this.byteIndex);
		}

		this.buffer =
				((this.data[this.byteIndex] & 0xffL) << 56)
				| ((this.data[this.byteIndex + 1] & 0xffL) << 48)
				| ((this.data[this.byteIndex + 2] & 0xffL) << 40)
				| ((this.data[this.byteIndex + 3] & 0xffL) << 32)
				| ((this.data[this.byteIndex + 4] & 0xffL) << 24)
				| ((this.data[this.byteIndex + 5] & 0xffL) << 16)
				| ((this.data[this.byteIndex + 6] & 0xffL) << 8)
				| (this.data[this.byteIndex + 7] & 0xffL);

		this.byteIndex += 8;
	}

	public int readVarInt() throws IOException {
		int value = 0;
		int size = 0;
		int b;

		while (((b = readByte()) & 0x80) == 0x80) {
			value |= (b & 0x7F) << (size++ * 7);

			if (size > 5) {
				throw new IOException("Invalid VarInt. byteIndex = " + this.byteIndex + ", value = " + value + ", size = " + size);
			}
		}

		return value | ((b & 0x7F) << (size * 7));
	}

	public int readByte() throws IOException {
		if (this.byteIndex >= this.data.length) {
			throw new IOException("No data to read. byteIndex = " + this.byteIndex);
		}

		return this.data[this.byteIndex++] & 0xff;
	}

    public int readShort() throws IOException {
        int b1 = readByte();
        int b2 = readByte();
        
        return (b1 << 8) | b2;
    }
}
