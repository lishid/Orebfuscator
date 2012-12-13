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

import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.utils.ReflectionHelper;

//Volatile
import net.minecraft.server.*;

public class Packet56 implements IPacket56
{
    Packet56MapChunkBulk packet;
    
    @Override
    public void setPacket(Object packet)
    {
        if (packet instanceof Packet56MapChunkBulk)
        {
            this.packet = (Packet56MapChunkBulk) packet;
        }
        else
        {
            InternalAccessor.Instance.PrintError();
        }
    }
    
    @Override
    public int getPacketChunkNumber()
    {
        return packet.d();
    }
    
    @Override
    public int[] getX()
    {
        return (int[]) ReflectionHelper.getPrivateField(packet, "c");
    }
    
    @Override
    public int[] getZ()
    {
        return (int[]) ReflectionHelper.getPrivateField(packet, "d");
    }
    
    @Override
    public int[] getChunkMask()
    {
        return packet.a;
    }
    
    @Override
    public int[] getExtraMask()
    {
        return packet.b;
    }
    
    @Override
    public byte[][] getInflatedBuffers()
    {
        return (byte[][]) ReflectionHelper.getPrivateField(packet, "inflatedBuffers");
    }
    
    @Override
    public byte[] getBuildBuffer()
    {
        return (byte[]) ReflectionHelper.getPrivateField(Packet56MapChunkBulk.class, packet, "buildBuffer");
    }
    
    @Override
    public byte[] getOutputBuffer()
    {
        return (byte[]) ReflectionHelper.getPrivateField(Packet56MapChunkBulk.class, packet, "buffer");
    }
    
    @Override
    public void compress(Deflater deflater)
    {
        if (getOutputBuffer() != null)
        {
            return;
        }
        
        byte[] buildBuffer = getBuildBuffer();
        
        deflater.reset();
        deflater.setInput(buildBuffer);
        deflater.finish();
        
        byte[] buffer = new byte[buildBuffer.length + 100];
        
        ReflectionHelper.setPrivateField(packet, "buffer", buffer);
        ReflectionHelper.setPrivateField(packet, "size", deflater.deflate(buffer));
    }
}
