package com.lishid.orebfuscator.api.chunk;

import java.io.IOException;

public interface IChunkWriter {

	public int getByteIndex();

	public void init();

	public void setBitsPerBlock(int bitsPerBlock);

	public void save() throws IOException;

	public void skip(int count);

	public void writeBytes(byte[] source, int index, int length) throws IOException;

	public void writeBlockBits(long bits) throws IOException;

	public void writeVarInt(int value) throws IOException;

	public void writeByte(int value) throws IOException;

	public void writeShort(int blockCount) throws IOException;
}