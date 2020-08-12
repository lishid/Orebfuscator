package net.imprex.orebfuscator.config;

public interface BlockMask {

	public static final int BLOCK_MASK_OBFUSCATE = 1;
	public static final int BLOCK_MASK_TILEENTITY = 2;
	public static final int BLOCK_MASK_PROXIMITY = 4;

	int mask(int blockId);

	int mask(int blockId, int y);
}
