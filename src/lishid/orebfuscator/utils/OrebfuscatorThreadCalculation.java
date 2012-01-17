package lishid.orebfuscator.utils;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import lishid.orebfuscator.Orebfuscator;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import net.minecraft.server.Packet51MapChunk;

public class OrebfuscatorThreadCalculation extends Thread implements Runnable
{
	private static final Object syncObj = new Object();
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
	
	public static void SyncThreads()
	{
		synchronized(syncObj)
		{
			int extra = threads.size() - OrebfuscatorConfig.getProcessingThreads();
			if (extra > 0)
			{
				for(int i = extra; i > 0; i--)
				{
					threads.get(i - 1).kill.set(true);
					threads.remove(i - 1);
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
	}
	
	public static void Queue(Packet51MapChunk packet, CraftPlayer player)
	{
		while(true)
		{
			try {
				queue.put(new ObfuscatedPlayerPacket(player, packet));
				return;
			}
			catch (Exception e) { Orebfuscator.log(e); }
		}
	}

	private AtomicBoolean kill = new AtomicBoolean(false);
	
	public void run() {
		while (!this.isInterrupted() && !kill.get()) {
			try {
				ObfuscatedPlayerPacket packet = queue.take();
				Calculations.Obfuscate(packet.packet, packet.player);
			}
			catch (Exception e) { Orebfuscator.log(e); }
		}
	}
}