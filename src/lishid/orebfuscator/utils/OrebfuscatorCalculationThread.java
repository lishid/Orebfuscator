package lishid.orebfuscator.utils;

//import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import lishid.orebfuscator.OrebfuscatorConfig;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import net.minecraft.server.Packet51MapChunk;

public class OrebfuscatorCalculationThread extends Thread implements Runnable
{
	private boolean kill = false;
//	private int ThreadNumber = 0;
//	private long CPUUsage = 0;
//	private int packetsProcessed = 0;
	//Global
	private static final int QUEUE_CAPACITY = 1024 * 10;
	private static ArrayList<OrebfuscatorCalculationThread> threads = new ArrayList<OrebfuscatorCalculationThread>();
	private static final LinkedBlockingDeque<ObfuscatedPlayerPacket> queue = new LinkedBlockingDeque<ObfuscatedPlayerPacket>(QUEUE_CAPACITY);
	
	public static int getThreads()
	{
		return threads.size();
	}
	
	public static boolean isRunning()
	{
		return threads.size() == OrebfuscatorConfig.ProcessingThreads();
	}
	
	public static void startThread() {
		int extra = threads.size() - OrebfuscatorConfig.ProcessingThreads();
		if (extra > 0) {
			for(int i = extra; i > 0; i--)
			{
				threads.get(i-1).kill = true;
				threads.remove(i-1);
			}
		}
		else if (extra < 0) {
			for(int i = 0; i < -extra; i++)
			{
				OrebfuscatorCalculationThread thread = new OrebfuscatorCalculationThread();
				thread.start();
				threads.add(thread);
//				thread.ThreadNumber = threads.size();
			}
		}
	}
	
	public void run() {
		while (!this.isInterrupted() && !kill) {
			try {
				handle();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try{
//				CPUUsage = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
//				packetsProcessed++;
				
//				System.out.println("Thread #" + ThreadNumber + ": " + CPUUsage/packetsProcessed);
			}catch(Exception e){}
		}
	}
	
	private void handle() {
		try {
			ObfuscatedPlayerPacket packet = queue.take();
			Calculations.Obfuscate(packet.packet, packet.player);
		}catch(Exception e){e.printStackTrace();}
	}

	public static void Queue(Packet51MapChunk packet, CraftPlayer player)
	{
		while(true)
		{
			try {
				queue.put(new ObfuscatedPlayerPacket(player, packet));
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}