package net.imprex.orebfuscator.chunk;

public class ChunkCapabilities {

	static boolean hasLightArray = false;
	static boolean hasBlockCount = false;
	static boolean hasDirectPaletteZeroLength = false;

	public static void hasLightArray() {
		hasLightArray = true;
	}

	public static void hasBlockCount() {
		hasBlockCount = true;
	}

	public static void hasDirectPaletteZeroLength() {
		hasDirectPaletteZeroLength = true;
	}
}
