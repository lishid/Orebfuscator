/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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

import net.minecraft.server.v1_9_R1.PacketPlayInBlockDig.EnumPlayerDigType;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.lishid.orebfuscator.chunkmap.ChunkData;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.Calculations;

public class ProtocolLibHook {
	private static boolean _isSaved = false;
	
    private ProtocolManager manager;

    public void register(Plugin plugin) {
        this.manager = ProtocolLibrary.getProtocolManager();

        this.manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(PacketEvent event) {
            	if(_isSaved) return;
            	
            	PacketContainer packet = event.getPacket();
            	
            	StructureModifier<Integer> ints = packet.getIntegers();
                StructureModifier<byte[]> byteArray = packet.getByteArrays();
                StructureModifier<Boolean> bools = packet.getBooleans();
                
                ChunkData chunkData = new ChunkData();
        		chunkData.chunkX = ints.read(0);
        		chunkData.chunkZ = ints.read(1);
        		chunkData.groundUpContinuous = bools.read(0);
        		chunkData.primaryBitMask = ints.read(2);
        		chunkData.data = byteArray.read(0);
        		chunkData.isOverworld = event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL;
                
				try {
					byte[] newData = Calculations.ObfuscateOrUseCache(chunkData, event.getPlayer());
					
					if(newData != null) {
						byteArray.write(0, newData);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                EnumPlayerDigType status = event.getPacket().getSpecificModifier(EnumPlayerDigType.class).read(0);
                if (status == EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                    if (!BlockHitManager.hitBlock(event.getPlayer(), null)) {
                        event.setCancelled(true);
                    }
                }
            }
        });
    }
}
