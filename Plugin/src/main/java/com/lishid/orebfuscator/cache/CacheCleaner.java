package com.lishid.orebfuscator.cache;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class CacheCleaner implements Runnable {
    public void run() {
    	if(!OrebfuscatorConfig.Enabled || OrebfuscatorConfig.DeleteCacheFilesAfterDays <= 0) return;
    	
        int count = 0;
        
        for(World world : Bukkit.getWorlds()) {
	        File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), world.getName());
	        count += ObfuscatedDataCache.deleteFiles(cacheFolder, OrebfuscatorConfig.DeleteCacheFilesAfterDays);
        }
        
        Orebfuscator.log("Cache cleaner completed, deleted files: " + count);
    }
}
