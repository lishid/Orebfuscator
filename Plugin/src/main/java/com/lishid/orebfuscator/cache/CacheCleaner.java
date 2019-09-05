package com.lishid.orebfuscator.cache;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.logger.OFCLogger;

public class CacheCleaner implements Runnable {
	public void run() {
		if (!Orebfuscator.config.isEnabled() || Orebfuscator.config.getDeleteCacheFilesAfterDays() <= 0)
			return;

		int count = 0;

		for (World world : Bukkit.getWorlds()) {
			File cacheFolder = new File(ObfuscatedDataCache.getCacheFolder(), world.getName());
			count += ObfuscatedDataCache.deleteFiles(cacheFolder, Orebfuscator.config.getDeleteCacheFilesAfterDays());
		}

		OFCLogger.log("Cache cleaner completed, deleted files: " + count);
	}
}
