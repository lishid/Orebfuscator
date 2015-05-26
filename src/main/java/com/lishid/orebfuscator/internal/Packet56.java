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

package com.lishid.orebfuscator.internal;

import com.lishid.orebfuscator.utils.ReflectionHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk.ChunkMap;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunkBulk;

//Volatile

public class Packet56 {
    private PacketPlayOutMapChunkBulk packet;
    private ChunkData[] chunkData;

    public void setPacket(Object packet) {
        this.packet = (PacketPlayOutMapChunkBulk) packet;
        int[] x = getX();
        int[] z = getZ();
        ChunkMap[] chunkMaps = getChunkMaps();
        chunkData = new ChunkData[x.length];
        for (int i = 0; i < x.length; i++) {
            chunkData[i] = new ChunkData(chunkMaps[i], x[i], z[i]);
        }
    }

    private int[] getX() {
        return (int[]) getFieldData("a");
    }

    private int[] getZ() {
        return (int[]) getFieldData("b");
    }

    private ChunkMap[] getChunkMaps() {
        return (ChunkMap[]) getFieldData("c");
    }

    public ChunkData[] getChunkData() {
        return chunkData;
    }

    private Object getFieldData(String field) {
        return ReflectionHelper.getPrivateField(PacketPlayOutMapChunkBulk.class, packet, field);
    }
}
