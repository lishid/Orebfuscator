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
import com.lishid.orebfuscator.threading.ChunkCompressionThread;
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
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraftserverhook.NetServerHandlerProxy;

public class OrebfuscatorNetServerHandler extends NetServerHandlerProxy
{
    public OrebfuscatorNetServerHandler(MinecraftServer minecraftserver, NetServerHandler instance)
    {
        super(minecraftserver, instance);
    }
    
    @Override
    public void sendPacket(Packet packet)
    {
        EntityPlayer player = this.getPlayer().getHandle();
        
        if (packet instanceof Packet51MapChunk)
        {
            Packet51MapChunk packet2 = (Packet51MapChunk) packet;
            
            // If empty chunk, then send it out directly
            if ((packet2.c == 0) && (packet2.d == 0))
            {
                super.sendPacket(packet);
            }
            else
            {
                // Tell the Chunk Manager that we're processing that chunk, so that we can delay any TileEntity updates
                ChunkCompressionThread.QueueChunkProcessing(player, player.world.getChunkAt(packet2.a, packet2.b));
                // Synchronize threading
                OrebfuscatorScheduler.getScheduler().SyncThreads();
                // Obfuscate packet
                OrebfuscatorScheduler.getScheduler().Queue((Packet51MapChunk) packet, this.getPlayer());
            }
            
        }
        else if (packet instanceof Packet56MapChunkBulk)
        {
            Packet56MapChunkBulk packet2 = (Packet56MapChunkBulk) packet;
            
            // Get X and Z coords of chunks within the packet
            int[] x = (int[]) ReflectionHelper.getPrivateField(packet2, "c");
            int[] z = (int[]) ReflectionHelper.getPrivateField(packet2, "d");
            
            for (int i = 0; i < x.length; i++)
            {
                // Tell the Chunk Manager that we're processing that chunk, so that we can delay any TileEntity updates
                ChunkCompressionThread.QueueChunkProcessing(player, player.world.getChunkAt(x[i], z[i]));
            }
            
            // Synchronize threading
            OrebfuscatorScheduler.getScheduler().SyncThreads();
            // Obfuscate packet
            OrebfuscatorScheduler.getScheduler().Queue((Packet56MapChunkBulk) packet, this.getPlayer());
        }
        else
        {
            // Intercept all TileEntity updates
            if (packet instanceof Packet130UpdateSign)
            {
                Packet130UpdateSign newPacket = (Packet130UpdateSign) packet;
                
                // Check if the chunk is being processed. If so, the packet is saved to be sent later.
                if (!ChunkCompressionThread.CheckChunkPacket(player, player.world.getChunkAt(newPacket.x >> 4, newPacket.z >> 4), newPacket))
                {
                    return;
                }
            }
            else if (packet instanceof Packet132TileEntityData)
            {
                Packet132TileEntityData newPacket = (Packet132TileEntityData) packet;
                
                // Check if the chunk is being processed. If so, the packet is saved to be sent later.
                if (!ChunkCompressionThread.CheckChunkPacket(player, player.world.getChunkAt(newPacket.a >> 4, newPacket.b >> 4), newPacket))
                {
                    return;
                }
            }
            
            // Send the packet if it does not need processing
            super.sendPacket(packet);
        }
    }
    
    // This is to fix a bukkit security hole causing raw block IDs being sent to clients
    @Override
    public void a(Packet14BlockDig packet)
    {
        if (packet.e == 1 || packet.e == 3)
        {
            if (!BlockHitManager.canFakeHit(this.getPlayer()))
            {
                return;
            }
            
            // Find distance to player
            int i = packet.a;
            int j = packet.b;
            int k = packet.c;
            
            double d4 = this.getPlayer().getHandle().locX - ((double) i + 0.5D);
            double d5 = this.getPlayer().getHandle().locY - ((double) j + 0.5D);
            double d6 = this.getPlayer().getHandle().locZ - ((double) k + 0.5D);
            double d7 = d4 * d4 + d5 * d5 + d6 * d6;
            
            if (d7 >= 256.0D)
            {
                // Anti-hack
                BlockHitManager.fakeHit(this.getPlayer());
                return;
            }
        }
        
        super.a(packet);
    }
}