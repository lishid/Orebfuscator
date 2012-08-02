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

package lishid.orebfuscator.threading;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.OrebfuscatorConfig;
import lishid.orebfuscator.obfuscation.Calculations;

import org.bukkit.craftbukkit.ChunkCompressionThread;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

public class OrebfuscatorThreadCalculation extends Thread implements Runnable
{
    private static final int QUEUE_CAPACITY = 1024 * 10;
    private static ArrayList<OrebfuscatorThreadCalculation> threads = new ArrayList<OrebfuscatorThreadCalculation>();
    private static final LinkedBlockingDeque<QueuedPacket> queue = new LinkedBlockingDeque<QueuedPacket>(QUEUE_CAPACITY);
    
    public static int getThreads()
    {
        return threads.size();
    }
    
    public static void terminateAll()
    {
        for (int i = 0; i < threads.size(); i++)
        {
            threads.get(i).kill.set(true);
        }
    }
    
    public static synchronized void SyncThreads()
    {
        int extra = threads.size() - OrebfuscatorConfig.getProcessingThreads();
        
        if (extra > 0)
        {
            for (int i = extra - 1; i >= 0; i--)
            {
                threads.get(i).kill.set(true);
                threads.remove(i);
            }
        }
        else if (extra < 0)
        {
            extra = -extra;
            for (int i = 0; i < extra; i++)
            {
                OrebfuscatorThreadCalculation thread = new OrebfuscatorThreadCalculation();
                thread.setName("Orebfuscator Calculation Thread");
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
                threads.add(thread);
            }
        }
    }
    
    public static void Queue(Packet56MapChunkBulk packet, CraftPlayer player)
    {
        while (true)
        {
            try
            {
                queue.put(new QueuedPacket(player, packet));
                return;
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    public static void Queue(Packet51MapChunk packet, CraftPlayer player)
    {
        while (true)
        {
            try
            {
                if (Math.abs(packet.a - player.getLocation().getChunk().getX()) <= 1 && Math.abs(packet.b - player.getLocation().getChunk().getZ()) <= 1)
                {
                    queue.putFirst(new QueuedPacket(player, packet));
                }
                else
                {
                    queue.put(new QueuedPacket(player, packet));
                }
                return;
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    private AtomicBoolean kill = new AtomicBoolean(false);
    private byte[] chunkBuffer = new byte[65536];
    
    public void run()
    {
        while (!this.isInterrupted() && !kill.get())
        {
            try
            {
                // Take a package from the queue
                QueuedPacket packet = queue.take();
                
                // Don't waste time if the player is gone
                synchronized (Orebfuscator.players)
                {
                    if (!Orebfuscator.players.containsKey(packet.player))
                    {
                        continue;
                    }
                }
                
                try
                {
                    // Try to obfuscate and send the packet
                    if (packet.packet instanceof Packet56MapChunkBulk)
                    {
                        Calculations.Obfuscate((Packet56MapChunkBulk) packet.packet, packet.player, true, chunkBuffer);
                    }
                    else if (packet.packet instanceof Packet51MapChunk)
                    {
                        Calculations.Obfuscate((Packet51MapChunk) packet.packet, packet.player, true, chunkBuffer);
                    }
                }
                catch (Throwable e)
                {
                    Orebfuscator.log(e);
                    // If we run into problems, just send the packet.
                    ChunkCompressionThread.sendPacket(packet.player.getHandle(), packet.packet);
                }
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
        
        threads.remove(this);
    }
    
    private static class QueuedPacket
    {
        final CraftPlayer player;
        final Packet packet;
        
        QueuedPacket(CraftPlayer player, Packet packet)
        {
            this.player = player;
            this.packet = packet;
        }
    }
}