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

package com.lishid.orebfuscator.internal.craftbukkit;

import java.util.zip.Deflater;

import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.utils.ReflectionHelper;

//Volatile
import net.minecraft.server.*;

public class Packet51 implements IPacket51
{
    private static Class<? extends Object> packetClass = Packet51MapChunk.class;
    
    Packet51MapChunk packet;
    
    @Override
    public void setPacket(Object packet)
    {
        if (packet instanceof Packet51MapChunk)
        {
            this.packet = (Packet51MapChunk) packet;
        }
        else
        {
            InternalAccessor.Instance.PrintError();
        }
    }
    
    @Override
    public int getX()
    {
        return packet.a;
    }
    
    @Override
    public int getZ()
    {
        return packet.b;
    }
    
    @Override
    public int getChunkMask()
    {
        return packet.c;
    }
    
    @Override
    public int getExtraMask()
    {
        return packet.d;
    }
    
    @Override
    public byte[] getBuffer()
    {
        return (byte[]) ReflectionHelper.getPrivateField(packetClass, packet, "inflatedBuffer");
    }
    
    private byte[] getOutputBuffer()
    {
        return (byte[]) ReflectionHelper.getPrivateField(packetClass, packet, "buffer");
    }
    
    @Override
    public void compress(Deflater deflater)
    {
        byte[] chunkInflatedBuffer = getBuffer();
        byte[] chunkBuffer = getOutputBuffer();
        
        deflater.reset();
        deflater.setInput(chunkInflatedBuffer, 0, chunkInflatedBuffer.length);
        deflater.finish();
        ReflectionHelper.setPrivateField(packetClass, packet, "size", deflater.deflate(chunkBuffer));
    }
}
