package com.lishid.orebfuscator.cache;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.config.ConfigManager;

public class CacheCleaner implements Runnable {

	private final ConfigManager configManager;

	public CacheCleaner(ConfigManager configManager) {
		this.configManager = configManager;
	}

	@Override
	public void run() {
		if (!this.configManager.getConfig().isEnabled() || this.configManager.getConfig().getDeleteCacheFilesAfterDays() <= 0) {
			return;
		}

		int count = 0;

		for (World world : Bukkit.getWorlds()) {
			File cacheFolder = new File(ObfuscatedDataCache.getCacheFolder(), world.getName());
			count += ObfuscatedDataCache.deleteFiles(cacheFolder, this.configManager.getConfig().getDeleteCacheFilesAfterDays());
		}

		Orebfuscator.log("Cache cleaner completed, deleted files: " + count);
	}
}
