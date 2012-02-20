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

package lishid.orebfuscator.hook;

import java.util.HashMap;

import lishid.orebfuscator.utils.Calculations;
import net.minecraft.server.Packet51MapChunk;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.getspout.spout.packet.standard.MCCraftPacket;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.packet.listener.PacketListener;
import org.getspout.spoutapi.packet.standard.MCPacket;

public class SpoutLoader {

	private static HashMap<String, byte[]> chunkBuffers = new HashMap<String, byte[]>();
	
	public static void InitializeSpout()
	{
		//Add spout listeners
		SpoutManager.getPacketManager().addListenerUncompressedChunk(new PacketListener(){
			
			//Processing a chunk packet
			public boolean checkPacket(Player player, MCPacket mcpacket)
			{
				if ((player == null) || (mcpacket == null) || (player.getWorld() == null)) return true;
				
				//Process the chunk
				if(((MCCraftPacket)mcpacket).getPacket() instanceof Packet51MapChunk)
				{
					Calculations.Obfuscate((Packet51MapChunk)((MCCraftPacket)mcpacket).getPacket(), (CraftPlayer)player, false, getBuffer());
				}
				return true;
			}
		});
	}
	
	private static byte[] getBuffer()
	{
		String thread = ((Long)Thread.currentThread().getId()).toString();
		if(!chunkBuffers.containsKey(thread))
			chunkBuffers.put(thread, new byte[16 * 16 * 128]);
		return chunkBuffers.get(thread);
	}
}
