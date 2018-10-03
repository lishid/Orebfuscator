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

import com.lishid.orebfuscator.NmsInstance;
import com.lishid.orebfuscator.nms.INBT;

public class ObfuscatedCachedChunk {
    File path;
    int x;
    int z;
    public byte[] data;
    public int[] proximityList;
    public int[] removedEntityList;
    public long hash = 0L;
    private boolean loaded = false;

    private static final ThreadLocal<INBT> nbtAccessor = new ThreadLocal<INBT>() {
        protected INBT initialValue() {
            return NmsInstance.current.createNBT();
        }
    };

    public ObfuscatedCachedChunk(File file, int x, int z) {
        this.x = x;
        this.z = z;
        this.path = new File(file, "data");
        path.mkdirs();
    }

    public void invalidate() {
        write(0L, new byte[0], new int[0], new int[0]);
    }

    public void free() {
        data = null;
        proximityList = null;
        removedEntityList = null;
    }

    public long getHash() {
        read();

        if (!loaded)
            return 0L;

        return hash;
    }

    public void read() {
        if (loaded)
            return;

        try {
            DataInputStream stream = ObfuscatedDataCache.getInputStream(path, x, z);
            if (stream != null) {
                INBT nbt = nbtAccessor.get();

                nbt.Read(stream);

                // Check if statuses makes sense
                if (nbt.getInt("X") != x || nbt.getInt("Z") != z)
                    return;

                // Get Hash
                hash = nbt.getLong("Hash");

                // Get Data
                data = nbt.getByteArray("Data");
                proximityList = nbt.getIntArray("ProximityList");
                removedEntityList = nbt.getIntArray("RemovedEntityList");
                loaded = true;
            }
        } catch (Exception e) {
            // Orebfuscator.log("Error reading Cache: " + e.getMessage());
            // e.printStackTrace();
            loaded = false;
        }
    }

    public void write(long hash, byte[] data, int[] proximityList, int[] removedEntityList) {
        try {
            INBT nbt = nbtAccessor.get();
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

            DataOutputStream stream = ObfuscatedDataCache.getOutputStream(path, x, z);

            nbt.Write(stream);

            try {
                stream.close();
            } catch (Exception e) {

            }
        } catch (Exception e) {
            // Orebfuscator.log("Error reading Cache: " + e.getMessage());
            // e.printStackTrace();
        }
    }
}