/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IChunkCache;
import com.lishid.orebfuscator.internal.InternalAccessor;

public class ObfuscatedDataCache {
    private static IChunkCache internalCache;

    private static IChunkCache getInternalCache() {
        if (internalCache == null) {
            internalCache = InternalAccessor.Instance.newChunkCache();
        }
        return internalCache;
    }

    public static void clearCache() {
        getInternalCache().clearCache();
    }

    public static DataInputStream getInputStream(File folder, int x, int z) {
        return getInternalCache().getInputStream(folder, x, z);
    }

    public static DataOutputStream getOutputStream(File folder, int x, int z) {
        return getInternalCache().getOutputStream(folder, x, z);
    }

    public static void ClearCache() {
        getInternalCache().clearCache();
        try {
            DeleteDir(OrebfuscatorConfig.getCacheFolder());
        }
        catch (Exception e) {
            Orebfuscator.log(e);
        }
    }

    private static void DeleteDir(File dir) {
        try {
            if (!dir.exists())
                return;

            if (dir.isDirectory())
                for (File f : dir.listFiles())
                    DeleteDir(f);

            dir.delete();
        }
        catch (Exception e) {
            Orebfuscator.log(e);
        }
    }
}