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

package com.lishid.orebfuscator.internal.v1_7_R2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.HashMap;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IChunkCache;

//Volatile
import net.minecraft.server.v1_7_R2.*;

public class ChunkCache implements IChunkCache {
    private static final HashMap<File, RegionFile> cachedRegionFiles = new HashMap<File, RegionFile>();

    private synchronized RegionFile getRegionFile(File folder, int x, int z) {
        File path = new File(folder, "region");
        File file = new File(path, "r." + (x >> 5) + "." + (z >> 5) + ".mcr");
        try {
            RegionFile regionFile = cachedRegionFiles.get(file);
            if (regionFile != null) {
                return regionFile;
            }

            if (!path.exists()) {
                path.mkdirs();
            }

            if (cachedRegionFiles.size() >= OrebfuscatorConfig.MaxLoadedCacheFiles) {
                clearCache();
            }

            regionFile = new RegionFile(file);
            cachedRegionFiles.put(file, regionFile);

            return regionFile;
        }
        catch (Exception e) {
            try {
                file.delete();
            }
            catch (Exception e2) {
                Orebfuscator.log(e);
            }
        }
        return null;
    }

    @Override
    public DataInputStream getInputStream(File folder, int x, int z) {
        RegionFile regionFile = getRegionFile(folder, x, z);
        return regionFile.a(x & 0x1F, z & 0x1F);
    }

    @Override
    public DataOutputStream getOutputStream(File folder, int x, int z) {
        RegionFile regionFile = getRegionFile(folder, x, z);
        return regionFile.b(x & 0x1F, z & 0x1F);
    }

    @Override
    public synchronized void clearCache() {
        for (RegionFile regionFile : cachedRegionFiles.values()) {
            try {
                if (regionFile != null)
                    regionFile.c();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        cachedRegionFiles.clear();
    }
}
