package com.lishid.orebfuscator.cache;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.cache.IObfuscatedDataCacheHandler;
import com.lishid.orebfuscator.api.config.IConfigHandler;
import com.lishid.orebfuscator.api.config.IOrebfuscatorConfig;
import com.lishid.orebfuscator.api.logger.OFCLogger;
import com.lishid.orebfuscator.handler.CraftHandler;

public class CacheCleanerHandler extends CraftHandler implements Runnable {

	private final IConfigHandler configHandler;
	private final IOrebfuscatorConfig config;
	private final IObfuscatedDataCacheHandler obfuscatedDataCacheHandler;

	private BukkitTask task;

	public CacheCleanerHandler(Orebfuscator core) {
		super(core);

		this.configHandler = this.plugin.getConfigHandler();
		this.config = this.configHandler.getConfig();
		this.obfuscatedDataCacheHandler = this.plugin.getObfuscatedDataCacheHandler();
	}

	@Override
	public void onEnable() {
		this.task = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 0, this.plugin.getConfigHandler().getConfig().getCacheCleanRate());
	}

	@Override
	public boolean enableHandler() {
		return this.config.isEnabled() || this.config.getDeleteCacheFilesAfterDays() <= 0;
	}

	@Override
	public void onDisable() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}

	@Override
	public void run() {
		int count = 0;

		for (World world : Bukkit.getWorlds()) {
			File cacheFolder = new File(this.obfuscatedDataCacheHandler.getCacheFolder(), world.getName());
			count += this.obfuscatedDataCacheHandler.deleteFiles(cacheFolder, this.config.getDeleteCacheFilesAfterDays());
		}

		OFCLogger.log("Cache cleaner completed, deleted files: " + count);
	}
}