package net.imprex.orebfuscator.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class ChunkBuffer {

	private final ByteBuf buffer;

	public ChunkBuffer() {
		this(PooledByteBufAllocator.DEFAULT.heapBuffer());
	}

	public ChunkBuffer(ByteBuf buffer) {
		this.buffer = buffer;
	}

	public int readVarInt() {
		int out = 0;
		int bytes = 0;
		byte in;
		do {
			in = this.buffer.readByte();
			out |= (in & 0x7F) << bytes++ * 7;
			if (bytes > 5) {
				throw new IndexOutOfBoundsException("varint32 too long");
			}
		} while ((in & 0x80) != 0);
		return out;
	}

	public void writeVarInt(int value) {
		while ((value & -0x80) != 0) {
			this.buffer.writeByte(value & 0x7F | 0x80);
			value >>>= 7;
		}
		this.buffer.writeByte(value);
	}

	public ByteBuf writeByte(int value) {
		return this.buffer.writeByte(value);
	}

	public byte readByte() {
		return this.buffer.readByte();
	}

	public int readUnsignedByte() {
		return this.buffer.readUnsignedByte();
	}

	public ByteBuf writeShort(int value) {
		return this.buffer.writeShort(value);
	}

	public short readShort() {
		return this.buffer.readShort();
	}

	public int readUnsignedShort() {
		return this.buffer.readUnsignedShort();
	}

	public ByteBuf writeLong(long value) {
		return this.buffer.writeLong(value);
	}

	public long readLong() {
		return this.buffer.readLong();
	}
}
