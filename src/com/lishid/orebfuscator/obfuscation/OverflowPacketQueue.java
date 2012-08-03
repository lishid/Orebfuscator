package com.lishid.orebfuscator.obfuscation;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

import org.bukkit.craftbukkit.ChunkCompressionThread;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class OverflowPacketQueue extends Thread implements Runnable
{
    private static final int QUEUE_CAPACITY = 1024 * 10;
    public static OverflowPacketQueue thread = new OverflowPacketQueue();
    private static final LinkedBlockingDeque<QueuedPacket> queue = new LinkedBlockingDeque<QueuedPacket>(QUEUE_CAPACITY);
    
    public long lastExecute = System.currentTimeMillis();
    public AtomicBoolean kill = new AtomicBoolean(false);
    
    public static void terminate()
    {
        if (thread != null)
        {
            thread.kill.set(true);
        }
    }
    
    public static void Queue(CraftPlayer player, Packet packet)
    {
        if (isOverflowing((NetworkManager) player.getHandle().netServerHandler.networkManager, packet.a()))
        {
            //If overflowing then queue for later processing 
            Queue(new QueuedPacket(player.getHandle(), packet));
        }
        else
        {
            //If not overflowing then send the packet out
            sendOut(player.getHandle(), packet);
        }
    }
    
    public static void sendOut(EntityPlayer player, Packet packet)
    {
        if (packet.lowPriority)
        {
            ChunkCompressionThread.sendPacket(player, packet);
        }
        else
        {
            player.netServerHandler.sendPacket(packet);
        }
    }
    
    public static void Queue(QueuedPacket packet)
    {
        if (thread == null || thread.isInterrupted() || !thread.isAlive())
        {
            thread = new OverflowPacketQueue();
            thread.setName("OverflowPacketMonitor Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        
        while (true)
        {
            try
            {
                queue.put(packet);
                return;
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    public void run()
    {
        while (!this.isInterrupted() && !kill.get())
        {
            try
            {
                // Wait until necessary
                long timeWait = lastExecute + OrebfuscatorConfig.getOverflowPacketCheckRate() - System.currentTimeMillis();
                lastExecute = System.currentTimeMillis();
                if (timeWait > 0)
                {
                    Thread.sleep(timeWait);
                }
                
                int size = queue.size();
                if (size > 0)
                {
                    for (int i = 0; i < size; i++)
                    {
                        QueuedPacket packet = queue.take();
                        if(packet.player.netServerHandler.disconnected)
                            continue;
                        
                        NetworkManager nm = (NetworkManager) packet.player.netServerHandler.networkManager;
                        if (isOverflowing(nm, packet.packet.a()))
                        {
                            //If overflowing, then re-queue the packet
                            Queue(packet);
                        }
                        else
                        {
                            //If not overflowing then send the packet out
                            sendOut(packet.player, packet.packet);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    public static boolean isOverflowing(NetworkManager nm, int size)
    {
        int y = (Integer) CalculationsUtil.getPrivateField(nm, "y");
        if(y > 524288)
            System.out.println("Network size: " + y);
        return y > 524288;
    }
    
    private static class QueuedPacket
    {
        final EntityPlayer player;
        final Packet packet;
        
        QueuedPacket(EntityPlayer player, Packet packet)
        {
            this.player = player;
            this.packet = packet;
        }
    }
}
