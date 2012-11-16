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

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet130UpdateSign;
import net.minecraft.server.Packet132TileEntityData;
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
import com.lishid.orebfuscator.utils.ReflectionHelper;

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
        manager = ProtocolLibrary.getProtocolManager();
        stream = new ChunkCompressorStream();
        
        //Process Chunk packets
        Integer[] packets = new Integer[] { Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK };
        
        manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, packets)
        {
            @Override
            public void onPacketSending(PacketEvent event)
            {
                EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();
                switch (event.getPacketID())
                {
                    case Packets.Server.MAP_CHUNK:
                        
                        Packet51MapChunk packet2 = (Packet51MapChunk) event.getPacket().getHandle();
                        
                        // If empty chunk, then send it out directly
                        if ((packet2.c == 0) && (packet2.d == 0))
                        {
                            event.getAsyncMarker().setAsyncCancelled(true);
                        }
                        else
                        {
                            // Tell the Chunk Manager that we're processing that chunk, so that we can delay any TileEntity updates
                            ChunkCompressionThread.QueueChunkProcessing(player, player.world.getChunkAt(packet2.a, packet2.b));
                            // Synchronize threading
                            scheduler.SyncThreads();
                        }
                        break;
                    
                    case Packets.Server.MAP_CHUNK_BULK:
                        
                        Packet56MapChunkBulk packet3 = (Packet56MapChunkBulk) event.getPacket().getHandle();
                        
                        // Get X and Z coords of chunks within the packet
                        int[] x = (int[]) ReflectionHelper.getPrivateField(packet3, "c");
                        int[] z = (int[]) ReflectionHelper.getPrivateField(packet3, "d");
                        
                        for (int i = 0; i < x.length; i++)
                        {
                            // Tell the Chunk Manager that we're processing that chunk, so that we can delay any TileEntity updates
                            ChunkCompressionThread.QueueChunkProcessing(player, player.world.getChunkAt(x[i], z[i]));
                        }
                        
                        // Synchronize threading
                        scheduler.SyncThreads();
                        break;
                }
            }
        });

        // This is to fix a bukkit security hole causing raw block IDs being sent to clients
        Integer[] packets2 = new Integer[] { Packets.Client.BLOCK_DIG };
        
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
                            if (event != null && event.getPlayer() != null)
                            {
                                if (!BlockHitManager.canFakeHit(event.getPlayer()))
                                {
                                    BlockHitManager.fakeHit(event.getPlayer());
                                    event.setCancelled(true);
                                    break;
                                }
                                
                                // Find distance to player
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

        // Intercept all TileEntity updates
        Integer[] packets3 = new Integer[] { Packets.Server.UPDATE_SIGN, Packets.Server.TILE_ENTITY_DATA };
        
        manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, packets3)
        {
            @Override
            public void onPacketSending(PacketEvent event)
            {
                EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();
                
                switch (event.getPacketID())
                {
                    case Packets.Server.UPDATE_SIGN:
                        if (event.getPacket().getHandle() instanceof Packet130UpdateSign)
                        {
                            Packet130UpdateSign newPacket = (Packet130UpdateSign) event.getPacket().getHandle();

                            // Check if the chunk is being processed. If so, the packet is saved to be sent later.
                            if (!ChunkCompressionThread.CheckChunkPacket(player, player.world.getChunkAt(newPacket.x >> 4, newPacket.z >> 4), newPacket))
                            {
                                event.setCancelled(true);
                            }
                        }
                        break;
                    case Packets.Server.TILE_ENTITY_DATA:
                        if (event.getPacket().getHandle() instanceof Packet132TileEntityData)
                        {
                            Packet132TileEntityData newPacket = (Packet132TileEntityData) event.getPacket().getHandle();

                            // Check if the chunk is being processed. If so, the packet is saved to be sent later.
                            if (!ChunkCompressionThread.CheckChunkPacket(player, player.world.getChunkAt(newPacket.a >> 4, newPacket.b >> 4), newPacket))
                            {
                                event.setCancelled(true);
                            }
                        }
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
                    marker.setTimeout(Long.MAX_VALUE);
                    OrebfuscatorThreadCalculation thread = getWorker(marker);
                    
                    CraftPlayer player = (CraftPlayer) event.getPlayer();
                    Packet packet = event.getPacket().getHandle();
                    
                    if (thread == null)
                    {
                        System.out.println("Cannot find worker " + marker.getWorkerID());
                        return;
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
