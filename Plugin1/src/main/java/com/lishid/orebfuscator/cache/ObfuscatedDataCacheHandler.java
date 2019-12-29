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

package com.lishid.orebfuscator.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.bukkit.Bukkit;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.cache.IObfuscatedDataCacheHandler;
import com.lishid.orebfuscator.api.logger.OFCLogger;
import com.lishid.orebfuscator.api.nms.IChunkCache;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.handler.CraftHandler;
import com.lishid.orebfuscator.utils.FileHelper;

public class ObfuscatedDataCacheHandler extends CraftHandler implements IObfuscatedDataCacheHandler {

	private static final String CACHE_FILE_NAME = "cache_config.yml";

	private INmsManager nmsManager;

	private File cacheFolder;
	private IChunkCache internalCache;

	public ObfuscatedDataCacheHandler(Orebfuscator core) {
		super(core);
	}

	@Override
	public void onInit() {
		this.nmsManager = this.plugin.getNmsManager();
	}

	@Override
	public void onDisable() {
		this.closeCacheFiles();
	}

	private IChunkCache getInternalCache() {
		if (internalCache == null) {
			internalCache = this.nmsManager.createChunkCache();
		}
		return internalCache;
	}

	public void resetCacheFolder() {
		cacheFolder = null;
	}

	public File getCacheFolder() {
		if (cacheFolder == null) {
			cacheFolder = new File(Bukkit.getServer().getWorldContainer(), this.plugin.getConfigHandler().getConfig().getCacheLocation());
		}

		// Try to make the folder
		if (!cacheFolder.exists()) {
			cacheFolder.mkdirs();
		}
		// Can't make folder? Use default
		if (!cacheFolder.exists()) {
			cacheFolder = new File("orebfuscator_cache");
		}
		return cacheFolder;
	}

	public void closeCacheFiles() {
		this.getInternalCache().closeCacheFiles();
	}

	public void checkCacheAndConfigSynchronized() throws IOException {
		String configContent = this.plugin.getConfig().saveToString();

		File cacheFolder = this.getCacheFolder();
		File cacheConfigFile = new File(cacheFolder, ObfuscatedDataCacheHandler.CACHE_FILE_NAME);
		String cacheConfigContent = FileHelper.readFile(cacheConfigFile);

		if (Objects.equals(configContent, cacheConfigContent)) {
			return;
		}

		this.clearCache();
	}

	public void clearCache() throws IOException {
		this.closeCacheFiles();

		File cacheFolder = this.getCacheFolder();
		File cacheConfigFile = new File(cacheFolder, ObfuscatedDataCacheHandler.CACHE_FILE_NAME);

		if (cacheFolder.exists()) {
			FileHelper.delete(cacheFolder);
		}

		OFCLogger.log("Cache cleared.");

		cacheFolder.mkdirs();

		this.plugin.getConfig().save(cacheConfigFile);
	}

	public DataInputStream getInputStream(File folder, int x, int z) throws IOException {
		return this.getInternalCache().getInputStream(folder, x, z);
	}

	public DataOutputStream getOutputStream(File folder, int x, int z) throws IOException {
		return this.getInternalCache().getOutputStream(folder, x, z);
	}

	public int deleteFiles(File folder, int deleteAfterDays) {
		int count = 0;

		try {
			File regionFolder = new File(folder, "data/region");

			if (!regionFolder.exists())
				return count;

			long deleteAfterDaysMs = (long) deleteAfterDays * 86400000L; // 24L * 60L * 60L * 1000L

			for (File file : regionFolder.listFiles()) {
				long diff = new Date().getTime() - file.lastModified();

				if (diff > deleteAfterDaysMs) {
					file.delete();
					count++;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return count;
	}
}