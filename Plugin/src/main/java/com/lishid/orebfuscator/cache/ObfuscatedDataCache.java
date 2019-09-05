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

import com.lishid.orebfuscator.NmsInstance;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.logger.OFCLogger;
import com.lishid.orebfuscator.nms.IChunkCache;
import com.lishid.orebfuscator.utils.FileHelper;

public class ObfuscatedDataCache {
	private static final String cacheFileName = "cache_config.yml";
	private static File cacheFolder;
	private static IChunkCache internalCache;

	private static IChunkCache getInternalCache() {
		if (internalCache == null) {
			internalCache = NmsInstance.get().createChunkCache();
		}
		return internalCache;
	}

	public static void resetCacheFolder() {
		cacheFolder = null;
	}

	public static File getCacheFolder() {
		if (cacheFolder == null) {
			cacheFolder = new File(Bukkit.getServer().getWorldContainer(), Orebfuscator.config.getCacheLocation());
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

	public static void closeCacheFiles() {
		getInternalCache().closeCacheFiles();
	}

	public static void checkCacheAndConfigSynchronized() throws IOException {
		String configContent = Orebfuscator.instance.getConfig().saveToString();

		File cacheFolder = getCacheFolder();
		File cacheConfigFile = new File(cacheFolder, cacheFileName);
		String cacheConfigContent = FileHelper.readFile(cacheConfigFile);

		if (Objects.equals(configContent, cacheConfigContent))
			return;

		clearCache();
	}

	public static void clearCache() throws IOException {
		closeCacheFiles();

		File cacheFolder = getCacheFolder();
		File cacheConfigFile = new File(cacheFolder, cacheFileName);

		if (cacheFolder.exists()) {
			FileHelper.delete(cacheFolder);
		}

		OFCLogger.log("Cache cleared.");

		cacheFolder.mkdirs();

		Orebfuscator.instance.getConfig().save(cacheConfigFile);
	}

	public static DataInputStream getInputStream(File folder, int x, int z) {
		return getInternalCache().getInputStream(folder, x, z);
	}

	public static DataOutputStream getOutputStream(File folder, int x, int z) {
		return getInternalCache().getOutputStream(folder, x, z);
	}

	public static int deleteFiles(File folder, int deleteAfterDays) {
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