/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package lishid.orebfuscator.chunkscrambler;

import java.lang.reflect.Field;
import java.util.Random;

import lishid.orebfuscator.utils.OrebfuscatorConfig;

import net.minecraft.server.*;

public class ScrambledBiomeDecorator extends BiomeDecorator{

	public ScrambledBiomeDecorator(BiomeBase biome, BiomeDecorator old)
	{
		super(biome);
		this.y = getInt("y", old, y);
		this.z = getInt("z", old, z);
		this.A = getInt("A", old, A);
		this.B = getInt("B", old, B);
		this.C = getInt("C", old, C);
		this.D = getInt("D", old, D);
		this.E = getInt("E", old, E);
		this.F = getInt("F", old, F);
		this.G = getInt("G", old, G);
		this.H = getInt("H", old, H);
		this.I = getInt("I", old, I);
		this.J = getInt("J", old, J);
		this.K = old.K;
	}
	
	private int getInt(String field, BiomeDecorator old, int def)
	{
		try {
			Field f = BiomeDecorator.class.getDeclaredField(field);
	    	f.setAccessible(true);
	    	return f.getInt(old);
		} catch (Exception e) { e.printStackTrace(); }
		return def;
	}
	
	@Override
	public void a(World world, Random random, int x, int z)
	{
		long newSeed = (OrebfuscatorConfig.getSeed() / 2) + (random.nextLong() / 2);
		super.a(world, new Random(newSeed), x, z);
	}
}
