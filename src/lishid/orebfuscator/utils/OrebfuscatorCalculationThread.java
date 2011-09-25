package lishid.orebfuscator.utils;

import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.server.Packet51MapChunk;

public class OrebfuscatorCalculationThread extends Thread implements Runnable
{
	//private static final int QUEUE_CAPACITY = 1024 * 10; // how many packets can be queued before the main thread blocks
	
	private static final OrebfuscatorCalculationThread instance = new OrebfuscatorCalculationThread();
	private static int TotalPackets = 0;
	private static Thread thread = null;
	private static boolean runs = false;
	
	public static boolean isRunning()
	{
		return runs;
	}
	
	public static void startThread() {
		if (!runs) {
			runs = true;
			thread = new Thread(instance);
			thread.start();
		}
	}

	public static void endThread() {
		instance.kill.set(true);
		if (thread != null) {
			thread.interrupt();
		}
		try {
			thread.join();
		} catch (InterruptedException ie) {
		}
		thread = null;
	}
	
	private final Queue<ObfuscatedPlayer> queue = new LinkedList<ObfuscatedPlayer>();
	private final AtomicBoolean kill = new AtomicBoolean(false);

	public void run() {
		while (thread != null && !thread.isInterrupted() && !kill.get()) {
			if(TotalPackets == 0)
			{
				synchronized(queue)
				{
					try {
						queue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				handle();
			} catch (Exception e) {
				e.printStackTrace(); // print & ignore
			}
		}
	}

	private void handle() {
		try {
			if(queue.size() > 0)
			{
				ObfuscatedPlayer player;
				synchronized(queue)
				{
					player = queue.poll();
				}
				if(player.packetQueue.size() > 0)
				{
					Calculations.Obfuscate(player.packetQueue.poll(), player.player);
					TotalPackets--;
				}
				if(player.player.isOnline())
				{
					synchronized(queue)
					{
						queue.add(player);
					}
				}
			}
		}catch(Exception e){e.printStackTrace();}
	}

	public static void Queue(Packet51MapChunk packet, CraftPlayer player)
	{
		TotalPackets++;
		synchronized(instance.queue)
		{
			for(ObfuscatedPlayer playerQueue : instance.queue)
			{
				if(playerQueue.player == player)
				{
					playerQueue.EnQueue(packet);
					instance.queue.notify();
					return;
				}
			}
			ObfuscatedPlayer playerQueue = new ObfuscatedPlayer(player);
			playerQueue.EnQueue(packet);
			instance.queue.add(playerQueue);
			instance.queue.notify();
		}
	}
}
