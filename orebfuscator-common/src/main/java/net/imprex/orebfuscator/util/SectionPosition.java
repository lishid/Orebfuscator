package net.imprex.orebfuscator.util;

import org.bukkit.World;

public class SectionPosition {

	public SectionPosition from(ChunkPosition chunkPosition, int sectionY) {
		return new SectionPosition(chunkPosition.getX(), sectionY, chunkPosition.getZ());
	}

	public SectionPosition from(BlockPos blockPos) {
		return new SectionPosition(blockToSectionCoord(blockPos.x), blockToSectionCoord(blockPos.y), blockToSectionCoord(blockPos.z));
	}

	public static int blockToSectionCoord(int block) {
		return block >> 4;
	}

	public static int sectionToBlockCoord(int section) {
		return section << 4;
	}

	public static int sectionToBlockCoord(int section, int relative) {
		return sectionToBlockCoord(section) + relative;
	}

	public static int sectionRelative(int block) {
		return block & 0xF;
	}

	public final int x;
	public final int y;
	public final int z;

	private SectionPosition(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public ChunkPosition toChunkPosition(World world) {
		return new ChunkPosition(world, this.x, this.z);
	}

	public BlockPos toBlockPos() {
		return this.toBlockPos(0, 0, 0);
	}

	public BlockPos toBlockPos(int x, int y, int z) {
		return new BlockPos(
			sectionToBlockCoord(this.x, x),
			sectionToBlockCoord(this.y, y),
			sectionToBlockCoord(this.z, z)
		);
	}
}
