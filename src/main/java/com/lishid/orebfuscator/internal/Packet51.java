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
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk.ChunkMap;

public class Packet51 {
    private static Class<PacketPlayOutMapChunk> packetClass = PacketPlayOutMapChunk.class;

    private PacketPlayOutMapChunk packet;
    private ChunkData chunkData;

    public void setPacket(Object packet) {
        this.packet = (PacketPlayOutMapChunk) packet;
        chunkData = new ChunkData((ChunkMap) getFieldData("c"), getX(), getZ());
    }

    private int getX() {
        return (Integer) getFieldData("a");
    }

    private int getZ() {
        return (Integer) getFieldData("b");
    }

    public ChunkData getChunkData() {
        return chunkData;
    }

    private Object getFieldData(String field) {
        return ReflectionHelper.getPrivateField(packetClass, packet, field);
    }
}
