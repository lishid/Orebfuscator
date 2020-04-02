package net.imprex.orebfuscator.chunk;

import net.imprex.orebfuscator.util.ObjectIdMap;

public class IndirectPalette implements Palette {

	private final ObjectIdMap<Integer> palette;
	private final ChunkSection chunkSection;
	private final int bitsPerBlock;

	public IndirectPalette(int bitsPerBlock, ChunkSection chunkSection) {
		this.bitsPerBlock = bitsPerBlock;
		this.chunkSection = chunkSection;

		this.palette = new ObjectIdMap<Integer>(1 << bitsPerBlock);
	}

	@Override
	public int fromBlockId(int block) {
		Integer blockId = Integer.valueOf(block);

		int index = this.palette.getKey(blockId);
		if (index == -1) {
			index = this.palette.add(blockId);

			if (index >= 1 << this.bitsPerBlock) {
				index = this.chunkSection.grow(this.bitsPerBlock + 1, block);
			}
		}

		return index;
	}

	@Override
	public int toBlockId(int index) {
		return this.palette.getValue(index);
	}

	@Override
	public void read(ChunkBuffer buffer) {
		this.palette.clear();

		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			this.palette.add(Integer.valueOf(buffer.readVarInt()));
		}
	}

	@Override
	public void write(ChunkBuffer buffer) {
		int size = this.palette.size();
		buffer.writeVarInt(size);

		for (int i = 0; i < size; i++) {
			buffer.writeVarInt(this.palette.getValue(i));
		}
	}

	@Override
	public String toString() {
		return this.palette.toString();
	}
}
