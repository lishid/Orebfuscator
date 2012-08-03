package com.lishid.orebfuscator.obfuscation;

import java.lang.reflect.Field;
import java.util.zip.CRC32;

import net.minecraft.server.ChunkProviderServer;
import net.minecraft.server.WorldServer;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

import com.lishid.orebfuscator.Orebfuscator;

public class CalculationsUtil
{
    public static boolean isChunkLoaded(World world, int x, int z)
    {
        return isChunkLoaded(((CraftWorld) world).getHandle(), x, z);
    }
    
    public static boolean isChunkLoaded(WorldServer worldServer, int x, int z)
    {
        if ((worldServer.chunkProvider instanceof ChunkProviderServer) ? ((ChunkProviderServer) worldServer.chunkProvider).chunks.containsKey(x, z) : worldServer.chunkProvider.isChunkLoaded(x, z))
        {
            return true;
        }
        return false;
    }
    
    public static Block getBlockAt(World world, int x, int y, int z)
    {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        if ((worldServer.chunkProvider instanceof ChunkProviderServer) ? ((ChunkProviderServer) worldServer.chunkProvider).chunks.containsKey(x >> 4, z >> 4) : worldServer.chunkProvider
                .isChunkLoaded(x >> 4, z >> 4))
        {
            return world.getBlockAt(x, y, z);
        }
        
        return null;
    }
    
    public static long Hash(byte[] data, int length)
    {
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(data, 0, length);
        long hash = crc.getValue();
        return hash;
    }
    
    public static int increment(int current, int max)
    {
        return (current + 1) % max;
    }
    
    public static Object getPrivateField(Object object, String fieldName)
    {
        Field field;
        Object newObject = new int[0];
        try
        {
            field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            newObject = field.get(object);
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
        
        return newObject;
    }
}
