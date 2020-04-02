package net.imprex.orebfuscator.chunk;

public class DirectPalette implements Palette {

	@Override
	public int fromBlockId(int blockId) {
		return blockId;
	}

	@Override
	public int toBlockId(int id) {
		return id;
	}

	@Override
	public void read(ChunkBuffer buffer) {
		if (ChunkCapabilities.hasDirectPaletteZeroLength) {
			buffer.readVarInt();
		}
	}

	@Override
	public void write(ChunkBuffer buffer) {
		if (ChunkCapabilities.hasDirectPaletteZeroLength) {
			buffer.writeVarInt(0);
		}
	}
}
