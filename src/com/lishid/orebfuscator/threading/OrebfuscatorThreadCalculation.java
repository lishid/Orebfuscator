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

package com.lishid.orebfuscator.threading;

import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.obfuscation.ChunkInfo;
import com.lishid.orebfuscator.threading.OrebfuscatorScheduler.QueuedPacket;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

public abstract class OrebfuscatorThreadCalculation extends Thread implements Runnable
{
    protected AtomicBoolean kill = new AtomicBoolean(false);
    protected byte[] chunkBuffer = new byte[65536];
 
    protected boolean queueForSending = true;
    
    public OrebfuscatorThreadCalculation(boolean queueForSending) {
		this.queueForSending = queueForSending;
	}

	public void kill() {
    	kill.set(true);
    }
    
    protected void processPacket(QueuedPacket packet) {
    	processPacket(packet.packet, packet.player);
    }
    
    public void processPacket(Packet packet, CraftPlayer player) {
        try
        {
            sendPacket(packet, player);
            
        	// Don't waste CPU if the player is gone
            if (player.getHandle().netServerHandler.disconnected)
            {
                return;
            }
            
            synchronized (Orebfuscator.players)
            {
                if (!Orebfuscator.players.containsKey(player))
                {
                	return;
                }
            }
            
            try
            {
                // Try to obfuscate and send the packet
                if (packet instanceof Packet56MapChunkBulk)
                {
                    Calculations.Obfuscate((Packet56MapChunkBulk) packet, player, queueForSending, chunkBuffer);
                }
                else if (packet instanceof Packet51MapChunk)
                {
                    Calculations.Obfuscate((Packet51MapChunk) packet, player, queueForSending, chunkBuffer);
                }
            }
            catch (Throwable e)
            {
                Orebfuscator.log(e);

                // If we run into problems, just send the packet.
                sendPacket(packet, player);
            }
            
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
    }
    
    protected void sendPacket(Packet packet, CraftPlayer player) {
        ChunkCompressionThread.Queue(player, packet, new ChunkInfo[0]);
    }

    protected void cleanup() {
    	OrebfuscatorScheduler.getScheduler().removeThread(this);
    }
}