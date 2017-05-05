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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.chunkmap.ChunkData;
import com.lishid.orebfuscator.config.WorldConfig;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.Calculations;

public class ProtocolLibHook {
    private ProtocolManager manager;

    public void register(Plugin plugin) {
        this.manager = ProtocolLibrary.getProtocolManager();

        this.manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(PacketEvent event) {
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
        		chunkData.blockEntities = getBlockEntities(packet, event.getPlayer());
                
				try {
					byte[] newData = Calculations.obfuscateOrUseCache(chunkData, event.getPlayer());
					
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
                EnumWrappers.PlayerDigType status = event.getPacket().getPlayerDigTypes().read(0);
                if (status == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK) {
                    if (!BlockHitManager.hitBlock(event.getPlayer(), null)) {
                        event.setCancelled(true);
                    }
                }
            }
        });
    }
    
    @SuppressWarnings("rawtypes")
	private static List<NbtCompound> getBlockEntities(PacketContainer packet, Player player) {
    	WorldConfig worldConfig = Orebfuscator.configManager.getWorld(player.getWorld());
    	
    	if(!worldConfig.isBypassObfuscationForSignsWithText()) {
    		return null;
    	}
    	
    	List list = packet.getSpecificModifier(List.class).read(0);
    	List<NbtCompound> result = new ArrayList<NbtCompound>();
    	
    	for(Object tag : list) {
    		result.add(NbtFactory.fromNMSCompound(tag));
    	}
    	
    	return result;
    }
    
    /*
    private static boolean _isSaved;
    private void saveTestData(ChunkData chunkData) {
    	if(_isSaved) return;
    	
		_isSaved = true;

		FileOutputStream fos;
		try {
			fos = new FileOutputStream("D:\\Temp\\chunk.dat");
			fos.write(chunkData.chunkX & 0xff);
			fos.write((chunkData.chunkX >> 8) & 0xff);
			fos.write(chunkData.chunkZ & 0xff);
			fos.write((chunkData.chunkZ >> 8) & 0xff);
			fos.write(chunkData.primaryBitMask & 0xff);
			fos.write((chunkData.primaryBitMask >> 8) & 0xff);
			fos.write(chunkData.data.length & 0xff);
			fos.write((chunkData.data.length >> 8) & 0xff);
			fos.write((chunkData.data.length >> 16) & 0xff);
			fos.write(chunkData.data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
}
