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

package com.lishid.orebfuscator.internal.v1_7_R1;

import java.util.zip.Deflater;

import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.utils.ReflectionHelper;

//Volatile
import net.minecraft.server.v1_7_R1.*;

public class Packet51 implements IPacket51 {
    private static Class<? extends Object> packetClass = PacketPlayOutMapChunk.class;

    PacketPlayOutMapChunk packet;

    @Override
    public void setPacket(Object packet) {
        if (packet instanceof PacketPlayOutMapChunk) {
            this.packet = (PacketPlayOutMapChunk) packet;
        }
        else {
            InternalAccessor.Instance.PrintError();
        }
    }

    @Override
    public int getX() {
        return (Integer) ReflectionHelper.getPrivateField(packetClass, packet, "a");
    }

    @Override
    public int getZ() {
        return (Integer) ReflectionHelper.getPrivateField(packetClass, packet, "b");
    }

    @Override
    public int getChunkMask() {
        return (Integer) ReflectionHelper.getPrivateField(packetClass, packet, "c");
    }

    @Override
    public int getExtraMask() {
        return (Integer) ReflectionHelper.getPrivateField(packetClass, packet, "d");
    }

    @Override
    public byte[] getBuffer() {
        Object e = ReflectionHelper.getPrivateField(packetClass, packet, "e");
        if (e instanceof byte[]) {
            return (byte[]) e;
        }
        return (byte[]) ReflectionHelper.getPrivateField(packetClass, packet, "inflatedBuffer");
    }

    private byte[] getOutputBuffer() {
        return (byte[]) ReflectionHelper.getPrivateField(packetClass, packet, "buffer");
    }

    @Override
    public void compress(Deflater deflater) {
        byte[] chunkInflatedBuffer = getBuffer();
        byte[] chunkBuffer = getOutputBuffer();

        deflater.reset();
        deflater.setInput(chunkInflatedBuffer, 0, chunkInflatedBuffer.length);
        deflater.finish();
        ReflectionHelper.setPrivateField(packetClass, packet, "size", deflater.deflate(chunkBuffer));
    }
}
