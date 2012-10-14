package com.lishid.orebfuscator.threading;

import java.util.ArrayList;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.obfuscation.CalculationsUtil;

public abstract class OrebfuscatorScheduler {
	
    static class QueuedPacket
    {
        final CraftPlayer player;
        final Packet packet;
        
        QueuedPacket(CraftPlayer player, Packet packet)
        {
            this.player = player;
            this.packet = packet;
        }
    }

    // Start by using the default scheduler
    private static OrebfuscatorScheduler scheduler = new OrebfuscatorSchedulerDefault();

    public static OrebfuscatorScheduler getScheduler() {
		return scheduler;
	}

	public static void setScheduler(OrebfuscatorScheduler scheduler) {
		OrebfuscatorScheduler.scheduler = scheduler;
	}

	protected ArrayList<OrebfuscatorThreadCalculation> threads = new ArrayList<OrebfuscatorThreadCalculation>();
    
    public int getThreads()
    {
        return threads.size();
    }
    
    public void terminateAll()
    {
        for (int i = 0; i < threads.size(); i++)
        {
            threads.get(i).kill();
        }
    }
    
    public void removeThread(OrebfuscatorThreadCalculation thread) {
    	threads.remove(thread);
    }
    
    public synchronized void SyncThreads()
    {
        int extra = threads.size() - OrebfuscatorConfig.getProcessingThreads();
        
        if (extra > 0)
        {
            for (int i = extra - 1; i >= 0; i--)
            {
                threads.get(i).kill();
                threads.remove(i);
            }
        }
        else if (extra < 0)
        {
            extra = -extra;
            for (int i = 0; i < extra; i++)
            {
                OrebfuscatorThreadCalculation thread = createThread();
                thread.setName("Orebfuscator Calculation Thread");
                
                if (OrebfuscatorConfig.getOrebfuscatorPriority() == 0)
                    thread.setPriority(Thread.MIN_PRIORITY);
                if (OrebfuscatorConfig.getOrebfuscatorPriority() == 1)
                    thread.setPriority(Thread.NORM_PRIORITY);
                if (OrebfuscatorConfig.getOrebfuscatorPriority() == 2)
                    thread.setPriority(Thread.MAX_PRIORITY);
                
                thread.start();
                threads.add(thread);
            }
        }
    }
    
    protected abstract OrebfuscatorThreadCalculation createThread();
    
    public abstract void Queue(Packet56MapChunkBulk packet, CraftPlayer player);
    public abstract void Queue(Packet51MapChunk packet, CraftPlayer player);
    
	public boolean isImportant(Packet56MapChunkBulk packet, CraftPlayer player) {
        int[] x = (int[]) CalculationsUtil.getPrivateField(packet, "c");
        int[] z = (int[]) CalculationsUtil.getPrivateField(packet, "d");
       
        for (int i = 0; i < x.length; i++)
        {
            if (Math.abs(x[i] - (((int) player.getLocation().getX()) >> 4)) == 0 && Math.abs(z[i] - (((int) player.getLocation().getZ())) >> 4) == 0)
            {
            	return true;
            }
        }
		
        return false;
	}
	
	public boolean isImportant(Packet51MapChunk packet, CraftPlayer player) {
		return Math.abs(packet.a - (((int) player.getLocation().getX()) >> 4)) == 0 && 
			   Math.abs(packet.b - (((int) player.getLocation().getZ())) >> 4) == 0;
	}
}
