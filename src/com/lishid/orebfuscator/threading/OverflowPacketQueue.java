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

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.obfuscation.CalculationsUtil;
import com.lishid.orebfuscator.obfuscation.ChunkInfo;

public class OverflowPacketQueue extends Thread implements Runnable
{
    private static final int QUEUE_CAPACITY = 1024 * 10;
    public static OverflowPacketQueue thread = new OverflowPacketQueue();
    public static Object threadLock = new Object();
    private static final LinkedBlockingDeque<QueuedPacket> queue = new LinkedBlockingDeque<QueuedPacket>(QUEUE_CAPACITY);
    
    private final int CHUNK_SIZE = 16 * 256 * 16 * 5 / 2;
    private final int REDUCED_DEFLATE_THRESHOLD = CHUNK_SIZE / 4;
    private final int DEFLATE_LEVEL_CHUNKS = 6;
    private final int DEFLATE_LEVEL_PARTS = 1;
    private final Deflater deflater = new Deflater();
    private byte[] deflateBuffer = new byte[CHUNK_SIZE + 100];
    
    public long lastExecute = System.currentTimeMillis();
    public AtomicBoolean kill = new AtomicBoolean(false);
    
    public static void terminate()
    {
        if (thread != null)
        {
            thread.kill.set(true);
        }
    }
    
    public static void Queue(CraftPlayer player, Packet packet, ChunkInfo[] info)
    {
        Queue(new QueuedPacket(player.getHandle(), packet, info));
    }
    
    public static void sendOut(QueuedPacket packet)
    {
        packet.player.netServerHandler.networkManager.queue(packet.packet);
        for(ChunkInfo info : packet.info)
        {
            if(info != null)
            {
                Calculations.sendTileEntities(info);
            }
        }
    }
    
    public static void Queue(QueuedPacket packet)
    {
        synchronized(threadLock)
        {
            if (thread == null || thread.isInterrupted() || !thread.isAlive())
            {
                thread = new OverflowPacketQueue();
                thread.setName("OverflowPacketMonitor Thread");
                thread.setPriority(Thread.MIN_PRIORITY);
                try
                {
                    thread.start();
                }
                catch (Exception e)
                {
                    //Orebfuscator.log(e);
                }
            }
        }
        
        while (true)
        {
            try
            {
                queue.put(packet);
                return;
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    public void run()
    {
        while (!this.isInterrupted() && !kill.get())
        {
            try
            {
                // Wait until necessary
                long timeWait = lastExecute + OrebfuscatorConfig.getOverflowPacketCheckRate() - System.currentTimeMillis();
                lastExecute = System.currentTimeMillis();
                if (timeWait > 0)
                {
                    Thread.sleep(timeWait);
                }
                
                int size = queue.size();
                if (size > 0)
                {
                    for (int i = 0; i < size; i++)
                    {
                        QueuedPacket packet = queue.take();
                        
                        if (packet.player.netServerHandler.disconnected)
                        {
                            continue;
                        }
                        
                        NetworkManager nm = (NetworkManager) packet.player.netServerHandler.networkManager;
                        if (isOverflowing(nm, packet.packet.a()))
                        {
                            // If overflowing, then re-queue the packet and wait for a later time to send it
                            Queue(packet);
                        }
                        else
                        {
                            // If not overflowing then send the packet out
                            CompressChunk(packet.packet);
                            sendOut(packet);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    private void CompressChunk(Packet packet)
    {
        if (packet instanceof Packet56MapChunkBulk)
        {
            Packet56MapChunkBulk newPacket = (Packet56MapChunkBulk) packet;
            if (newPacket.buffer != null)
            {
                return;
            }
            int dataSize = newPacket.buildBuffer.length;
            if (deflateBuffer.length < dataSize + 100)
            {
                deflateBuffer = new byte[dataSize + 100];
            }
            
            deflater.reset();
            deflater.setLevel(dataSize < REDUCED_DEFLATE_THRESHOLD ? DEFLATE_LEVEL_PARTS : DEFLATE_LEVEL_CHUNKS);
            deflater.setInput(newPacket.buildBuffer);
            deflater.finish();
            int size = deflater.deflate(deflateBuffer);
            if (size == 0)
            {
                size = deflater.deflate(deflateBuffer);
            }
            
            // copy compressed data to packet
            newPacket.buffer = new byte[size];
            newPacket.size = size;
            System.arraycopy(deflateBuffer, 0, newPacket.buffer, 0, size);
        }
        else if (packet instanceof Packet51MapChunk)
        {
            Packet51MapChunk newPacket = (Packet51MapChunk) packet;
            if (newPacket.buffer != null)
            {
                return;
            }
            int dataSize = newPacket.inflatedBuffer.length;
            if (deflateBuffer.length < dataSize + 100)
            {
                deflateBuffer = new byte[dataSize + 100];
            }
            
            deflater.reset();
            deflater.setLevel(dataSize < REDUCED_DEFLATE_THRESHOLD ? DEFLATE_LEVEL_PARTS : DEFLATE_LEVEL_CHUNKS);
            deflater.setInput(newPacket.inflatedBuffer);
            deflater.finish();
            int size = deflater.deflate(deflateBuffer);
            if (size == 0)
            {
                size = deflater.deflate(deflateBuffer);
            }
            
            // copy compressed data to packet
            newPacket.buffer = new byte[size];
            newPacket.size = size;
            System.arraycopy(deflateBuffer, 0, newPacket.buffer, 0, size);
        }
    }
    
    public static boolean isOverflowing(NetworkManager nm, int size)
    {
        int y = (Integer) CalculationsUtil.getPrivateField(nm, "y");
        return y > 524288;
    }
    
    private static class QueuedPacket
    {
        final EntityPlayer player;
        final Packet packet;
        final ChunkInfo[] info;
        
        QueuedPacket(EntityPlayer player, Packet packet, ChunkInfo[] info)
        {
            this.player = player;
            this.packet = packet;
            this.info = info;
        }
    }
}
