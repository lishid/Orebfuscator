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
import lishid.orebfuscator.OrebfuscatorConfig;
import lishid.orebfuscator.obfuscation.Calculations;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import net.minecraft.server.Packet51MapChunk;

public class OrebfuscatorThreadCalculation extends Thread implements Runnable
{
	private static final int QUEUE_CAPACITY = 1024 * 10;
	private static ArrayList<OrebfuscatorThreadCalculation> threads = new ArrayList<OrebfuscatorThreadCalculation>();
	private static final LinkedBlockingDeque<PlayerPacket> queue = new LinkedBlockingDeque<PlayerPacket>(QUEUE_CAPACITY);
	
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
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
				threads.add(thread);
			}
		}
	}
	
	public static void Queue(Packet51MapChunk packet, CraftPlayer player)
	{
		while(true)
		{
			try {
				if(packet.a == player.getLocation().getChunk().getX() && packet.b == player.getLocation().getChunk().getZ())
				{
					queue.putFirst(new PlayerPacket(player, packet));
				}
				else
				{
					queue.put(new PlayerPacket(player, packet));
				}
				return;
			}
			catch (Exception e) { Orebfuscator.log(e); }
		}
	}
	
	private AtomicBoolean kill = new AtomicBoolean(false);
	private byte[] chunkBuffer = new byte[65536];
	
	public void run() {
		while (!this.isInterrupted() && !kill.get()) {
			try {
				//Take a package from the queue
				PlayerPacket packet = queue.take();
				
				try {
					//Try to obfuscate and send the packet
					Calculations.Obfuscate(packet.packet, packet.player, true, chunkBuffer);
				}
				catch (Throwable e)
				{
					Orebfuscator.log(e);
		    		//If we run into problems, just send the packet.

					//Compress packets
					if(packet.packet.buffer == null)
					{
						try{
							synchronized(Calculations.deflateBuffer)
							{
								//Compression
						        int dataSize = packet.packet.rawData.length;
						        if (Calculations.deflateBuffer.length < dataSize + 100) {
						        	Calculations.deflateBuffer = new byte[dataSize + 100];
						        }
						        
						        Calculations.deflater.reset();
						        Calculations.deflater.setLevel(dataSize < 20480 ? 1 : 6);
						        Calculations.deflater.setInput(packet.packet.rawData);
						        Calculations.deflater.finish();
						        int size = Calculations.deflater.deflate(Calculations.deflateBuffer);
						        if (size == 0) {
						            size = Calculations.deflater.deflate(Calculations.deflateBuffer);
						        }
					        	
						        //Copy compressed packet out
						        packet.packet.buffer = new byte[size];
						        packet.packet.size = size;
						        System.arraycopy(Calculations.deflateBuffer, 0, packet.packet.buffer, 0, size);
							}
						} catch (Exception e2) { Orebfuscator.log(e2); }
					}
					
					packet.player.getHandle().netServerHandler.sendPacket(packet.packet);
				}
			} catch (Exception e) { Orebfuscator.log(e); }
		}
	}
}