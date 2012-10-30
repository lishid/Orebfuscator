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

package com.lishid.orebfuscator.obfuscation;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.TileEntity;
import net.minecraft.server.WorldServer;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.proximityhider.ProximityHider;
import com.lishid.orebfuscator.threading.ChunkCompressionThread;
import com.lishid.orebfuscator.utils.MemoryManager;

public class Calculations
{
    public static void Obfuscate(Packet56MapChunkBulk packet, CraftPlayer player, boolean sendPacket, byte[] chunkBuffer)
    {
        NetServerHandler nsh = player.getHandle().netServerHandler;
        if (nsh == null || nsh.disconnected || nsh.networkManager == null)
            return;
        
        ChunkInfo[] infos = getInfo(packet, player);
        
        for (int chunkNum = 0; chunkNum < infos.length; chunkNum++)
        {
            // Create an info objects
            ChunkInfo info = infos[chunkNum];
            infos[chunkNum] = info;
            info.buffer = chunkBuffer;
            ComputeChunkInfoAndObfuscate(info, packet.buildBuffer);
        }
        
        if (sendPacket)
        {
            // ChunkCompressionThread.sendPacket(player.getHandle(), packet);
            ChunkCompressionThread.Queue(player, packet, infos);
        }
        
        // Let MemoryManager do its work
        MemoryManager.CheckAndCollect();
    }
    
    public static ChunkInfo[] getInfo(Packet56MapChunkBulk packet, CraftPlayer player) {
    	
        ChunkInfo[] infos = new ChunkInfo[packet.d()];
        WorldServer server = player.getHandle().world.getWorld().getHandle();
        
        int dataStartIndex = 0;
        
        int[] x = (int[]) CalculationsUtil.getPrivateField(packet, "c");
        int[] z = (int[]) CalculationsUtil.getPrivateField(packet, "d");
        
        int[] chunkMask = packet.a;
        int[] extraMask = packet.b;
        
        for (int chunkNum = 0; chunkNum < packet.d(); chunkNum++)
        {
            // Create an info objects
            ChunkInfo info = new ChunkInfo();
            infos[chunkNum] = info;
            info.world = server;
            info.player = player;
            info.chunkX = x[chunkNum];
            info.chunkZ = z[chunkNum];
            info.chunkMask = chunkMask[chunkNum];
            info.extraMask = extraMask[chunkNum];
            info.data = packet.buildBuffer;
            info.startIndex = dataStartIndex;
            
            dataStartIndex += info.size;
        }
        
        return infos;
    }
    
    public static ChunkInfo getInfo(Packet51MapChunk packet, CraftPlayer player) {
    	
        // Create an info objects
        ChunkInfo info = new ChunkInfo();
        info.world = player.getHandle().world.getWorld().getHandle();
        info.player = player;
        info.chunkX = packet.a;
        info.chunkZ = packet.b;
        info.chunkMask = packet.c;
        info.extraMask = packet.d;
        info.data = packet.inflatedBuffer;
        info.startIndex = 0;
        return info;
    }
    
    public static void Obfuscate(Packet51MapChunk packet, CraftPlayer player, boolean sendPacket, byte[] chunkBuffer)
    {
        NetServerHandler nsh = player.getHandle().netServerHandler;
        if (nsh == null || nsh.disconnected || nsh.networkManager == null)
            return;
        
        ChunkInfo info = getInfo(packet, player);
        info.buffer = chunkBuffer;
        
        ComputeChunkInfoAndObfuscate(info, packet.inflatedBuffer);
        
        if (sendPacket)
        {
            // ChunkCompressionThread.sendPacket(player.getHandle(), packet);
            ChunkCompressionThread.Queue(player, packet, new ChunkInfo[] { info });
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
        
        if(info.startIndex + info.blockSize > info.data.length)
        {
            return;
        }
        
        // Obfuscate
        if (!OrebfuscatorConfig.isWorldDisabled(info.world.getWorld().getName()) && // World not enabled
                OrebfuscatorConfig.obfuscateForPlayer(info.player) && // Should the player have obfuscation?
                OrebfuscatorConfig.getEnabled() && // Plugin enabled
                CalculationsUtil.isChunkLoaded(info.world, info.chunkX, info.chunkZ)) // Make sure the chunk is loaded
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
            hash = CalculationsUtil.Hash(info.buffer, info.blockSize);
            
            // Check if hash is consistent
            cache.Read();
            
            long storedHash = cache.getHash();
            if (storedHash == hash)
            {
                // Get data
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
        
        int randomBlocksLength = OrebfuscatorConfig.getRandomBlocks(false).length;
        boolean randomAlternate = false;
        
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
                
                OrebfuscatorConfig.shuffleRandomBlocks();
                for (int y = 0; y < 16; y++)
                {
                    for (int z = 0; z < 16; z++)
                    {
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
                                proximityBlocks.add(CalculationsUtil.getBlockAt(info.player.getWorld(), startX + x, (i << 4) + y, startZ + z));
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
                                else
                                {
                                    randomIncrement2 = CalculationsUtil.increment(randomIncrement2, incrementMax);
                                    
                                    if (engineMode == 1)
                                    {
                                        // Engine mode 1, replace with stone
                                        info.buffer[index] = 1;
                                    }
                                    else if (engineMode == 2)
                                    {
                                        // Ending mode 2, replace with random block
                                        if (randomBlocksLength > 1)
                                            randomIncrement = CalculationsUtil.increment(randomIncrement, randomBlocksLength);
                                        info.buffer[index] = OrebfuscatorConfig.getRandomBlock(randomIncrement, randomAlternate);
                                        randomAlternate = !randomAlternate;
                                    }
                                    // Anti texturepack and freecam
                                    if (OrebfuscatorConfig.getAntiTexturePackAndFreecam())
                                    {
                                        // Add random air blocks
                                        if (randomIncrement2 == 0)
                                            info.buffer[index] = 0;
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
            int cX = ((x % 16) < 0) ? (x % 16 + 16) : (x % 16);
            int cZ = ((z % 16) < 0) ? (z % 16 + 16) : (z % 16);
            
            int index = section * 4096 + (y % 16 << 8) + (cZ << 4) + cX;
            try
            {
                id = info.data[info.startIndex + index];
                foundID = true;
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
        
        if (!foundID)
        {
            if (CalculationsUtil.isChunkLoaded(info.world, x >> 4, z >> 4))
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
        if (CalculationsUtil.isChunkLoaded(info.world, x >> 4, z >> 4))
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
    
    @SuppressWarnings("unchecked")
    public static void sendTileEntities(ChunkInfo info)
    {
        // Send TileEntities
        int x = info.chunkX * 16;
        int z = info.chunkZ * 16;
        try
        {
            @SuppressWarnings("rawtypes")
            ArrayList tileEntitiesList = new ArrayList();
            tileEntitiesList.addAll(info.world.tileEntityList);
            
            @SuppressWarnings("rawtypes")
            Iterator iterator = tileEntitiesList.iterator();
            
            while (iterator.hasNext())
            {
                try
                {
                    Object o = iterator.next();
                    if (o == null)
                    {
                        continue;
                    }
                    TileEntity tileentity = (TileEntity) o;
                    if (tileentity.x >= x && tileentity.z >= z && tileentity.x < x + 16 && tileentity.z < z + 16)
                    {
                        if (tileentity != null)
                        {
                            Packet p = ((TileEntity) tileentity).l();
                            if (p != null)
                            {
                                info.player.getHandle().netServerHandler.sendPacket(p);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
    }
}