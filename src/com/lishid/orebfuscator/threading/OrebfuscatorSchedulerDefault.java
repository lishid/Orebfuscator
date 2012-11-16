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

import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.lishid.orebfuscator.Orebfuscator;

public class OrebfuscatorSchedulerDefault extends OrebfuscatorScheduler
{
    
    private final int QUEUE_CAPACITY = 1024 * 10;
    private final LinkedBlockingDeque<QueuedPacket> queue = new LinkedBlockingDeque<QueuedPacket>(QUEUE_CAPACITY);
    
    @Override
    protected OrebfuscatorThreadCalculation createThread()
    {
        return new OrebfuscatorThreadCalculation(true)
        {
            @Override
            public void run()
            {
                while (!this.isInterrupted() && !kill.get())
                {
                    try
                    {
                        // Take a package from the queue
                        QueuedPacket packet = queue.take();
                        processPacket(packet);
                    }
                    catch (InterruptedException e)
                    {
                        Orebfuscator.log(e);
                    }
                }
                
                cleanup();
            }
        };
    }
    
    @Override
    public void Queue(Packet56MapChunkBulk packet, CraftPlayer player)
    {
        boolean isImportant = isImportant(packet, player);
        
        while (true)
        {
            try
            {
                if (isImportant)
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
    
    @Override
    public void Queue(Packet51MapChunk packet, CraftPlayer player)
    {
        while (true)
        {
            try
            {
                if (isImportant(packet, player))
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
}
