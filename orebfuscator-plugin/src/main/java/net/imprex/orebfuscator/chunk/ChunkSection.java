package net.imprex.orebfuscator.chunk;

import net.imprex.orebfuscator.NmsInstance;

public class ChunkSection {

	private int blockCount;
	public int bitsPerBlock;

	public Palette palette;
	public VarBitBuffer data;

	public ChunkSection() {
		this.setBitsPerBlock(4);
	}

	public ChunkSection(int bitsPerBlock) {
		this.setBitsPerBlock(bitsPerBlock);
	}

	private void setBitsPerBlock(int bitsPerBlock) {
		if (this.bitsPerBlock != bitsPerBlock) {
			if (bitsPerBlock <= 8) {
				this.bitsPerBlock = Math.max(4, bitsPerBlock);
				this.palette = new IndirectPalette(this.bitsPerBlock, this);
			} else {
				this.bitsPerBlock = NmsInstance.get().getBitsPerBlock();
				this.palette = new DirectPalette();
			}

			this.palette.fromBlockId(0); // add air by default
			this.data = new VarBitBuffer(this.bitsPerBlock, 4096);
		}
	}

	int grow(int bitsPerBlock, int blockId) {
		Palette palette = this.palette;
		VarBitBuffer data = this.data;

		this.setBitsPerBlock(bitsPerBlock);

		for (int i = 0; i < data.size(); i++) {
			int preBlockId = palette.toBlockId(data.get(i));
			this.data.set(i, this.palette.fromBlockId(preBlockId));
		}

		return this.palette.fromBlockId(blockId);
	}

	private static int positionToIndex(int x, int y, int z) {
		return y << 8 | z << 4 | x;
	}

	public void setBlock(int x, int y, int z, int blockId) {
		this.setBlock(positionToIndex(x, y, z), blockId);
	}

	public void setBlock(int index, int blockId) {
		int prevBlockId = this.getBlock(index);

		if (!NmsInstance.get().isAir(prevBlockId)) {
			--this.blockCount;
		}

		if (!NmsInstance.get().isAir(blockId)) {
			++this.blockCount;
		}

		int paletteIndex = this.palette.fromBlockId(blockId);
		this.data.set(index, paletteIndex);
	}

	public int getBlock(int x, int y, int z) {
		return this.getBlock(positionToIndex(x, y, z));
	}

	public int getBlock(int index) {
		return this.palette.toBlockId(this.data.get(index));
	}

	public void write(ChunkBuffer chunkBuffer) {
		if (ChunkCapabilities.hasBlockCount) {
			chunkBuffer.writeShort(this.blockCount);
		}

		chunkBuffer.writeByte(this.bitsPerBlock);
		this.palette.write(chunkBuffer);

		long[] data = this.data.toArray();
		chunkBuffer.writeVarInt(data.length);
		for (long entry : data) {
			chunkBuffer.writeLong(entry);
		}
	}

	public void read(ChunkBuffer chunkBuffer) {
		if (ChunkCapabilities.hasBlockCount) {
			this.blockCount = chunkBuffer.readShort();
		}

		this.setBitsPerBlock(chunkBuffer.readUnsignedByte());

		this.palette.read(chunkBuffer);

		long[] data = this.data.toArray();
		if (data.length != chunkBuffer.readVarInt()) {
			throw new IndexOutOfBoundsException("data.length != VarBitBuffer::size");
		}

		for (int i = 0; i < data.length; i++) {
			data[i] = chunkBuffer.readLong();
		}
	}
}
