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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.comphenix.protocol.async.AsyncRunnable;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.hook.ProtocolLibHook;

public class OrebfuscatorSchedulerProtocolLib extends OrebfuscatorScheduler
{
    
    private ProtocolLibHook protocolLibHook;
    private Map<Integer, OrebfuscatorThreadCalculation> workers = new ConcurrentHashMap<Integer, OrebfuscatorThreadCalculation>();
    
    public OrebfuscatorSchedulerProtocolLib(ProtocolLibHook protocolLibHook)
    {
        super();
        this.protocolLibHook = protocolLibHook;
    }
    
    @Override
    protected OrebfuscatorThreadCalculation createThread()
    {
        return new OrebfuscatorThreadCalculation(false)
        {
            private AsyncRunnable runnable;
            
            @Override
            public void kill()
            {
                try
                {
                    if (runnable != null)
                    {
                        workers.remove(runnable.getID());
                        runnable.stop();
                        runnable = null;
                    }
                }
                catch (InterruptedException e)
                {
                    Orebfuscator.log(e);
                }
            }
            
            @Override
            public void run()
            {
                runnable = protocolLibHook.getAsyncHandler().getListenerLoop();
                workers.put(runnable.getID(), this);
                
                System.out.println("Created worker " + runnable.getID());
                
                // Start worker
                runnable.run();
            }
            
            @Override
            protected void sendPacket(Packet packet, CraftPlayer player)
            {
                // ProtocolLib will send it for us (at the end)
            }
        };
    }
    
    public OrebfuscatorThreadCalculation getCalculator(int id)
    {
        return workers.get(id);
    }
    
    @Override
    public void Queue(Packet56MapChunkBulk packet, CraftPlayer player)
    {
        throw new IllegalStateException("ProtocolLib does the packet queuing for us.");
    }
    
    @Override
    public void Queue(Packet51MapChunk packet, CraftPlayer player)
    {
        throw new IllegalStateException("ProtocolLib does the packet queuing for us.");
    }
}
