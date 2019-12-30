package com.lishid.orebfuscator.api.chunk;

import java.io.IOException;

public interface IChunkReader {

	public int getByteIndex();

	public void skip(int count);

	public void setBitsPerBlock(int bitsPerBlock);

	public int readBlockBits() throws IOException;

	public int readVarInt() throws IOException;

	public int readByte() throws IOException;

	public int readShort() throws IOException;
}