package lishid.orebfuscator.cache;

import lishid.orebfuscator.utils.OrebfuscatorConfig;
import net.minecraft.server.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;

public class ObfuscatedRegionFileCache {
    private static final HashMap<File, Reference<RegionFile>> cachedRegionFiles = new HashMap<File, Reference<RegionFile>>();

    public static synchronized RegionFile getRegionFile(File folder, int x, int z) {
        File path = new File(folder, "region");
        File file = new File(path, "r." + (x >> 5) + "." + (z >> 5) + ".mcr");

        Reference<RegionFile> reference = cachedRegionFiles.get(file);

        if (reference != null) {
        	RegionFile regionFile = (RegionFile) reference.get();
            if (regionFile != null) {
                return regionFile;
            }
        }

        if (!path.exists()) {
            path.mkdirs();
        }

        if (cachedRegionFiles.size() >= OrebfuscatorConfig.getMaxLoadedCacheFiles()) {
            clearCache();
        }

        RegionFile regionFile = new RegionFile(file);
        cachedRegionFiles.put(file, new SoftReference<RegionFile>(regionFile));
        return regionFile;
    }

    public static synchronized void clearCache() {
        for (Reference<RegionFile> reference: cachedRegionFiles.values()) {
            try {
                RegionFile regionFile = (RegionFile) reference.get();
                if (regionFile != null) regionFile.a();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cachedRegionFiles.clear();
    }

    public static DataInputStream getInputStream(File folder, int x, int z) {
        RegionFile regionFile = getRegionFile(folder, x, z);
        return regionFile.a(x & 0x1F, z & 0x1F);
    }

    public static DataOutputStream getOutputStream(File folder, int x, int z) {
        RegionFile regionFile = getRegionFile(folder, x, z);
        return regionFile.b(x & 0x1F, z & 0x1F);
    }
}