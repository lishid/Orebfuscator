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

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IChunkQueue;
import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.obfuscation.Calculations;

public class ChunkProcessingThread extends Thread
{
    private static LinkedBlockingDeque<ChunkProcessingOrder> queue = new LinkedBlockingDeque<ChunkProcessingThread.ChunkProcessingOrder>();
    
    private static LinkedList<ChunkProcessingThread> threads = new LinkedList<ChunkProcessingThread>();
    
    static ThreadLocal<Deflater> localDeflater = new ThreadLocal<Deflater>()
    {
        @Override
        protected Deflater initialValue()
        {
            // Use higher compression level if asked
            return new Deflater(OrebfuscatorConfig.getCompressionLevel());
        }
    };
    
    static class ChunkProcessingOrder
    {
        IPacket56 packet;
        Player player;
        IChunkQueue output;
        
        public ChunkProcessingOrder(IPacket56 packet, Player player, IChunkQueue output)
        {
            this.packet = packet;
            this.player = player;
            this.output = output;
        }
    }
    
    public synchronized static void KillAll()
    {
        for (ChunkProcessingThread thread : threads)
        {
            thread.kill.set(true);
            thread.interrupt();
        }
        threads.clear();
        queue.clear();
    }
    
    public synchronized static void SyncThreads()
    {
        if (threads.size() > OrebfuscatorConfig.getProcessingThreads())
        {
            threads.getLast().kill.set(true);
            threads.getLast().interrupt();
            threads.removeLast();
            return;
        }
        else if(threads.size() == OrebfuscatorConfig.getProcessingThreads())
        {
            return;
        }
        
        int startThreads = OrebfuscatorConfig.getProcessingThreads() - threads.size();
        for (int i = 0; i < startThreads; i++)
        {
            ChunkProcessingThread thread = new ChunkProcessingThread();
            thread.setName("Orebfuscator Processing Thread");
            thread.setPriority(MIN_PRIORITY);
            thread.start();
            threads.add(thread);
        }
    }
    
    public static void Queue(IPacket56 packet, Player player, IChunkQueue output)
    {
        SyncThreads();
        try
        {
            queue.put(new ChunkProcessingOrder(packet, player, output));
        }
        catch (InterruptedException e)
        {
            // Should never come here unless there's INT_MAX players online.
        }
    }
    
    AtomicBoolean kill = new AtomicBoolean(false);
    
    @Override
    public void run()
    {
        while (!Thread.interrupted() && !kill.get())
        {
            try
            {
                ChunkProcessingOrder order = queue.take();
                Calculations.Obfuscate(order.packet, order.player);
                order.packet.compress(localDeflater.get());
                order.output.FinishedProcessing(order.packet);
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                // If interrupted then exit
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
}
