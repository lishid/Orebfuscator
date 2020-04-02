package net.imprex.orebfuscator.obfuscation;

import java.text.DecimalFormat;
import java.util.LinkedList;

import org.bukkit.World;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.chunkmap.ChunkData;
import com.lishid.orebfuscator.obfuscation.CalculationsUtil;

import net.imprex.orebfuscator.cache.ChunkCache;
import net.imprex.orebfuscator.cache.ChunkCacheEntry;
import net.imprex.orebfuscator.chunk.Chunk;
import net.imprex.orebfuscator.chunk.ChunkSection;
import net.imprex.orebfuscator.config.OrebfuscatorConfig;
import net.imprex.orebfuscator.config.ProximityConfig;
import net.imprex.orebfuscator.config.WorldConfig;
import net.imprex.orebfuscator.util.ChunkPosition;

public class Obfuscator {

	private final OrebfuscatorConfig config;
	private final ChunkCache chunkCache;

	public Obfuscator(Orebfuscator orebfuscator) {
		this.config = orebfuscator.getOrebfuscatorConfig();
		this.chunkCache = orebfuscator.getChunkCache();
	}

	private LinkedList<Long> avgTimes = new LinkedList<>();
	private double calls = 0;
	private DecimalFormat formatter = new DecimalFormat("###,###,###,###.00");

	public ChunkCacheEntry obfuscateOrUseCache(World world, ChunkData chunkData) {
		long time = System.nanoTime();
		try {
			return obfuscateOrUseCache0(world, chunkData);
		} finally {
			long diff = System.nanoTime() - time;

			avgTimes.add(diff);
			if (avgTimes.size() > 1000) {
				avgTimes.removeFirst();
			}

			if (calls++ % 100 == 0) {
				System.out.println("avg: "
						+ formatter.format(
								((double) avgTimes.stream().reduce(0L, Long::sum) / (double) avgTimes.size()) / 1000D)
						+ "μs");
			}
		}
	}

	public ChunkCacheEntry obfuscateOrUseCache0(World world, ChunkData chunkData) {
		if (chunkData.primaryBitMask == 0) {
			return null;
		}

		final ChunkPosition position = new ChunkPosition(world.getName(), chunkData.chunkX, chunkData.chunkZ);
		final long hash = CalculationsUtil.Hash(chunkData.data, chunkData.data.length, this.config.hash());

		if (this.config.cache().enabled()) {
			return this.chunkCache.get(position, hash, key -> this.obfuscate(hash, world, chunkData));
		} else {
			return this.obfuscate(hash, world, chunkData);
		}
	}

	private ChunkCacheEntry obfuscate(long hash, World world, ChunkData chunkData) {
		WorldConfig worldConfig = this.config.world(world);
		ProximityConfig proximityConfig = this.config.proximity(world);

		int baseX = chunkData.chunkX << 4;
		int baseZ = chunkData.chunkZ << 4;

		try (Chunk chunk = Chunk.fromChunkData(chunkData)) {
			for (int sectionIndex = 0; sectionIndex < chunk.getSectionCount(); sectionIndex++) {
				ChunkSection chunkSection = chunk.nextChunkSection();
				ChunkSection obfuscatedChunkSection = new ChunkSection();

				final int baseY = sectionIndex * 16;
				for (int index = 0; index < 4096; index++) {
					int blockData = chunkSection.getBlock(index);

					int x = baseX + (index & 15);
					int y = baseY + (index >> 8 & 15);
					int z = baseZ + (index >> 4 & 15);

					int obfuscateBits = worldConfig.blockmask(blockData);
					boolean obfuscateFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_OBFUSCATE) != 0;
					boolean darknessBlockFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_DARKNESS) != 0;
					boolean tileEntityFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_TILEENTITY) != 0;
					boolean proximityHiderFlag = proximityConfig != null && proximityConfig.shouldHide(y, blockData);

					if (obfuscateFlag) {
						blockData = worldConfig.randomBlockId();
					} else if (proximityHiderFlag) {
						blockData = proximityConfig.randomBlockId();
					}

					obfuscatedChunkSection.setBlock(index, blockData);
				}

				chunk.writeChunkSection(obfuscatedChunkSection);
			}

			byte[] data = chunk.finalizeOutput();

			return new ChunkCacheEntry(hash, data);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
