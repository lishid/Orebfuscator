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

import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.obfuscation.ChunkInfo;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

public class OrebfuscatorThreadCalculation extends Thread implements Runnable
{
    private static final int QUEUE_CAPACITY = 1024 * 10;
    private static ArrayList<ProcessingQueue> queues = new ArrayList<ProcessingQueue>();
    
    public static int getThreads()
    {
        return queues.size();
    }
    
    public static void terminateAll()
    {
        for (int i = 0; i < queues.size(); i++)
        {
            queues.get(i).thread.kill.set(true);
        }
    }
    
    public static synchronized void SyncThreads()
    {
        int extra = queues.size() - OrebfuscatorConfig.getProcessingThreads();
        
        if (extra > 0)
        {
            for (int i = extra - 1; i >= 0; i--)
            {
                queues.get(i).thread.kill.set(true);
                queues.get(0).thread.queue.addAll(queues.get(i).thread.queue);
                queues.remove(i);
            }
        }
        else if (extra < 0)
        {
            extra = -extra;
            for (int i = 0; i < extra; i++)
            {
                OrebfuscatorThreadCalculation thread = new OrebfuscatorThreadCalculation();
                thread.setName("Orebfuscator Calculation Thread");
                
                if (OrebfuscatorConfig.getOrebfuscatorPriority() == 0)
                    thread.setPriority(Thread.MIN_PRIORITY);
                if (OrebfuscatorConfig.getOrebfuscatorPriority() == 1)
                    thread.setPriority(Thread.NORM_PRIORITY);
                if (OrebfuscatorConfig.getOrebfuscatorPriority() == 2)
                    thread.setPriority(Thread.MAX_PRIORITY);

                queues.add(new ProcessingQueue(thread));
                thread.start();
            }
        }
    }
    
    public static void Queue(Packet56MapChunkBulk packet, CraftPlayer player)
    {
        getPlayerQueue(player).Queue(new QueuedPacket(player, packet));
    }
    
    public static void Queue(Packet51MapChunk packet, CraftPlayer player)
    {
        getPlayerQueue(player).Queue(new QueuedPacket(player, packet));
    }
    
    public static ProcessingQueue getPlayerQueue(CraftPlayer player)
    {
        ProcessingQueue smallestQueue = queues.get(0);
        for (ProcessingQueue queue : queues)
        {
            if(queue.packetsWaiting < smallestQueue.packetsWaiting)
                smallestQueue = queue;
            if (queue.playersInvolved.containsKey(player))
            {
                return queue;
            }
        }
        return smallestQueue;
    }
    
    private AtomicBoolean kill = new AtomicBoolean(false);
    private byte[] chunkBuffer = new byte[65536];
    LinkedBlockingDeque<QueuedPacket> queue = new LinkedBlockingDeque<QueuedPacket>(QUEUE_CAPACITY);
    ProcessingQueue processor;
    
    public void run()
    {
        while (!this.isInterrupted() && !kill.get())
        {
            try
            {
                // Take a package from the queue
                QueuedPacket packet = processor.Take();
                
                // Don't waste CPU if the player is gone
                if (packet.player.getHandle().netServerHandler.disconnected)
                {
                    continue;
                }
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
                    OverflowPacketQueue.Queue(packet.player, packet.packet, new ChunkInfo[0]);
                }
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    private static class ProcessingQueue
    {
        OrebfuscatorThreadCalculation thread;
        WeakHashMap<CraftPlayer, Integer> playersInvolved = new WeakHashMap<CraftPlayer, Integer>();
        int packetsWaiting = 0;
        
        ProcessingQueue(OrebfuscatorThreadCalculation thread)
        {
            thread.processor = this;
            this.thread = thread;
        }
        
        void Queue(QueuedPacket packet)
        {
            packetsWaiting++;
            
            while (true)
            {
                try
                {
                    thread.queue.add(packet);
                    return;
                }
                catch (Exception e)
                {
                    Orebfuscator.log(e);
                }
            }
        }
        
        QueuedPacket Take()
        {
            packetsWaiting--;
            while (true)
            {
                try
                {
                    return thread.queue.take();
                }
                catch (Exception e)
                {
                    Orebfuscator.log(e);
                }
            }
        }
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