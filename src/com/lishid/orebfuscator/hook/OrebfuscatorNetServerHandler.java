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

import com.lishid.orebfuscator.threading.OrebfuscatorScheduler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
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
        if (packet instanceof Packet51MapChunk)
        {
            // Obfuscate packet
            OrebfuscatorScheduler.getScheduler().SyncThreads();
            OrebfuscatorScheduler.getScheduler().Queue((Packet51MapChunk) packet, this.getPlayer());
        }
        else if (packet instanceof Packet56MapChunkBulk)
        {
            // Obfuscate packet
            OrebfuscatorScheduler.getScheduler().SyncThreads();
            OrebfuscatorScheduler.getScheduler().Queue((Packet56MapChunkBulk) packet, this.getPlayer());
        }
        else
        {
            super.sendPacket(packet);
        }
    }
    
    @Override
    public void a(Packet14BlockDig packet)
    {
        //Anti-hack
        if (packet.e == 1 && packet.e == 3)
        {
            int i = packet.a;
            int j = packet.b;
            int k = packet.c;
            
            double d4 = this.player.locX - ((double) i + 0.5D);
            double d5 = this.player.locY - ((double) j + 0.5D);
            double d6 = this.player.locZ - ((double) k + 0.5D);
            double d7 = d4 * d4 + d5 * d5 + d6 * d6;
            
            if (d7 >= 64.0D)
            {
                return;
            }
        }
        
        super.sendPacket(packet);
    }
}