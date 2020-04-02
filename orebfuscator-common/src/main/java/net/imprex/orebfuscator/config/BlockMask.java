package net.imprex.orebfuscator.config;

public interface BlockMask {

	public static final int BLOCK_MASK_OBFUSCATE = 1;
	public static final int BLOCK_MASK_DARKNESS = 2;
	public static final int BLOCK_MASK_TILEENTITY = 4;
	public static final int BLOCK_MASK_PROXIMITY = 8;

	int mask(int blockId, int y);
}
