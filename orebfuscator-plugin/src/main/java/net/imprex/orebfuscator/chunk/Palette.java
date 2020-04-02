package net.imprex.orebfuscator.chunk;

public interface Palette {

	int fromBlockId(int blockId);

	int toBlockId(int id);

	void read(ChunkBuffer buffer);

	void write(ChunkBuffer buffer);

}
