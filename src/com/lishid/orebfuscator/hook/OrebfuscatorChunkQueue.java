package com.lishid.orebfuscator.hook;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;

import net.minecraft.server.v1_4_5.*;

public class OrebfuscatorChunkQueue extends LinkedList<ChunkCoordIntPair>
{
    private static final long serialVersionUID = -1928681564741152336L;
    List<ChunkCoordIntPair> internalQueue = Collections.synchronizedList(new LinkedList<ChunkCoordIntPair>());
    List<ChunkCoordIntPair> outputQueue = Collections.synchronizedList(new LinkedList<ChunkCoordIntPair>());
    List<ChunkCoordIntPair> processingQueue = Collections.synchronizedList(new LinkedList<ChunkCoordIntPair>());
    
    CraftPlayer player;
    
    
    Thread thread;
    AtomicBoolean kill = new AtomicBoolean(false);
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public OrebfuscatorChunkQueue(CraftPlayer player2, List previousEntries)
    {
        this.player = player2;
        internalQueue.addAll(previousEntries);
    }
    
    // Called when the queue should be cleared
    @Override
    public void clear()
    {
        // Clear the internal queue
        internalQueue.clear();
        // Cancel processing of any queue'd packets
        super.clear();
    }
    
    // Called when new chunks are queued
    @Override
    public boolean add(ChunkCoordIntPair e)
    {
        // Move everything into the internal queue
        return internalQueue.add(e);
        // return super.add(e);
    }
    
    // Called when the list should be sorted
    @Override
    public Object[] toArray()
    {
        // Sort the internal array according to CB - See PlayerManager.movePlayer(EntityPlayer entityplayer)
        final int x = player.getLocation().getChunk().getX();
        final int z = player.getLocation().getChunk().getZ();
        java.util.Collections.sort(internalQueue, new java.util.Comparator<ChunkCoordIntPair>()
        {
            public int compare(ChunkCoordIntPair a, ChunkCoordIntPair b)
            {
                return Math.max(Math.abs(a.x - x), Math.abs(a.z - z)) - Math.max(Math.abs(b.x - x), Math.abs(b.z - z));
            }
        });
        
        // Return the old array to be sorted
        return internalQueue.toArray();
    }
    
    // This checks for chunks in the queue
    @Override
    public boolean contains(Object o)
    {
        // Checks whether the coords are actually in
        return internalQueue.contains(o) || processingQueue.contains(o);
    }
    
    @Override
    public boolean isEmpty()
    {
        continueProcessing();
        return true;
    }
    
    private void continueProcessing()
    {
        if(player.getHandle().netServerHandler.disconnected)
        {
            internalQueue.clear();
        }
        
        // Chunk packet already queue'd out
        while (!outputQueue.isEmpty())
        {
            ChunkCoordIntPair chunk = outputQueue.remove(0);
            
            @SuppressWarnings("rawtypes")
            List tileEntities = ((WorldServer) player.getHandle().world).getTileEntities(chunk.x * 16, 0, chunk.z * 16, chunk.x * 16 + 16, 256, chunk.z * 16 + 16);
            
            for (Object o : tileEntities)
            {
                this.updateTileEntity((TileEntity) o);
            }
            
            player.getHandle().p().getTracker().a(player.getHandle(), player.getHandle().p().getChunkAt(chunk.x, chunk.z));
        }
        
        // Queue next chunk packet out
        if (processingQueue.isEmpty() && !internalQueue.isEmpty())
        {
            List<Chunk> chunks = new LinkedList<Chunk>();
            
            while (!internalQueue.isEmpty() && chunks.size() < 5)
            {
                ChunkCoordIntPair chunkcoordintpair = internalQueue.remove(0);
                
                if (chunkcoordintpair != null && player.getHandle().world.isLoaded(chunkcoordintpair.x << 4, 0, chunkcoordintpair.z << 4))
                {
                    processingQueue.add(chunkcoordintpair);
                    chunks.add(player.getHandle().world.getChunkAt(chunkcoordintpair.x, chunkcoordintpair.z));
                }
            }
            
            if (!chunks.isEmpty())
            {
                ChunkProcessingThread.Queue(new Packet56MapChunkBulk(chunks), player, this);
            }
        }
    }
    
    private void updateTileEntity(TileEntity tileentity)
    {
        if (tileentity != null)
        {
            Packet packet = tileentity.getUpdatePacket();
            
            if (packet != null)
            {
                player.getHandle().netServerHandler.sendPacket(packet);
            }
        }
    }
    
    @Override
    public ListIterator<ChunkCoordIntPair> listIterator()
    {
        return new FakeIterator();
    }

    public void FinishedProcessing(Packet56MapChunkBulk packet)
    {
        player.getHandle().netServerHandler.sendPacket(packet);
        outputQueue.addAll(processingQueue);
        processingQueue.clear();
    }

    private class FakeIterator implements ListIterator<ChunkCoordIntPair> {

        @Override
        public boolean hasNext()
        {
            return false;
        }

        @Override
        public ChunkCoordIntPair next()
        {
            return null;
        }

        @Override
        public boolean hasPrevious()
        {
            return false;
        }

        @Override
        public ChunkCoordIntPair previous()
        {
            return null;
        }

        @Override
        public int nextIndex()
        {
            return 0;
        }

        @Override
        public int previousIndex()
        {
            return 0;
        }

        @Override
        public void remove()
        {
        }

        @Override
        public void set(ChunkCoordIntPair e)
        {
        }

        @Override
        public void add(ChunkCoordIntPair e)
        {
        }
    }
}
