/**
 * @author lishid
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_14_R1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

import net.minecraft.server.v1_14_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_14_R1.RegionFile;

import com.lishid.orebfuscator.nms.IChunkCache;

public class ChunkCache implements IChunkCache {
    private static final HashMap<File, RegionFile> cachedRegionFiles = new HashMap<File, RegionFile>();
    
    private int maxLoadedCacheFiles;
    
    public ChunkCache(int maxLoadedCacheFiles) {
    	this.maxLoadedCacheFiles = maxLoadedCacheFiles;
    }

    public DataInputStream getInputStream(File folder, int x, int z) {
        RegionFile regionFile = getRegionFile(folder, x, z);
        return regionFile.a(new ChunkCoordIntPair(x & 0x1F, z & 0x1F));
    }

    public DataOutputStream getOutputStream(File folder, int x, int z) {
        RegionFile regionFile = getRegionFile(folder, x, z);
        return regionFile.c(new ChunkCoordIntPair(x & 0x1F, z & 0x1F));
    }
    
    public void closeCacheFiles() {
    	closeCacheFilesInternal();
    }
    
    private synchronized RegionFile getRegionFile(File folder, int x, int z) {
        File path = new File(folder, "region");
        File file = new File(path, "r." + (x >> 5) + "." + (z >> 5) + ".mcr");
        try {
            RegionFile regionFile = cachedRegionFiles.get(file);
            if (regionFile != null) {
                return regionFile;
            }

            if (!path.exists()) {
                path.mkdirs();
            }

            if (cachedRegionFiles.size() >= this.maxLoadedCacheFiles) {
                closeCacheFiles();
            }

            regionFile = new RegionFile(file);
            cachedRegionFiles.put(file, regionFile);

            return regionFile;
        }
        catch (Exception e) {
            try {
                file.delete();
            }
            catch (Exception e2) {
            }
        }
        return null;
    }

    private synchronized void closeCacheFilesInternal() {
        for (RegionFile regionFile : cachedRegionFiles.values()) {
            try {
                if (regionFile != null) {
                	// This lovely piece of work is due to an NMS change in Spigot 1.13.2 without an R increase.
                	try {
                		Method c = regionFile.getClass().getDeclaredMethod("c");
                		c.invoke(regionFile);
                	} catch (NoSuchMethodException nsme) {
                		try {
                			Method close = regionFile.getClass().getDeclaredMethod("close");
                			close.invoke(regionFile);
                		} catch (NoSuchMethodException nsme2) {
                			
                		}
                	}
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        cachedRegionFiles.clear();
    }
}
