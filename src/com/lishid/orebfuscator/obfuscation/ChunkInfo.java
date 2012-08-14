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

package com.lishid.orebfuscator.obfuscation;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import net.minecraft.server.WorldServer;

public class ChunkInfo
{
    public boolean useCache;
    public int chunkX;
    public int chunkZ;
    public int chunkMask;
    public int extraMask;
    public int chunkSectionNumber;
    public int extraSectionNumber;
    public boolean canUseCache;
    public int[] chunkSectionToIndexMap = new int[16];
    public int[] extraSectionToIndexMap = new int[16];
    public WorldServer world;
    public byte[] data;
    public byte[] buffer;
    public CraftPlayer player;
    public int startIndex;
    public int size;
    public int blockSize;
}
