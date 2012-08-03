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

import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.getspout.spout.packet.standard.MCCraftPacket;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.packet.listener.PacketListener;
import org.getspout.spoutapi.packet.standard.MCPacket;

import com.lishid.orebfuscator.threading.OrebfuscatorThreadCalculation;

public class SpoutLoader
{
    public static void InitializeSpout()
    {
        // Add spout listener
        PacketListener listener = new PacketListener()
        {
            // Processing a chunk packet
            public boolean checkPacket(Player player, MCPacket mcpacket)
            {
                if ((player == null) || (mcpacket == null) || (player.getWorld() == null))
                    return true;
                
                // Process the chunk
                if (((MCCraftPacket) mcpacket).getPacket() instanceof Packet51MapChunk)
                {
                    // Obfuscate packet
                    OrebfuscatorThreadCalculation.SyncThreads();
                    OrebfuscatorThreadCalculation.Queue((Packet51MapChunk) ((MCCraftPacket) mcpacket).getPacket(), (CraftPlayer) player);
                    return false;
                }
                if (((MCCraftPacket) mcpacket).getPacket() instanceof Packet56MapChunkBulk)
                {
                    // Obfuscate packet
                    OrebfuscatorThreadCalculation.SyncThreads();
                    OrebfuscatorThreadCalculation.Queue((Packet56MapChunkBulk) ((MCCraftPacket) mcpacket).getPacket(), (CraftPlayer) player);
                    return false;
                }
                return true;
            }
        };
        SpoutManager.getPacketManager().addListener(51, listener);
    }
    
    
}
