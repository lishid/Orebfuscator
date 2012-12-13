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
import java.util.zip.Deflater;

import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.IPacket56;

public class Calculations
{
    public static final ThreadLocal<byte[]> buffer = new ThreadLocal<byte[]>()
    {
        protected byte[] initialValue()
        {
            return new byte[65536];
        }
    };
    
    public static final ThreadLocal<Deflater> localDeflater = new ThreadLocal<Deflater>()
    {
        @Override
        protected Deflater initialValue()
        {
            // Use higher compression level!!
            return new Deflater(Deflater.DEFAULT_COMPRESSION);
        }
    };
    
    public static void Obfuscate(IPacket56 packet, Player player)
    {
        if (packet.getOutputBuffer() != null)
        {
            return;
        }
        
        ChunkInfo[] infos = getInfo(packet, player);
        
        for (int chunkNum = 0; chunkNum < infos.length; chunkNum++)
        {
            // Create an info objects
            ChunkInfo info = infos[chunkNum];
            info.buffer = buffer.get();
            ComputeChunkInfoAndObfuscate(info, packet.getBuildBuffer());
        }
    }
    
    public static void Obfuscate(IPacket51 packet, Player player)
    {
        Obfuscate(packet, player, true);
    }
    
    public static void Obfuscate(IPacket51 packet, Player player, boolean needCompression)
    {
        ChunkInfo info = getInfo(packet, player);
        info.buffer = buffer.get();
        
        if (info.chunkMask == 0 && info.extraMask == 0)
        {
            return;
        }
        
        ComputeChunkInfoAndObfuscate(info, packet.getBuffer());
        
        if (needCompression)
        {
            Deflater deflater = localDeflater.get();
            packet.compress(deflater);
        }
    }
    
    public static ChunkInfo[] getInfo(IPacket56 packet, Player player)
    {
        ChunkInfo[] infos = new ChunkInfo[packet.getPacketChunkNumber()];
        
        int dataStartIndex = 0;
        
        int[] x = packet.getX();
        int[] z = packet.getZ();
        
        byte[][] inflatedBuffers = packet.getInflatedBuffers();
        
        int[] chunkMask = packet.getChunkMask();
        int[] extraMask = packet.getExtraMask();
        
        for (int chunkNum = 0; chunkNum < packet.getPacketChunkNumber(); chunkNum++)
        {
            // Create an info objects
            ChunkInfo info = new ChunkInfo();
            infos[chunkNum] = info;
            info.world = player.getWorld();
            info.player = player;
            info.chunkX = x[chunkNum];
            info.chunkZ = z[chunkNum];
            info.chunkMask = chunkMask[chunkNum];
            info.extraMask = extraMask[chunkNum];
            info.data = packet.getBuildBuffer();
            info.startIndex = dataStartIndex;
            info.size = inflatedBuffers[chunkNum].length;
            
            dataStartIndex += info.size;
        }
        
        return infos;
    }
    
    public static ChunkInfo getInfo(IPacket51 packet, Player player)
    {
        // Create an info objects
        ChunkInfo info = new ChunkInfo();
        info.world = player.getWorld();
        info.player = player;
        info.chunkX = packet.getX();
        info.chunkZ = packet.getZ();
        info.chunkMask = packet.getChunkMask();
        info.extraMask = packet.getExtraMask();
        info.data = packet.getBuffer();
        info.startIndex = 0;
        return info;
    }
    
    public static void ComputeChunkInfoAndObfuscate(ChunkInfo info, byte[] original)
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
        
        if (info.startIndex + info.blockSize > info.data.length)
        {
            return;
        }
        
        // Obfuscate
        if (!OrebfuscatorConfig.isWorldDisabled(info.world.getName()) && // World not enabled
                OrebfuscatorConfig.obfuscateForPlayer(info.player) && // Should the player have obfuscation?
                OrebfuscatorConfig.getEnabled()) // Plugin enabled
        {
            byte[] obfuscated = Obfuscate(info, original);
            // Copy the data out of the buffer
            System.arraycopy(obfuscated, 0, original, info.startIndex, info.blockSize);
        }
    }
    
    public static byte[] Obfuscate(ChunkInfo info, byte[] original)
    {
        // Used for caching
        ObfuscatedCachedChunk cache = null;
        // Hash used to check cache consistency
        long hash = 0L;
        // Start with caching false
        info.useCache = false;
        
        int initialRadius = OrebfuscatorConfig.getInitialRadius();
        
        // Expand buffer if not enough space
        if (info.blockSize > info.buffer.length)
        {
            info.buffer = new byte[info.blockSize];
            buffer.set(info.buffer);
        }
        
        // Copy data into buffer
        System.arraycopy(info.data, info.startIndex, info.buffer, 0, info.blockSize);
        
        // Caching
        if (OrebfuscatorConfig.getUseCache())
        {
            // Get cache folder
            File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), info.world.getName());
            // Create cache objects
            cache = new ObfuscatedCachedChunk(cacheFolder, info.chunkX, info.chunkZ, initialRadius);
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
                if (data != null)
                {
                    // Hash match, use the cached data instead and skip calculations
                    return data;
                }
            }
        }
        
        // Track of pseudo-randomly assigned randomBlock
        int randomIncrement = 0;
        int randomIncrement2 = 0;
        // Track of whether a block should be obfuscated or not
        boolean obfuscate = false;
        
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
            if ((info.chunkMask & 1 << i) != 0)
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
        
        // If cache is still allowed
        if (info.useCache)
        {
            // Save cache
            cache.initialRadius = initialRadius;
            cache.Write(hash, info.buffer);
        }
        
        // Free memory taken by cache quickly
        if (cache != null)
        {
            cache.free();
        }
        
        return info.buffer;
    }
    
    public static boolean areAjacentBlocksTransparent(ChunkInfo info, int x, int y, int z, int countdown)
    {
        byte id = 0;
        boolean foundID = false;
        
        if (y >= info.world.getMaxHeight() || y < 0)
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
                id = (byte) info.world.getBlockTypeIdAt(x, y, z);
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
            if (info.world.getBlockAt(x, y, z).getLightLevel() > 0)
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
}