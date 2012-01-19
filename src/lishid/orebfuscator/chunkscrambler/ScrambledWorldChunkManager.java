package lishid.orebfuscator.chunkscrambler;

import net.minecraft.server.*;

public class ScrambledWorldChunkManager extends WorldChunkManager {
	public WorldChunkManager instance;
	public ScrambledWorldChunkManager(World world)
	{
		super(world);
	}
	
	@Override
	public BiomeBase getBiome(int x, int z)
	{
		BiomeBase biome = super.getBiome(x, z);
		biome.G = new ScrambledBiomeDecorator(biome, biome.G);
		return biome;
	}
}
