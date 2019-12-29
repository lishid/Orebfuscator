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

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.cache.IObfuscatedDataCacheHandler;
import com.lishid.orebfuscator.api.logger.OFCLogger;
import com.lishid.orebfuscator.api.nms.INBT;
import com.lishid.orebfuscator.api.nms.INmsManager;

public class ObfuscatedCachedChunk {

	private final Orebfuscator plugin;
	private final INmsManager nmsManager;
	private final IObfuscatedDataCacheHandler obfuscatedDataCacheHandler;

	private File path;
	private int x;
	private int z;
	public byte[] data;
	public int[] proximityList;
	public int[] removedEntityList;
	public long hash = 0L;
	private boolean loaded = false;

	public ObfuscatedCachedChunk(Orebfuscator plugin, File file, int x, int z) {
		this.plugin = plugin;
		this.x = x;
		this.z = z;
		this.path = new File(file, "data");

		this.nmsManager = this.plugin.getNmsManager();
		this.obfuscatedDataCacheHandler = this.plugin.getObfuscatedDataCacheHandler();

		path.mkdirs();
	}

	public void invalidate() {
		write(0L, new byte[0], new int[0], new int[0]);
	}

	public void free() {
		this.data = null;
		this.proximityList = null;
		this.removedEntityList = null;
	}

	public long getHash() {
		this.read();

		if (!this.loaded)
			return 0L;

		return this.hash;
	}

	public void read() {
		if (this.loaded)
			return;

		try {
			DataInputStream stream = this.obfuscatedDataCacheHandler.getInputStream(this.path, this.x, this.z);
			if (stream != null) {
				INBT nbt = this.nmsManager.createNBT();

				nbt.Read(stream);

				// Check if statuses makes sense
				if (nbt.getInt("X") != this.x || nbt.getInt("Z") != this.z)
					return;

				// Get Hash
				this.hash = nbt.getLong("Hash");

				// Get Data
				this.data = nbt.getByteArray("Data");
				this.proximityList = nbt.getIntArray("ProximityList");
				this.removedEntityList = nbt.getIntArray("RemovedEntityList");
				this.loaded = true;
			}
		} catch (Exception e) {
			OFCLogger.log("Error reading Cache: " + e.getMessage());
			e.printStackTrace();
			this.loaded = false;
		}
	}

	public void write(long hash, byte[] data, int[] proximityList, int[] removedEntityList) {
		try {
			INBT nbt = this.nmsManager.createNBT();
			nbt.reset();

			// Set status indicator
			nbt.setInt("X", x);
			nbt.setInt("Z", z);

			// Set hash
			nbt.setLong("Hash", hash);

			// Set data
			nbt.setByteArray("Data", data);
			nbt.setIntArray("ProximityList", proximityList);
			nbt.setIntArray("RemovedEntityList", removedEntityList);

			DataOutputStream stream = this.obfuscatedDataCacheHandler.getOutputStream(path, x, z);

			nbt.Write(stream);

			try {
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			OFCLogger.log("Error reading Cache: " + e.getMessage());
			e.printStackTrace();
		}
	}
}