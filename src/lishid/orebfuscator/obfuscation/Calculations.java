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

package lishid.orebfuscator.obfuscation;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.CRC32;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.OrebfuscatorConfig;
import lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import lishid.orebfuscator.proximityhider.ProximityHider;
import lishid.orebfuscator.utils.MemoryManager;

import net.minecraft.server.ChunkProviderServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.TileEntity;
import net.minecraft.server.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.ChunkCompressionThread;
import org.bukkit.craftbukkit.CraftWorld;

public class Calculations
{
    public static void UpdateBlocksNearby(Block block)
    {
        HashSet<Block> blocks = Calculations.GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.getUpdateRadius());
        
        HashSet<CraftPlayer> players = new HashSet<CraftPlayer>();
        
        List<Player> playerList = getPlayers(block.getWorld());
        
        for (Player player : playerList)
        {
            double dx = Math.abs(player.getLocation().getX() - block.getX());
            double dz = Math.abs(player.getLocation().getZ() - block.getZ());
            double dist = Bukkit.getServer().getViewDistance() * 16;
            if (dx < dist && dz < dist)
            {
                players.add((CraftPlayer) player);
            }
        }
        
        blocks.remove(block);
        
        for (Block nearbyBlock : blocks)
        {
            Calculations.UpdateBlock(nearbyBlock, players);
        }
    }
    
    public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown)
    {
        if (block == null)
            return allBlocks;
        
        AddBlockCheck(allBlocks, block);
        
        if (countdown == 0)
            return allBlocks;
        
        GetAjacentBlocks(world, allBlocks, getBlockAt(world, block.getX() + 1, block.getY(), block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, getBlockAt(world, block.getX() - 1, block.getY(), block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, getBlockAt(world, block.getX(), block.getY() + 1, block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, getBlockAt(world, block.getX(), block.getY() - 1, block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, getBlockAt(world, block.getX(), block.getY(), block.getZ() + 1), countdown - 1);
        GetAjacentBlocks(world, allBlocks, getBlockAt(world, block.getX(), block.getY(), block.getZ() - 1), countdown - 1);
        
        return allBlocks;
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
    
    public static void AddBlockCheck(HashSet<Block> allBlocks, Block block)
    {
        if ((OrebfuscatorConfig.isObfuscated((byte) block.getTypeId()) || OrebfuscatorConfig.isDarknessObfuscated((byte) block.getTypeId())))
        {
            allBlocks.add(block);
        }
    }
    
    public static void UpdateBlock(Block block, HashSet<CraftPlayer> players)
    {
        if (block == null)
            return;
        
        Packet p = null;
        while (true)
        {
            try
            {
                TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(block.getX(), block.getY(), block.getZ());
                if (te != null)
                {
                    p = te.e();
                }
                break;
            }
            catch (Exception e)
            {
            } // ConcurrentModificationException
        }
        
        for (CraftPlayer player : players)
        {
            player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
            
            if (p != null)
            {
                player.getHandle().netServerHandler.sendPacket(p);
            }
        }
    }
    
    public static List<Player> getPlayers(World world)
    {
        List<Player> players = new ArrayList<Player>();
        
        synchronized (Orebfuscator.players)
        {
            for (Player p : Orebfuscator.players.keySet())
            {
                if (p.getWorld().getName().equals(world.getName()))
                    players.add(p);
            }
        }
        
        return players;
    }
    
    public static void Obfuscate(Packet56MapChunkBulk packet, CraftPlayer player, boolean sendPacket, byte[] chunkBuffer)
    {
        
        NetServerHandler nsh = player.getHandle().netServerHandler;
        if (nsh == null || nsh.disconnected || nsh.networkManager == null)
            return;
        
        ChunkInfo[] infos = new ChunkInfo[packet.d()];
        
        int dataStartIndex = 0;
        
        int[] x = (int[]) getPrivateField(packet, "c");
        int[] z = (int[]) getPrivateField(packet, "d");
        
        int[] chunkMask = packet.a;
        int[] extraMask = packet.b;
        
        for (int chunkNum = 0; chunkNum < packet.d(); chunkNum++)
        {
            // Create an info objects
            ChunkInfo info = new ChunkInfo();
            infos[chunkNum] = info;
            info.world = player.getHandle().world.getWorld().getHandle();
            info.player = player;
            info.chunkX = x[chunkNum];
            info.chunkZ = z[chunkNum];
            info.chunkMask = chunkMask[chunkNum];
            info.extraMask = extraMask[chunkNum];
            info.buffer = chunkBuffer;
            info.data = packet.buildBuffer;
            info.startIndex = dataStartIndex;
            
            ComputeChunkInfoAndObfuscate(info, packet.buildBuffer);
            
            dataStartIndex += info.size;
        }
        
        if (sendPacket)
        {
            ChunkCompressionThread.sendPacket(player.getHandle(), packet);
            
            for (ChunkInfo info : infos)
            {
                if (info != null)
                {
                    sendTileEntities(info, nsh);
                }
            }
        }
        
        // Let MemoryManager do its work
        MemoryManager.CheckAndCollect();
    }
    
    public static void Obfuscate(Packet51MapChunk packet, CraftPlayer player, boolean sendPacket, byte[] chunkBuffer)
    {
        NetServerHandler nsh = player.getHandle().netServerHandler;
        if (nsh == null || nsh.disconnected || nsh.networkManager == null)
            return;
        
        // Create an info objects
        ChunkInfo info = new ChunkInfo();
        info.world = player.getHandle().world.getWorld().getHandle();
        info.player = player;
        info.chunkX = packet.a;
        info.chunkZ = packet.b;
        info.chunkMask = packet.c;
        info.extraMask = packet.d;
        info.buffer = chunkBuffer;
        info.data = packet.inflatedBuffer;
        info.startIndex = 0;
        
        ComputeChunkInfoAndObfuscate(info, packet.inflatedBuffer);
        
        if (sendPacket)
        {
            ChunkCompressionThread.sendPacket(player.getHandle(), packet);
            sendTileEntities(info, nsh);
        }
        
        // Let MemoryManager do its work
        MemoryManager.CheckAndCollect();
    }

    public static void ComputeChunkInfoAndObfuscate(ChunkInfo info, byte[] returnData)
    {
        // Compute chunk number
        for (int i = 0; i < 16; i++)
        {
            if ((info.chunkMask & 1 << i) > 0)
            {
                info.chunkSectionToIndexMap[i] = info.chunkSectionNumber;
                info.chunkSectionNumber++;
            }
            else
            {
                info.chunkSectionToIndexMap[i] = -1;
            }
            if ((info.extraMask & 1 << i) > 0)
            {
                info.extraSectionToIndexMap[i] = info.extraSectionNumber;
                info.extraSectionNumber++;
            }
        }
        
        info.size = 2048 * (5 * info.chunkSectionNumber + info.extraSectionNumber) + 256;
        info.blockSize = 4096 * info.chunkSectionNumber;

        // Obfuscate
        if (info.world.getWorld().getEnvironment() == Environment.NORMAL && // Is overworld
                !OrebfuscatorConfig.isWorldDisabled(info.world.getWorld().getName()) && // World not
                OrebfuscatorConfig.obfuscateForPlayer(info.player) && // Should the player have obfuscation?
                OrebfuscatorConfig.getEnabled() && // Plugin enabled
                isChunkLoaded(info.world, info.chunkX, info.chunkZ))// Make sure the chunk is loaded
        {
            byte[] obfuscated = Obfuscate(info);
            // Copy the data out of the buffer
            System.arraycopy(obfuscated, 0, returnData, info.startIndex, info.blockSize);
        }
    }
    
    public static byte[] Obfuscate(ChunkInfo info)
    {
        // Used for caching
        ObfuscatedCachedChunk cache = null;
        // Hash used to check cache consistency
        long hash = 0L;
        // Blocks kept track for ProximityHider
        ArrayList<Block> proximityBlocks = new ArrayList<Block>();
        // Start with caching false
        info.useCache = false;
        
        int initialRadius = OrebfuscatorConfig.getInitialRadius();
        
        // Expand buffer if not enough space
        if (info.blockSize > info.buffer.length)
        {
            info.buffer = new byte[info.blockSize];
        }
        // Copy data into buffer
        System.arraycopy(info.data, info.startIndex, info.buffer, 0, info.blockSize);
        
        // Caching
        if (OrebfuscatorConfig.getUseCache())
        {
            // Get cache folder
            File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), info.world.getWorld().getName());
            // Create cache objects
            cache = new ObfuscatedCachedChunk(cacheFolder, info.chunkX, info.chunkZ, initialRadius, OrebfuscatorConfig.getUseProximityHider());
            info.useCache = true;
            // Hash the chunk
            hash = Hash(info.buffer, info.blockSize);
            
            // Check if hash is consistent
            long storedHash = cache.getHash();
            if (storedHash != 0L && hash == storedHash)
            {
                // Get data
                cache.getDataAndProximityList();
                byte[] data = cache.data;
                int[] chestList = cache.proximityBlockList;
                if (data != null)
                {
                    // Decrypt chest list
                    if (chestList != null)
                    {
                        for (int i = 0; i < chestList.length; i += 3)
                        {
                            Block b = info.player.getWorld().getBlockAt(chestList[i], chestList[i + 1], chestList[i + 2]);
                            proximityBlocks.add(b);
                        }
                    }
                    // ProximityHider add blocks
                    ProximityHider.AddProximityBlocks(info.player, proximityBlocks);
                    
                    // Hash match, use the cached data instead
                    System.arraycopy(data, 0, info.buffer, 0, data.length);
                    // Skip calculations
                    return info.buffer;
                }
            }
        }
        
        // Track of pseudo-randomly assigned randomBlock
        int randomIncrement = 0;
        int randomIncrement2 = 0;
        // Track of whether a block should be obfuscated or not
        boolean obfuscate = false;
        // Track of whether blocks needs special treatment for ProximityHider
        boolean specialObfuscate = false;
        
        int engineMode = OrebfuscatorConfig.getEngineMode();
        int maxChance = OrebfuscatorConfig.getAirGeneratorMaxChance();
        int incrementMax = maxChance;
        
        Integer[] randomBlocks = OrebfuscatorConfig.getRandomBlocks();
        
        // Loop over 16x16x16 chunks in the 16x256x16 column
        int dataIndexModifier = 0;
        // int extraIndexModifier = 0;
        // int extraIndexStart = totalChunks * (4096 + 2048 + 2048 + 2048);
        int startX = info.chunkX << 4;
        int startZ = info.chunkZ << 4;
        for (int i = 0; i < 16; i++)
        {
            // If the bitmask indicates this chunk is sent...
            if ((info.chunkMask & 1 << i) > 0)
            {
                int indexDataStart = dataIndexModifier * 4096;
                // boolean useExtraData = (info.chunkExtra & 1 << i) > 0;
                // int indexExtraStart = extraIndexModifier * 2048;
                
                int tempIndex = 0;
                
                for (int y = 0; y < 16; y++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        OrebfuscatorConfig.shuffleRandomBlocks();
                        incrementMax = (maxChance + OrebfuscatorConfig.random(maxChance)) / 2;
                        for (int x = 0; x < 16; x++)
                        {
                            int index = indexDataStart + tempIndex;
                            byte data = info.data[info.startIndex + index];
                            /*
                             * byte extra = 0;
                             * if(useExtraData)
                             * {
                             * if(tempIndex % 2 == 0)
                             * extra = (byte) (info.data[extraIndexStart + indexExtraStart + (tempIndex >> 1)] & 0x0F);
                             * else
                             * extra = (byte) (info.data[extraIndexStart + indexExtraStart + (tempIndex >> 1)] >> 4);
                             * }
                             */
                            
                            // Initialize data
                            obfuscate = false;
                            specialObfuscate = false;
                            
                            // Check if the block should be obfuscated for the default engine modes
                            if (OrebfuscatorConfig.isObfuscated(data))
                            {
                                if (initialRadius == 0)
                                {
                                    // Obfuscate all blocks
                                    obfuscate = true;
                                }
                                else
                                {
                                    // Check if any nearby blocks are transparent
                                    if (!areAjacentBlocksTransparent(info, startX + x, (i << 4) + y, startZ + z, initialRadius))
                                    {
                                        obfuscate = true;
                                    }
                                }
                            }
                            
                            // Check if the block should be obfuscated because of proximity check
                            if (!obfuscate && OrebfuscatorConfig.getUseProximityHider() && OrebfuscatorConfig.isProximityObfuscated(info.data[info.startIndex + index])
                                    && ((i << 4) + y) <= OrebfuscatorConfig.getProximityHiderEnd())
                            {
                                proximityBlocks.add(getBlockAt(info.player.getWorld(), startX + x, (i << 4) + y, startZ + z));
                                obfuscate = true;
                                if (OrebfuscatorConfig.getUseSpecialBlockForProximityHider())
                                    specialObfuscate = true;
                            }
                            
                            // Check if the block should be obfuscated because of the darkness
                            if (!obfuscate && OrebfuscatorConfig.getDarknessHideBlocks() && OrebfuscatorConfig.isDarknessObfuscated(data))
                            {
                                if (initialRadius == 0)
                                {
                                    obfuscate = true;
                                }
                                else if (!areAjacentBlocksBright(info, startX + x, (i << 4) + y, startZ + z, initialRadius))
                                {
                                    obfuscate = true;
                                }
                            }
                            
                            // Check if the block is obfuscated
                            if (obfuscate)
                            {
                                if (specialObfuscate)
                                {
                                    // Proximity hider
                                    info.buffer[index] = (byte) OrebfuscatorConfig.getProximityHiderID();
                                }
                                else if (engineMode == 1)
                                {
                                    // Engine mode 1, replace with stone
                                    info.buffer[index] = 1;
                                    
                                    // Anti texturepack and freecam
                                    if (OrebfuscatorConfig.getAntiTexturePackAndFreecam())
                                    {
                                        randomIncrement2 = increment(randomIncrement2, incrementMax);
                                        // Add random air blocks
                                        if (randomIncrement2 == 0)
                                            info.buffer[index] = 0;
                                    }
                                }
                                else if (engineMode == 2)
                                {
                                    // Ending mode 2, replace with random block
                                    if (randomBlocks.length > 1)
                                        randomIncrement = increment(randomIncrement, randomBlocks.length);
                                    info.buffer[index] = OrebfuscatorConfig.getRandomBlock(randomIncrement);
                                    
                                    // Anti texturepack and freecam
                                    if (OrebfuscatorConfig.getAntiTexturePackAndFreecam())
                                    {
                                        randomIncrement2 = increment(randomIncrement2, incrementMax);
                                        // Add random air blocks and stone blocks
                                        if (randomIncrement2 == 0)
                                            info.buffer[index] = 0;
                                        else if (randomIncrement2 == 1)
                                            info.buffer[index] = 1;
                                    }
                                }
                            }
                            
                            tempIndex++;
                        }
                    }
                }
                
                dataIndexModifier++;
                // if(useExtraData)
                // {
                // extraIndexModifier++;
                // }
            }
        }
        
        ProximityHider.AddProximityBlocks(info.player, proximityBlocks);
        
        // If cache is still allowed
        if (info.useCache)
        {
            // Save cache
            cache.initialRadius = initialRadius;
            int[] chestList = new int[proximityBlocks.size() * 3];
            for (int i = 0; i < proximityBlocks.size(); i++)
            {
                Block b = proximityBlocks.get(i);
                if (b != null)
                {
                    chestList[i * 3] = b.getX();
                    chestList[i * 3 + 1] = b.getY();
                    chestList[i * 3 + 2] = b.getZ();
                }
            }
            cache.Write(hash, info.buffer, chestList);
        }
        
        return info.buffer;
    }
    
    public static boolean areAjacentBlocksTransparent(ChunkInfo info, int x, int y, int z, int countdown)
    {
        byte id = 0;
        boolean foundID = false;
        
        if (y >= info.world.getHeight() || y < 0)
            return true;
        
        int section = info.chunkSectionToIndexMap[y >> 4];
        
        if ((info.chunkMask & (1 << (y >> 4))) > 0 && x >> 4 == info.chunkX && z >> 4 == info.chunkZ)
        {
            int cX = x % 16;
            if (cX < 0)
                cX += 16;
            int cZ = z % 16;
            if (cZ < 0)
                cZ += 16;
            int index = section * 4096 + (y % 16 << 8) + (cZ << 4) + cX;
            try
            {
                id = info.data[info.startIndex + index];
                foundID = true;
            }
            catch (Exception e)
            {
            }
        }
        
        if (!foundID)
        {
            if (isChunkLoaded(info.world, x >> 4, z >> 4))
            {
                id = (byte) info.world.getTypeId(x, y, z);
            }
            else
            {
                id = 1;
                info.useCache = false;
            }
        }
        
        if (OrebfuscatorConfig.isBlockTransparent(id))
        {
            return true;
        }
        
        if (countdown == 0)
            return false;
        
        if (areAjacentBlocksTransparent(info, x, y + 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, x, y - 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, x + 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, x - 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, x, y, z + 1, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, x, y, z - 1, countdown - 1))
            return true;
        
        return false;
    }
    
    public static boolean areAjacentBlocksBright(ChunkInfo info, int x, int y, int z, int countdown)
    {
        if (isChunkLoaded(info.world, x >> 4, z >> 4))
        {
            if (info.world.getLightLevel(x, y, z) > 0)
            {
                return true;
            }
        }
        else
        {
            return true;
        }
        
        if (countdown == 0)
            return false;
        
        if (areAjacentBlocksBright(info, x, y + 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksBright(info, x, y - 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksBright(info, x + 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksBright(info, x - 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksBright(info, x, y, z + 1, countdown - 1))
            return true;
        if (areAjacentBlocksBright(info, x, y, z - 1, countdown - 1))
            return true;
        
        return false;
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
    
    public static void sendTileEntities(ChunkInfo info, NetServerHandler nsh)
    {
        // Send TileEntities
        int i = info.chunkX * 16;
        int j = info.chunkZ * 16;
        for (int k = 0; k < 16; ++k)
        {
            if ((info.chunkMask & 1 << k) != 0)
            {
                int l = k << 4;
                try
                {
                    Object[] list = info.world.getTileEntities(i, l, j, i + 16, l + 16, j + 16).toArray();
                    
                    for (int i1 = 0; i1 < list.length; i1++)
                    {
                        TileEntity tileentity = (TileEntity) list[i1];
                        if (tileentity != null)
                        {
                            Packet p = tileentity.e();
                            if (p != null)
                            {
                                nsh.sendPacket(p);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Orebfuscator.log("Get Minecraft entity list error: " + e.getMessage());
                }
            }
        }
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