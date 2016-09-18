package com.lishid.orebfuscator.cache;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class CacheCleaner implements Runnable {
    public void run() {
    	if(!OrebfuscatorConfig.Enabled || OrebfuscatorConfig.DeleteCacheFilesAfterDays <= 0) return;
    	
        Orebfuscator.log("Started checking old cache files to delete...");
        
        for(World world : Bukkit.getWorlds()) {
	        File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), world.getName());
	        ObfuscatedDataCache.deleteFiles(cacheFolder, OrebfuscatorConfig.DeleteCacheFilesAfterDays);
        }
        
        Orebfuscator.log("Compleetd checking old cache files to delete...");
    }
}
