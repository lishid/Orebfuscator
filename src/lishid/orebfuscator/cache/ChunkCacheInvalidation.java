package lishid.orebfuscator.cache;

import java.util.HashSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import lishid.orebfuscator.Orebfuscator;

import org.bukkit.block.Block;

public class ChunkCacheInvalidation extends Thread implements Runnable
{
    private static final int QUEUE_CAPACITY = 1024 * 10;
    private static final LinkedBlockingDeque<Block> queue = new LinkedBlockingDeque<Block>(QUEUE_CAPACITY);
    private static ChunkCacheInvalidation thread;
    public static HashSet<Long> invalidChunks = new HashSet<Long>();
    public static Object invalidationLock = new Object();
    
    public static void terminate()
    {
        if(thread != null)
            thread.kill.set(true);
    }

    public static void Queue(Block block)
    {
        /*
        if(thread == null || thread.isInterrupted() || !thread.isAlive())
        {
            thread = new ChunkCacheInvalidation();
            thread.setName("Orebfuscator Cache Invalidation Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        */

        synchronized(invalidationLock)
        {
            long chunk = (long)block.getX() << 32 + block.getZ();
            invalidChunks.add(chunk);
        }
        /*
        while(true)
        {
            try {
                //Queue block for later processing
                queue.put(block);
                return;
            }
            catch (Exception e) { Orebfuscator.log(e); }
        }*/
    }

    private AtomicBoolean kill = new AtomicBoolean(false);
    
    public void run() {
        while (!this.isInterrupted() && !kill.get()) {
            try {
                //Remove the first block from the queue
                Block block = queue.take();
                //Invalidate the block
                synchronized(invalidationLock)
                {
                    long chunk = (long)block.getX() << 32 + block.getZ();
                    invalidChunks.add(chunk);
                }
            }
            catch (Exception e) { Orebfuscator.log(e); }
        }
    }
}
