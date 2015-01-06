/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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

import com.lishid.orebfuscator.internal.ChunkData;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ChunkInfo {
    public Player player;
    public World world;

    public int chunkX;
    public int chunkZ;
    public int chunkMask;
    public byte[] original;

    public byte[] buffer;
    public boolean useCache;

    public int bytes;
    public int sections;
    public int[] sectionIndices;

    public ChunkInfo(Player player, ChunkData chunkData, byte[] buffer) {
        this.player = player;
        this.world = player.getWorld();

        this.chunkX = chunkData.x;
        this.chunkZ = chunkData.z;
        this.chunkMask = chunkData.mask;
        this.original = chunkData.buffer;

        this.buffer = buffer;
        this.sectionIndices = new int[16];
        this.sections = 0;

        int sectionIndex = 0;
        for (int i = 0; i < 16; i++) {
            this.sectionIndices[i] = -1;
            if ((chunkMask & 1 << i) != 0) {
                this.sections++;
                this.sectionIndices[i] = sectionIndex;
                sectionIndex++;
            }
        }
        this.bytes = sections * Calculations.BYTES_PER_SECTION;
    }
}
