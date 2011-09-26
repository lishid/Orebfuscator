package lishid.orebfuscator.utils;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import net.minecraft.server.Packet51MapChunk;

public class OrebfuscatorCalculationThread extends Thread implements Runnable
{
	private static final int QUEUE_CAPACITY = 1024 * 10; // how many packets can be queued before the main thread blocks

	private static final OrebfuscatorCalculationThread instance = new OrebfuscatorCalculationThread();
	private static Thread thread = null;
	private static boolean runs = false;
	private final LinkedBlockingDeque<ObfuscatedPlayerPacket> queue = new LinkedBlockingDeque<ObfuscatedPlayerPacket>(QUEUE_CAPACITY);
	private final AtomicBoolean kill = new AtomicBoolean(false);

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
		while(!instance.queue.isEmpty()) {
			instance.handle();
		}
	}
	
	// consumer thread
	public void run() {
		while (thread != null && !thread.isInterrupted() && !kill.get()) {
			try {
				handle();
			} catch (Exception e) {
				e.printStackTrace(); // print & ignore
			}
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
				instance.queue.put(new ObfuscatedPlayerPacket(player, packet));
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}