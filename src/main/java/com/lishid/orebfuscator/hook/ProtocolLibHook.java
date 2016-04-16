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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
import com.lishid.orebfuscator.chunkmap.ChunkMapManager;
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
        		
        		/*
				try {
					byte[] newData = test(chunkData);
	        		byteArray.write(0, newData);
				} catch (Exception e) {
					e.printStackTrace();
					//saveTestData(chunkData);
				}
				*/
				
				//Orebfuscator.log(event.getPlayer().getWorld().getEnvironment().toString());
        		
                //Orebfuscator.log("Orebfuscator packet.getIntegers(): " + ints.size());
                //Orebfuscator.log("Orebfuscator packet.getByteArrays(): " + byteArray.size());
                //Orebfuscator.log("Orebfuscator packet.getByteArrays().length: " + byteArray.read(0).length);
                
				try {
					byte[] newData = Calculations.ObfuscateOrUseCache(chunkData, event.getPlayer());
	                byteArray.write(0, newData);
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
    
    private byte[] test(ChunkData chunkData) throws IOException {
		ChunkMapManager manager = new ChunkMapManager(chunkData);
		manager.init();
		
		for(int i = 0; i < manager.getSectionCount(); i++) {
			for(int j = 0; j < 16; j++) {
				for(int offsetZ = 0; offsetZ < 16; offsetZ++) {
					for(int offsetX = 0; offsetX < 16; offsetX++) {						
						int blockData = manager.readNextBlock();

						if(j == 0 && offsetZ == 0 && offsetX == 0) {
							manager.finalizeOutput();
							
							manager.initOutputPalette();
							manager.addToOutputPalette(16);
							manager.initOutputSection();
						}
						
						//manager.writeOutputBlock(blockData);
						
						blockData = blockData == 32 ? 16: blockData;
						
						manager.writeOutputBlock(blockData);
					}
				}
			}
		}
		
		manager.finalizeOutput();
		
		return manager.createOutput();
    }
    
    private void saveTestData(ChunkData chunkData) {
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
    		fos.write(chunkData.data);
    		fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
