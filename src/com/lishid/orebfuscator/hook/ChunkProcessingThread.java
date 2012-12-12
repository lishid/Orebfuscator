package com.lishid.orebfuscator.hook;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

import net.minecraft.server.v1_4_5.*;

import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.utils.ReflectionHelper;

public class ChunkProcessingThread extends Thread
{
    private static LinkedBlockingDeque<ChunkProcessingOrder> queue = new LinkedBlockingDeque<ChunkProcessingThread.ChunkProcessingOrder>();
    
    private static LinkedList<ChunkProcessingThread> threads = new LinkedList<ChunkProcessingThread>();
    private static int numThreads = Runtime.getRuntime().availableProcessors();
    
    static
    {
        numThreads = Runtime.getRuntime().availableProcessors() - 1;
        if (numThreads < 1)
        {
            numThreads = 1;
        }
    }
    
    static class ChunkProcessingOrder
    {
        Packet56MapChunkBulk packet;
        CraftPlayer player;
        OrebfuscatorChunkQueue output;
        
        public ChunkProcessingOrder(Packet56MapChunkBulk packet, CraftPlayer player, OrebfuscatorChunkQueue output)
        {
            this.packet = packet;
            this.player = player;
            this.output = output;
        }
    }
    
    public synchronized static void KillAll()
    {
        for (ChunkProcessingThread thread : threads)
        {
            thread.kill.set(true);
            thread.interrupt();
        }
        threads.clear();
        queue.clear();
    }
    
    public synchronized static void SyncThreads()
    {
        if (numThreads == threads.size())
        {
            return;
        }
        
        int startThreads = numThreads - threads.size();
        for (int i = 0; i < startThreads; i++)
        {
            ChunkProcessingThread thread = new ChunkProcessingThread();
            thread.setName("Orebfuscator Processing Thread");
            thread.setPriority(MIN_PRIORITY);
            thread.start();
            threads.add(thread);
        }
    }
    
    public static void Queue(Packet56MapChunkBulk packet, CraftPlayer player, OrebfuscatorChunkQueue output)
    {
        SyncThreads();
        try
        {
            queue.put(new ChunkProcessingOrder(packet, player, output));
        }
        catch (InterruptedException e)
        {
            // Should never come here unless there's INT_MAX players online.
        }
    }
    
    static ThreadLocal<Deflater> localDeflater = new ThreadLocal<Deflater>()
    {
        @Override
        protected Deflater initialValue()
        {
            // Use higher compression level!!
            return new Deflater(Deflater.BEST_COMPRESSION);
        }
    };
    
    AtomicBoolean kill = new AtomicBoolean(false);
    
    @Override
    public void run()
    {
        while (!Thread.interrupted() && !kill.get())
        {
            try
            {
                ChunkProcessingOrder order = queue.take();
                Calculations.Obfuscate(order.packet, order.player);
                // order.packet.compress();
                compress(order.packet);
                order.output.FinishedProcessing(order.packet);
            }
            catch (InterruptedException e)
            {
                // If interrupted then exit
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    public void compress(Packet56MapChunkBulk packet)
    {
        
        if (ReflectionHelper.getPrivateField(packet, "buffer") != null)
        {
            return;
        }
        
        byte[] buildBuffer = (byte[]) ReflectionHelper.getPrivateField(packet, "buildBuffer");
        
        Deflater deflater = localDeflater.get();
        deflater.reset();
        deflater.setInput(buildBuffer);
        deflater.finish();
        
        byte[] buffer = new byte[buildBuffer.length + 100];
        
        ReflectionHelper.setPrivateField(packet, "buffer", buffer);
        ReflectionHelper.setPrivateField(packet, "size", deflater.deflate(buffer));
    }
}
