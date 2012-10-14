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

import java.lang.reflect.InvocationTargetException;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet14BlockDig;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.obfuscation.ChunkInfo;
import com.lishid.orebfuscator.threading.ChunkCompressionThread;
import com.lishid.orebfuscator.threading.OrebfuscatorScheduler;
import com.lishid.orebfuscator.threading.OrebfuscatorSchedulerProtocolLib;
import com.lishid.orebfuscator.threading.OrebfuscatorThreadCalculation;

public class ProtocolLibHook
{
    
    private class ChunkCompressorStream implements PacketStream
    {
        @Override
        public void recieveClientPacket(Player sender, PacketContainer packet) throws IllegalAccessException, InvocationTargetException
        {
            manager.recieveClientPacket(sender, packet, true);
        }
        
        @Override
        public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) throws IllegalAccessException, InvocationTargetException
        {
            manager.recieveClientPacket(sender, packet, filters);
        }
        
        @Override
        public void sendServerPacket(Player reciever, PacketContainer packet) throws InvocationTargetException
        {
            sendServerPacket(reciever, packet, true);
        }
        
        @Override
        public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException
        {
            
            CraftPlayer player = (CraftPlayer) reciever;
            Packet mcPacket = packet.getHandle();
            ChunkInfo[] info;
            
            if (mcPacket instanceof Packet51MapChunk)
            {
                info = new ChunkInfo[] { Calculations.getInfo((Packet51MapChunk) mcPacket, player) };
            }
            else if (mcPacket instanceof Packet56MapChunkBulk)
            {
                info = Calculations.getInfo((Packet56MapChunkBulk) mcPacket, player);
            }
            else
            {
                // Never mind
                manager.sendServerPacket(reciever, packet, filters);
                return;
            }
            
            ChunkCompressionThread.Queue((CraftPlayer) reciever, mcPacket, info);
        }
    }
    
    private ProtocolManager manager;
    
    private AsyncListenerHandler asyncHandler;
    private OrebfuscatorSchedulerProtocolLib scheduler;
    private ChunkCompressorStream stream;
    
    public void register(Plugin plugin)
    {
        
        Integer[] packets = new Integer[] { Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK };
        Integer[] packets2 = new Integer[] { Packets.Client.BLOCK_DIG };
        
        manager = ProtocolLibrary.getProtocolManager();
        stream = new ChunkCompressorStream();
        
        manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.CLIENT_SIDE, packets2)
        {
            @Override
            public void onPacketReceiving(PacketEvent event)
            {
                switch (event.getPacketID())
                {
                    case Packets.Client.BLOCK_DIG:
                        
                        Packet14BlockDig packet = (Packet14BlockDig) event.getPacket().getHandle();
                        
                        if (packet.e == 1 || packet.e == 3)
                        {
                            if (!BlockHitManager.canFakeHit(event.getPlayer()))
                            {
                                BlockHitManager.fakeHit(event.getPlayer());
                                event.setCancelled(true);
                            }
                            
                            if (event != null && event.getPlayer() != null)
                            {
                                // Anti-hack
                                int i = packet.a;
                                int j = packet.b;
                                int k = packet.c;
                                
                                double d4 = event.getPlayer().getLocation().getX() - ((double) i + 0.5D);
                                double d5 = event.getPlayer().getLocation().getY() - ((double) j + 0.5D);
                                double d6 = event.getPlayer().getLocation().getZ() - ((double) k + 0.5D);
                                double d7 = d4 * d4 + d5 * d5 + d6 * d6;
                                
                                if (d7 >= 256.0D)
                                {
                                    BlockHitManager.fakeHit(event.getPlayer());
                                    event.setCancelled(true);
                                }
                            }
                        }
                        break;
                }
            }
        });
        
        manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, packets)
        {
            @Override
            public void onPacketSending(PacketEvent event)
            {
                switch (event.getPacketID())
                {
                    case Packets.Server.MAP_CHUNK:
                    case Packets.Server.MAP_CHUNK_BULK:
                        // Create or remove more workers now
                        scheduler.SyncThreads();
                        break;
                }
            }
        });
        
        asyncHandler = manager.getAsynchronousManager().registerAsyncHandler(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH, packets)
        {
            
            @Override
            public void onPacketSending(PacketEvent event)
            {
                try
                {
                    AsyncMarker marker = event.getAsyncMarker();
                    OrebfuscatorThreadCalculation thread = getWorker(marker);
                    
                    CraftPlayer player = (CraftPlayer) event.getPlayer();
                    Packet packet = event.getPacket().getHandle();
                    
                    if (thread == null)
                    {
                        System.out.println("Cannot find worker " + marker.getWorkerID());
                        return;
                    }
                    
                    // Set priority too
                    if (packet instanceof Packet51MapChunk)
                    {
                        if (scheduler.isImportant((Packet51MapChunk) packet, player))
                            marker.setNewSendingIndex(0);
                    }
                    else if (packet instanceof Packet56MapChunkBulk)
                    {
                        if (scheduler.isImportant((Packet56MapChunkBulk) packet, player))
                            marker.setNewSendingIndex(0);
                    }
                    else
                    {
                        throw new IllegalArgumentException("Cannot process packet ID " + event.getPacketID());
                    }
                    
                    marker.setPacketStream(stream);
                    thread.processPacket(packet, player);
                    
                }
                catch (Exception e)
                {
                    Orebfuscator.log(e);
                }
            }
        });
        
        // Update thread scheduler
        scheduler = new OrebfuscatorSchedulerProtocolLib(this);
        OrebfuscatorScheduler.setScheduler(scheduler);
        scheduler.SyncThreads();
    }
    
    private OrebfuscatorThreadCalculation getWorker(AsyncMarker marker)
    {
        return scheduler.getCalculator(marker.getWorkerID());
    }
    
    public AsyncListenerHandler getAsyncHandler()
    {
        return asyncHandler;
    }
}
