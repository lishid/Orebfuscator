/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.orebfuscator.cache.CacheCleaner;
import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.chunkmap.ChunkMapBuffer;
import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.config.ConfigManager;
import com.lishid.orebfuscator.config.OrebfuscatorConfig;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.hook.ProtocolLibHook;
import com.lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;
import com.lishid.orebfuscator.logger.OFCLogger;

/**
 * Orebfuscator Anti X-RAY
 *
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin {

	public static Orebfuscator instance;
	public static OrebfuscatorConfig config;
	public static ConfigManager configManager;

	private boolean isProtocolLibFound;

	@Override
	public void onEnable() {
		Orebfuscator.instance = this;

		// Calling the class to initialize and returning with error message when no version is available
		if (NmsInstance.get() == null)
			return;

		ChunkMapBuffer.init(NmsInstance.get().getBitsPerBlock());

		// Load configurations
		this.loadOrebfuscatorConfig();
		this.makeConfigExample();

		PluginManager pluginManager = Bukkit.getPluginManager();
		this.isProtocolLibFound = pluginManager.getPlugin("ProtocolLib") != null;

		if (!this.isProtocolLibFound) {
			OFCLogger.log("ProtocolLib is not found! Plugin cannot be enabled.");
			return;
		}

		// Orebfuscator events
		pluginManager.registerEvents(new OrebfuscatorPlayerListener(), this);
		pluginManager.registerEvents(new OrebfuscatorEntityListener(), this);
		pluginManager.registerEvents(new OrebfuscatorBlockListener(), this);

		// Orebfuscator commands
		getCommand("ofc").setExecutor(new OrebfuscatorCommandExecutor());

		new ProtocolLibHook(this).register();

		// Run CacheCleaner
		getServer().getScheduler().runTaskTimerAsynchronously(this, new CacheCleaner(), 0, Orebfuscator.config.getCacheCleanRate());
	}

	@Override
	public void onDisable() {
		ObfuscatedDataCache.closeCacheFiles();
		BlockHitManager.clearAll();
		getServer().getScheduler().cancelTasks(this);
	}

	public void runTask(Runnable task) {
		if (this.isEnabled()) {
			getServer().getScheduler().runTask(this, task);
		}
	}

	public boolean getIsProtocolLibFound() {
		return this.isProtocolLibFound;
	}

	public void loadOrebfuscatorConfig() {
		if (Orebfuscator.config == null) {
			Orebfuscator.config = new OrebfuscatorConfig();
			Orebfuscator.configManager = new ConfigManager(this, OFCLogger.logger, Orebfuscator.config);
		}

		Orebfuscator.configManager.load();

		ObfuscatedDataCache.resetCacheFolder();

		NmsInstance.get().setMaxLoadedCacheFiles(Orebfuscator.config.getMaxLoadedCacheFiles());

		// Make sure cache is cleared if config was changed since last start
		try {
			ObfuscatedDataCache.checkCacheAndConfigSynchronized();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void makeConfigExample() {
		File outputFile = new File(getDataFolder(), "config.example_enabledworlds.yml");

		if (outputFile.exists())
			return;

		InputStream configStream = Orebfuscator.class.getResourceAsStream("/resources/config.example_enabledworlds.yml");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(configStream));
				PrintWriter writer = new PrintWriter(outputFile)) {
			String line;

			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reloadOrebfuscatorConfig() {
		this.reloadConfig();
		this.loadOrebfuscatorConfig();
	}
}
