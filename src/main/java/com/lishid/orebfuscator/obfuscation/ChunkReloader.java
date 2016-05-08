package com.lishid.orebfuscator.obfuscation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.server.v1_9_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_9_R1.PlayerChunk;
import net.minecraft.server.v1_9_R1.PlayerChunkMap;
import net.minecraft.server.v1_9_R1.WorldServer;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.internal.ChunkCoord;

public class ChunkReloader extends Thread implements Runnable {
    private static final Map<World, HashSet<ChunkCoord>> loadedChunks = new WeakHashMap<World, HashSet<ChunkCoord>>();
    private static final Map<World, HashSet<ChunkCoord>> unloadedChunks = new WeakHashMap<World, HashSet<ChunkCoord>>();

    private static ChunkReloader thread = new ChunkReloader();

    private long lastExecute = System.currentTimeMillis();
    private AtomicBoolean kill = new AtomicBoolean(false);

    public static void Load() {
        if (thread == null || thread.isInterrupted() || !thread.isAlive()) {
            thread = new ChunkReloader();
            thread.setName("Orebfuscator ChunkReloader Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    public static void terminate() {
        if (thread != null) {
            thread.kill.set(true);
        }
    }

    public void run() {
        HashSet<ChunkCoord> loadedChunksForProcess = new HashSet<ChunkCoord>();
    	HashSet<ChunkCoord> unloadedChunksForProcess = new HashSet<ChunkCoord>();
        Map<World, HashSet<ChunkCoord>> chunksForReload = new WeakHashMap<World, HashSet<ChunkCoord>>();
        ArrayList<World> localWorldsToCheck = new ArrayList<World>();
        ArrayList<ChunkCoord> reloadedChunks = new ArrayList<ChunkCoord>();

        while (!this.isInterrupted() && !kill.get()) {
            try {
                // Wait until necessary
                long timeWait = lastExecute + OrebfuscatorConfig.ChunkReloaderRate - System.currentTimeMillis();
                lastExecute = System.currentTimeMillis();
                if (timeWait > 0) {
                    Thread.sleep(timeWait);
                }
                
                synchronized (loadedChunks) {
                	localWorldsToCheck.addAll(loadedChunks.keySet());
                }
                
                for(World world : localWorldsToCheck) {
                	HashSet<ChunkCoord> chunksForReloadForWorld = chunksForReload.get(world); 
                	if(chunksForReloadForWorld == null) {
                		chunksForReload.put(world, chunksForReloadForWorld = new HashSet<ChunkCoord>());
                	}

                	synchronized (unloadedChunks) {
                    	HashSet<ChunkCoord> unloadedChunksForWorld = unloadedChunks.get(world);
                    	
                    	if(unloadedChunksForWorld != null && !unloadedChunksForWorld.isEmpty()) {
	                    	unloadedChunksForProcess.addAll(unloadedChunksForWorld);
	                    	unloadedChunksForWorld.clear();
                    	}
                    }
                    
                	for(ChunkCoord unloadedChunk : unloadedChunksForProcess) {
                		chunksForReloadForWorld.remove(unloadedChunk);
                	}
                	
                    unloadedChunksForProcess.clear();
                    
                    synchronized (loadedChunks) {
                    	HashSet<ChunkCoord> loadedChunksForWorld = loadedChunks.get(world); 
                    	
                    	if(loadedChunksForWorld != null && !loadedChunksForWorld.isEmpty()) {
                    		loadedChunksForProcess.addAll(loadedChunksForWorld);
	                    	loadedChunksForWorld.clear();
                    	}
                    }
                	
                    for(ChunkCoord loadedChunk : loadedChunksForProcess) {
                    	ChunkCoord chunk1 = new ChunkCoord(loadedChunk.x - 1, loadedChunk.z);
                    	ChunkCoord chunk2 = new ChunkCoord(loadedChunk.x + 1, loadedChunk.z);
                    	ChunkCoord chunk3 = new ChunkCoord(loadedChunk.x, loadedChunk.z - 1);
                    	ChunkCoord chunk4 = new ChunkCoord(loadedChunk.x, loadedChunk.z + 1);
                    	
                    	chunksForReloadForWorld.add(chunk1);
                    	chunksForReloadForWorld.add(chunk2);
                    	chunksForReloadForWorld.add(chunk3);
                    	chunksForReloadForWorld.add(chunk4);
                    }
                    
                    loadedChunksForProcess.clear();
                    
                    if(!chunksForReloadForWorld.isEmpty()) {
                    	reloadChunks(world, chunksForReloadForWorld, reloadedChunks);                    	
                    	
               			chunksForReloadForWorld.removeAll(reloadedChunks);
               			reloadedChunks.clear();
                    }
                }
                
                localWorldsToCheck.clear();
            } catch (Exception e) {
                Orebfuscator.log(e);
            }
        }
    }
    
    private static void reloadChunks(
    		World world,
    		HashSet<ChunkCoord> chunksForReloadForWorld,
    		ArrayList<ChunkCoord> reloadedChunks
    		)
    {
    	WorldServer worldServer = ((CraftWorld)world).getHandle();
    	PlayerChunkMap chunkMap = worldServer.getPlayerChunkMap();
    	File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), world.getName());
    	
    	for(ChunkCoord chunk : chunksForReloadForWorld) {
    		if(!chunkMap.isChunkInUse(chunk.x, chunk.z)) continue;

    		PlayerChunk playerChunk = chunkMap.b(chunk.x, chunk.z);
    		if(playerChunk == null || playerChunk.chunk == null || !playerChunk.chunk.isReady()) continue;
    		
    		reloadedChunks.add(chunk);
    		
    		if(OrebfuscatorConfig.UseCache) {
	    		ObfuscatedCachedChunk cache = new ObfuscatedCachedChunk(cacheFolder, chunk.x, chunk.z);
	    		if(cache.getHash() != 0) continue;
    		}
    		
   			//Orebfuscator.log("Force chunk x = " + chunk.x + ", z = " + chunk.z + " reload for players");/*debug*/
    		
    		playerChunk.a(new PacketPlayOutMapChunk(playerChunk.chunk, true, 0xffff));//Reload chunks for players loaded it
    	}
    }
    
    private static void restart() {
        synchronized (thread) {
            if (thread.isInterrupted() || !thread.isAlive()) {
            	ChunkReloader.Load();
            }
        }
    }

    public static void addLoadedChunk(World world, int chunkX, int chunkZ) {
        restart();
        
        synchronized (loadedChunks) {
        	HashSet<ChunkCoord> chunks = loadedChunks.get(world);
        	
        	if(chunks == null) {
        		loadedChunks.put(world, chunks = new HashSet<ChunkCoord>());
        	}
        	
        	chunks.add(new ChunkCoord(chunkX, chunkZ));
        }
    }

    public static void addUnloadedChunk(World world, int chunkX, int chunkZ) {
        restart();
        
        synchronized (unloadedChunks) {
        	HashSet<ChunkCoord> chunks = unloadedChunks.get(world);
        	
        	if(chunks == null) {
        		unloadedChunks.put(world, chunks = new HashSet<ChunkCoord>());
        	}
        	
        	chunks.add(new ChunkCoord(chunkX, chunkZ));
        }
    }
}
