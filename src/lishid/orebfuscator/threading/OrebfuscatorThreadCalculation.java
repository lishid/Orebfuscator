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

package lishid.orebfuscator.threading;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.utils.Calculations;
import lishid.orebfuscator.utils.ObfuscatedPlayerPacket;
import lishid.orebfuscator.utils.OrebfuscatorConfig;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import net.minecraft.server.Packet51MapChunk;

public class OrebfuscatorThreadCalculation extends Thread implements Runnable
{
	private static final int QUEUE_CAPACITY = 1024 * 10;
	private static ArrayList<OrebfuscatorThreadCalculation> threads = new ArrayList<OrebfuscatorThreadCalculation>();
	private static final LinkedBlockingDeque<ObfuscatedPlayerPacket> queue = new LinkedBlockingDeque<ObfuscatedPlayerPacket>(QUEUE_CAPACITY);
	
	public static int getThreads()
	{
		return threads.size();
	}
	
	public static boolean CheckThreads()
	{
		return threads.size() == OrebfuscatorConfig.getProcessingThreads();
	}
	
	public static void terminateAll()
	{
		for(int i = 0; i < threads.size(); i++)
		{
			threads.get(i).kill.set(true);
			threads.remove(i);
		}
	}
	
	public static synchronized void SyncThreads()
	{
		int extra = threads.size() - OrebfuscatorConfig.getProcessingThreads();
		if (extra > 0)
		{
			for(int i = extra - 1; i >= 0; i--)
			{
				threads.get(i).kill.set(true);
				threads.remove(i);
			}
		}
		else if (extra < 0)
		{
			extra = -extra;
			for(int i = 0; i < extra; i++)
			{
				OrebfuscatorThreadCalculation thread = new OrebfuscatorThreadCalculation();
				thread.setName("Orebfuscator Calculation Thread");
				thread.start();
				threads.add(thread);
			}
		}
	}
	
	public static void Queue(Packet51MapChunk packet, CraftPlayer player)
	{
		while(true)
		{
			try {/*
				int x = packet.a >> 4;
				int z = packet.c >> 4;
				WorldServer world = player.getHandle().world.getWorld().getHandle();
				tryLoadChunk(world, x+1, z);
				tryLoadChunk(world, x-1, z);
				tryLoadChunk(world, x, z+1);
				tryLoadChunk(world, x, z-1);
				tryLoadChunk(world, x+1, z+1);
				tryLoadChunk(world, x+1, z-1);
				tryLoadChunk(world, x-1, z+1);
				tryLoadChunk(world, x-1, z-1);*/
				queue.put(new ObfuscatedPlayerPacket(player, packet));
				return;
			}
			catch (Exception e) { Orebfuscator.log(e); }
		}
	}
	/*
	private static void tryLoadChunk(WorldServer world, int x, int z)
	{
		if(!world.isLoaded(x << 4, 0, z << 4))
		{
			try
			{
				world.chunkProvider.getChunkAt(x+1, z);
			}
			catch (Exception e) { Orebfuscator.log(e); }
		}
	}
*/
	private AtomicBoolean kill = new AtomicBoolean(false);
	private byte[] chunkBuffer = new byte[16 * 16 * 128];
	
	public void run() {
		while (!this.isInterrupted() && !kill.get()) {
			try {
				//Take a package from the queue
				ObfuscatedPlayerPacket packet = queue.take();
				
				try {
					//Try to obfuscate and send the packet
					Calculations.Obfuscate(packet.packet, packet.player, true, chunkBuffer);
				}
				catch (Exception e)
				{
					Orebfuscator.log(e);
		    		//If we run into problems, just send the packet.
					packet.player.getHandle().netServerHandler.sendPacket(packet.packet);
				}
			} catch (Exception e) { Orebfuscator.log(e); }
		}
	}
}