package lishid.orebfuscator.utils;

import net.minecraft.server.WorldServer;

public class ChunkInfo {
	int totalSize;
	int sizeX;
	int sizeY;
	int sizeZ;
	int startX;
	int startY;
	int startZ;
	WorldServer world;
	byte[] data;
}
