/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.obfuscation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.nms.IChunkManager;
import com.lishid.orebfuscator.types.ChunkCoord;

public class ChunkReloader extends Thread implements Runnable {
    private static final Map<World, HashSet<ChunkCoord>> loadedChunks = new WeakHashMap<World, HashSet<ChunkCoord>>();
    private static final Map<World, HashSet<ChunkCoord>> unloadedChunks = new WeakHashMap<World, HashSet<ChunkCoord>>();
    private static final Map<World, HashSet<ChunkCoord>> chunksForReload = new WeakHashMap<World, HashSet<ChunkCoord>>();

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
        HashSet<ChunkCoord> localLoadedChunks = new HashSet<ChunkCoord>();
    	HashSet<ChunkCoord> localUnloadedChunks = new HashSet<ChunkCoord>();
        Map<World, HashSet<ChunkCoord>> localChunksForReload = new WeakHashMap<World, HashSet<ChunkCoord>>();
        ArrayList<World> localWorldsToCheck = new ArrayList<World>();

        while (!this.isInterrupted() && !kill.get()) {
            try {
                // Wait until necessary
                long timeWait = lastExecute + OrebfuscatorConfig.ChunkReloaderRate - System.currentTimeMillis();
                lastExecute = System.currentTimeMillis();
                if (timeWait > 0) {
                    Thread.sleep(timeWait);
                }
                
                if (!OrebfuscatorConfig.UseChunkReloader) {
                    return;
                }
                
                synchronized (loadedChunks) {
                	localWorldsToCheck.addAll(loadedChunks.keySet());
                }
                
                for(World world : localWorldsToCheck) {
                	HashSet<ChunkCoord> localChunksForReloadForWorld = localChunksForReload.get(world);
                	
                	if(localChunksForReloadForWorld == null) {
                		localChunksForReload.put(world, localChunksForReloadForWorld = new HashSet<ChunkCoord>());
                	}
                	
                	synchronized (chunksForReload) {
                		HashSet<ChunkCoord> chunksForReloadForWorld = chunksForReload.get(world);
                		
                		if(chunksForReloadForWorld != null && !chunksForReloadForWorld.isEmpty()) {
                			localChunksForReloadForWorld.addAll(chunksForReloadForWorld);
                			chunksForReloadForWorld.clear();
                		}
                	}

                	synchronized (unloadedChunks) {
                    	HashSet<ChunkCoord> unloadedChunksForWorld = unloadedChunks.get(world);
                    	
                    	if(unloadedChunksForWorld != null && !unloadedChunksForWorld.isEmpty()) {
	                    	localUnloadedChunks.addAll(unloadedChunksForWorld);
	                    	unloadedChunksForWorld.clear();
                    	}
                    }
                    
                	for(ChunkCoord unloadedChunk : localUnloadedChunks) {
                		localChunksForReloadForWorld.remove(unloadedChunk);
                	}
                	
                    localUnloadedChunks.clear();
                    
                    synchronized (loadedChunks) {
                    	HashSet<ChunkCoord> loadedChunksForWorld = loadedChunks.get(world); 
                    	
                    	if(loadedChunksForWorld != null && !loadedChunksForWorld.isEmpty()) {
                    		localLoadedChunks.addAll(loadedChunksForWorld);
	                    	loadedChunksForWorld.clear();
                    	}
                    }
                	
                    for(ChunkCoord loadedChunk : localLoadedChunks) {
                    	ChunkCoord chunk1 = new ChunkCoord(loadedChunk.x - 1, loadedChunk.z);
                    	ChunkCoord chunk2 = new ChunkCoord(loadedChunk.x + 1, loadedChunk.z);
                    	ChunkCoord chunk3 = new ChunkCoord(loadedChunk.x, loadedChunk.z - 1);
                    	ChunkCoord chunk4 = new ChunkCoord(loadedChunk.x, loadedChunk.z + 1);
                    	
                    	localChunksForReloadForWorld.add(chunk1);
                    	localChunksForReloadForWorld.add(chunk2);
                    	localChunksForReloadForWorld.add(chunk3);
                    	localChunksForReloadForWorld.add(chunk4);
                    }
                    
                    localLoadedChunks.clear();
                    
                    if(!localChunksForReloadForWorld.isEmpty()) {
                    	scheduleReloadChunks(world, localChunksForReloadForWorld);                    	
               			localChunksForReloadForWorld.clear();
                    }
                }
                
                localWorldsToCheck.clear();
            } catch (Exception e) {
                Orebfuscator.log(e);
            }
        }
    }
    
    private static void scheduleReloadChunks(final World world, HashSet<ChunkCoord> chunksForReloadForWorld) {
    	File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), world.getName());
    	final IChunkManager chunkManager = Orebfuscator.nms.getChunkManager(world);    	
    	
    	for(final ChunkCoord chunk : chunksForReloadForWorld) {
    		if(OrebfuscatorConfig.UseCache) {
	    		ObfuscatedCachedChunk cache = new ObfuscatedCachedChunk(cacheFolder, chunk.x, chunk.z);
	    		if(cache.getHash() != 0) continue;
    		}
    		
   			//Orebfuscator.log("Add chunk x = " + chunk.x + ", z = " + chunk.z + " to schedule for reload for players");/*debug*/
    		
    		Orebfuscator.instance.runTask(new Runnable() {
                public void run() {
                	runReloadChunk(world, chunkManager, chunk);
                }
            });
    	}
    }
    
    private static void runReloadChunk(World world, IChunkManager chunkManager, ChunkCoord chunk) {
		//Reload chunk for players
    	HashSet<Player> affectedPlayers = new HashSet<Player>();
    	
		if(!world.isChunkLoaded(chunk.x, chunk.z)) return;
		
		if(!chunkManager.resendChunk(chunk.x, chunk.z, affectedPlayers)) {
	    	synchronized (chunksForReload) {
	    		HashSet<ChunkCoord> chunksForReloadForWorld = chunksForReload.get(world);
	    		
	    		if(chunksForReloadForWorld == null) {
	    			chunksForReload.put(world, chunksForReloadForWorld = new HashSet<ChunkCoord>());
	    		}
	    		
	    		chunksForReloadForWorld.add(chunk);
	    	}

	    	//Orebfuscator.log("Is not possible to reload chunk x = " + chunk.x + ", z = " + chunk.z + ", add for later reload");/*debug*/
		} else {
			//Orebfuscator.log("Force chunk x = " + chunk.x + ", z = " + chunk.z + " to reload for players");/*debug*/
		}
		
		if(affectedPlayers.size() > 0) {
			ProximityHider.addPlayersToReload(affectedPlayers);
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
        if (!OrebfuscatorConfig.Enabled // Plugin enabled
        		|| OrebfuscatorConfig.isWorldDisabled(world.getName())) // World not enabled
        {
            return;
        }
    	
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
        if (!OrebfuscatorConfig.Enabled // Plugin enabled
        		|| OrebfuscatorConfig.isWorldDisabled(world.getName())) // World not enabled
        {
            return;
        }

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
