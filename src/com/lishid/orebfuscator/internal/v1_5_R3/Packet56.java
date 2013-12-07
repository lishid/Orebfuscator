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

package com.lishid.orebfuscator.internal.v1_5_R3;

import java.util.zip.Deflater;

import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.utils.ReflectionHelper;

//Volatile
import net.minecraft.server.v1_5_R3.*;

public class Packet56 implements IPacket56 {
    Packet56MapChunkBulk packet;

    @Override
    public void setPacket(Object packet) {
        if (packet instanceof Packet56MapChunkBulk) {
            this.packet = (Packet56MapChunkBulk) packet;
        }
        else {
            InternalAccessor.Instance.PrintError();
        }
    }

    @Override
    public int getPacketChunkNumber() {
        return packet.d();
    }

    @Override
    public int[] getX() {
        return (int[]) ReflectionHelper.getPrivateField(packet, "c");
    }

    @Override
    public int[] getZ() {
        return (int[]) ReflectionHelper.getPrivateField(packet, "d");
    }

    @Override
    public int[] getChunkMask() {
        return packet.a;
    }

    @Override
    public int[] getExtraMask() {
        return packet.b;
    }

    @Override
    public Object getFieldData(String field) {
        return ReflectionHelper.getPrivateField(Packet56MapChunkBulk.class, packet, field);
    }

    @Override
    public void setFieldData(String field, Object data) {
        ReflectionHelper.setPrivateField(Packet56MapChunkBulk.class, packet, field, data);
    }

    public String getInflatedBuffers() {
        return "inflatedBuffers";
    }

    public String getBuildBuffer() {
        return "buildBuffer";
    }

    public String getOutputBuffer() {
        return "buffer";
    }

    @Override
    public void compress(Deflater deflater) {
        if (getFieldData(getOutputBuffer()) != null) {
            return;
        }

        byte[] buildBuffer = (byte[]) getFieldData(getBuildBuffer());

        deflater.reset();
        deflater.setInput(buildBuffer);
        deflater.finish();

        byte[] buffer = new byte[buildBuffer.length + 100];

        ReflectionHelper.setPrivateField(packet, "buffer", buffer);
        int size = deflater.deflate(buffer);
        ReflectionHelper.setPrivateField(packet, "size", size);

        // Free memory
        ReflectionHelper.setPrivateField(packet, "buildBuffer", null);
        ReflectionHelper.setPrivateField(packet, "inflatedBuffers", null);

        if (OrebfuscatorCommandExecutor.DebugMode) {
            System.out.println("Packet size: " + size);
        }
    }
}
