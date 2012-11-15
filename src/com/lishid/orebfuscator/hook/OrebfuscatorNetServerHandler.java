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

package com.lishid.orebfuscator.hook;

import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.threading.OrebfuscatorScheduler;
import com.lishid.orebfuscator.utils.ReflectionHelper;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet130UpdateSign;
import net.minecraft.server.Packet132TileEntityData;
import net.minecraft.server.Packet14BlockDig;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet53BlockChange;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraftserverhook.NetServerHandlerProxy;

public class OrebfuscatorNetServerHandler extends NetServerHandlerProxy
{
    public static byte[] buffer;
    public static int size;
    public static int initialized = 0;
    
    static
    {
        byte[] buffer2 = new byte[10240 + 256];
        java.util.Arrays.fill(buffer2, 0, buffer2.length, (byte) 0);
        
        Packet51MapChunk prechunk = new Packet51MapChunk();
        ReflectionHelper.setPrivateField(prechunk, "inflatedBuffer", buffer2);
        prechunk.compress();
        buffer = (byte[]) ReflectionHelper.getPrivateField(prechunk, "buffer");
        size = (int) (Integer) ReflectionHelper.getPrivateField(prechunk, "size");
    }
    
    public OrebfuscatorNetServerHandler(MinecraftServer minecraftserver, NetServerHandler instance)
    {
        super(minecraftserver, instance);
    }
    
    public static Packet51MapChunk preChunk(int x, int z, EntityPlayer player)
    {
        Packet51MapChunk prechunk = new Packet51MapChunk();
        
        prechunk.a = x;
        prechunk.b = z;
        prechunk.c = 1;
        
        ReflectionHelper.setPrivateField(prechunk, "e", true);
        ReflectionHelper.setPrivateField(prechunk, "buffer", buffer);
        ReflectionHelper.setPrivateField(prechunk, "size", size);
        
        return prechunk;
    }
    
    @Override
    public void sendPacket(Packet packet)
    {
        if (packet instanceof Packet51MapChunk)
        {
            
            Packet51MapChunk packet2 = (Packet51MapChunk) packet;
            if ((packet2.c == 0) && (packet2.d == 0)) 
            {
                super.sendPacket(packet);
            }
            else
            {
                // Send pre-chunk
                // networkManager.queue(preChunk(packet2.a, packet2.b, this.player));
                // Obfuscate packet
                OrebfuscatorScheduler.getScheduler().SyncThreads();
                OrebfuscatorScheduler.getScheduler().Queue((Packet51MapChunk) packet, this.getPlayer());
            }
            
        }
        else if (packet instanceof Packet56MapChunkBulk)
        {
            // Send pre-chunk
            Packet56MapChunkBulk packet2 = (Packet56MapChunkBulk) packet;
            int[] x = (int[]) ReflectionHelper.getPrivateField(packet2, "c");
            int[] z = (int[]) ReflectionHelper.getPrivateField(packet2, "d");
            
            for (int i = 0; i < x.length; i++)
            {
                networkManager.queue(preChunk(x[i], z[i], this.player));
            }
            
            // Obfuscate packet
            OrebfuscatorScheduler.getScheduler().SyncThreads();
            OrebfuscatorScheduler.getScheduler().Queue((Packet56MapChunkBulk) packet, this.getPlayer());
        }
        else
        {
            if (packet instanceof Packet130UpdateSign)
            {
                Packet130UpdateSign newPacket = (Packet130UpdateSign) packet;
                networkManager.queue(new Packet53BlockChange(newPacket.x, newPacket.y, newPacket.z, this.player.world));
            }
            else if (packet instanceof Packet132TileEntityData)
            {
                Packet132TileEntityData newPacket = (Packet132TileEntityData) packet;
                networkManager.queue(new Packet53BlockChange(newPacket.a, newPacket.b, newPacket.c, this.player.world));
            }
            
            super.sendPacket(packet);
        }
    }
    
    @Override
    public void a(Packet14BlockDig packet)
    {
        if (packet.e == 1 || packet.e == 3)
        {
            if (!BlockHitManager.canFakeHit(this.getPlayer()))
            {
                return;
            }
            
            // Anti-hack
            int i = packet.a;
            int j = packet.b;
            int k = packet.c;
            
            double d4 = this.getPlayer().getHandle().locX - ((double) i + 0.5D);
            double d5 = this.getPlayer().getHandle().locY - ((double) j + 0.5D);
            double d6 = this.getPlayer().getHandle().locZ - ((double) k + 0.5D);
            double d7 = d4 * d4 + d5 * d5 + d6 * d6;
            
            if (d7 >= 256.0D)
            {
                BlockHitManager.fakeHit(this.getPlayer());
                return;
            }
        }
        
        super.a(packet);
    }
}