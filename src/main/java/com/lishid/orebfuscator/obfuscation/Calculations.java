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

package com.lishid.orebfuscator.obfuscation;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.internal.ChunkData;
import com.lishid.orebfuscator.internal.Packet51;
import com.lishid.orebfuscator.internal.Packet56;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class Calculations {
    public static final int BYTES_PER_BLOCK = 2;

    public static final int BLOCKS_PER_SECTION = 16 * 16 * 16;
    public static final int BYTES_PER_SECTION = BYTES_PER_BLOCK * BLOCKS_PER_SECTION;

    public static final int MAX_SECTIONS_PER_CHUNK = 16;
    public static final int MAX_BYTES_PER_CHUNK = BYTES_PER_SECTION * MAX_SECTIONS_PER_CHUNK;


    public static final ThreadLocal<byte[]> buffer = new ThreadLocal<byte[]>() {
        protected byte[] initialValue() {
            return new byte[MAX_BYTES_PER_CHUNK];
        }
    };

    private static Map<Player, Map<ChunkAddress, Set<MinecraftBlock>>> signsMap = new WeakHashMap<Player, Map<ChunkAddress, Set<MinecraftBlock>>>();

    private static Map<ChunkAddress, Set<MinecraftBlock>> getPlayerSignsMap(Player player) {
        Map<ChunkAddress, Set<MinecraftBlock>> map = signsMap.get(player);
        if (map == null) {
            map = new HashMap<ChunkAddress, Set<MinecraftBlock>>();
            signsMap.put(player, map);
        }
        return map;
    }

    private static void putSignsList(Player player, int chunkX, int chunkZ, Set<MinecraftBlock> blocks) {
        Map<ChunkAddress, Set<MinecraftBlock>> map = getPlayerSignsMap(player);
        ChunkAddress address = new ChunkAddress(chunkX, chunkZ);
        map.put(address, blocks);
    }

    public static Set<MinecraftBlock> getSignsList(Player player, int chunkX, int chunkZ) {
        Map<ChunkAddress, Set<MinecraftBlock>> map = getPlayerSignsMap(player);
        ChunkAddress address = new ChunkAddress(chunkX, chunkZ);
        return map.get(address);
    }

    public static void putSignsList(Player player, int chunkX, int chunkZ, List<Block> proximityBlocks) {
        Set<MinecraftBlock> signs = new HashSet<MinecraftBlock>();
        for (Block b : proximityBlocks) {
            if (b.getState() instanceof Sign) {
                signs.add(new MinecraftBlock(b));
            }
        }
        putSignsList(player, chunkX, chunkZ, signs);
    }

    public static void Obfuscate(Object packet, Player player) {
        // Assuming that NoLagg will pass a Packet51
        Packet51 packet51 = new Packet51();
        packet51.setPacket(packet);
        Calculations.Obfuscate(packet51, player);
    }

    public static void Obfuscate(Packet56 packet, Player player) {
        ChunkInfo[] infos = getInfo(packet, player);

        for (ChunkInfo info : infos) {
            ComputeChunkInfoAndObfuscate(info);
        }
    }

    public static void Obfuscate(Packet51 packet, Player player) {
        ChunkInfo info = getInfo(packet, player);

        if (info.chunkMask == 0) {
            return;
        }
        ComputeChunkInfoAndObfuscate(info);
    }

    public static ChunkInfo[] getInfo(Packet56 packet, Player player) {
        ChunkData[] chunks = packet.getChunkData();
        ChunkInfo[] infos = new ChunkInfo[chunks.length];

        for (int i = 0; i < chunks.length; i++) {
            // Create an info objects
            ChunkInfo info = new ChunkInfo(player, chunks[i], buffer.get());
            infos[i] = info;
        }

        return infos;
    }

    public static ChunkInfo getInfo(Packet51 packet, Player player) {
        ChunkInfo info = new ChunkInfo(player, packet.getChunkData(), buffer.get());
        return info;
    }

    public static void ComputeChunkInfoAndObfuscate(ChunkInfo info) {
        // Obfuscate
        if (!OrebfuscatorConfig.isWorldDisabled(info.world.getName()) && // World not enabled
                OrebfuscatorConfig.obfuscateForPlayer(info.player) && // Should the player have obfuscation?
                OrebfuscatorConfig.Enabled) // Plugin enabled
        {
            byte[] obfuscated = Obfuscate(info);
            // Copy the data out of the buffer
            System.arraycopy(obfuscated, 0, info.original, 0, info.bytes);
        }
    }

    public static byte[] Obfuscate(ChunkInfo info) {
        Environment environment = info.world.getEnvironment();
        // Used for caching
        ObfuscatedCachedChunk cache = null;
        // Hash used to check cache consistency
        long hash = 0L;
        // Blocks kept track for ProximityHider
        ArrayList<Block> proximityBlocks = new ArrayList<Block>();
        // Start with caching false
        info.useCache = false;

        int initialRadius = OrebfuscatorConfig.InitialRadius;

        // Copy data into buffer
        System.arraycopy(info.original, 0, info.buffer, 0, info.bytes);

        // Caching
        if (OrebfuscatorConfig.UseCache) {
            // Hash the chunk
            hash = CalculationsUtil.Hash(info.buffer, info.bytes);
            // Sanitize buffer for caching
            PrepareBufferForCaching(info.buffer, info.bytes);
            // Get cache folder
            File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), info.world.getName());
            // Create cache objects
            cache = new ObfuscatedCachedChunk(cacheFolder, info.chunkX, info.chunkZ);
            info.useCache = true;                     
            // Check if hash is consistent
            cache.Read();
            
            long storedHash = cache.getHash();
            int[] proximityList = cache.proximityList;

            if (storedHash == hash && cache.data != null) {
                // Decrypt chest list
                if (proximityList != null) {
                    for (int i = 0; i < proximityList.length; i += 3) {
                        Block b = CalculationsUtil.getBlockAt(info.player.getWorld(), proximityList[i], proximityList[i + 1], proximityList[i + 2]);
                        proximityBlocks.add(b);
                    }
                }

                // Caching done, de-sanitize buffer
                RepaintChunkToBuffer(cache.data, info);

                // ProximityHider add blocks
                putSignsList(info.player, info.chunkX, info.chunkZ, proximityBlocks);
                ProximityHider.AddProximityBlocks(info.player, proximityBlocks);

                // Hash match, use the cached data instead and skip calculations
                return cache.data;
            }
        }

        // Track of pseudo-randomly assigned randomBlock
        int randomIncrement = 0;
        int randomIncrement2 = 0;
        int ramdomCave = 0;
        // Track of whether a block should be obfuscated or not
        boolean obfuscate = false;
        boolean specialObfuscate = false;

        int engineMode = OrebfuscatorConfig.EngineMode;
        int maxChance = OrebfuscatorConfig.AirGeneratorMaxChance;
        int incrementMax = maxChance;

        int randomBlocksLength = OrebfuscatorConfig.getRandomBlocks(false, environment).length;
        boolean randomAlternate = false;

        int startX = info.chunkX << 4;
        int startZ = info.chunkZ << 4;

        int index = 0;

        for (int i = 0; i < 16; i++) {
            if ((info.chunkMask & 1 << i) != 0) {
                OrebfuscatorConfig.shuffleRandomBlocks();
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        incrementMax = (maxChance + OrebfuscatorConfig.random(maxChance)) / 2;
                        for (int x = 0; x < 16; x++) {
                            int blockData = chunkGetBlockData(info.original, index);
                            int blockMeta = blockDataToMeta(blockData);
                            int blockId = blockDataToId(blockData);

                            if (blockId >= 256) {
                                index++;
                                continue;
                            }

                            int blockY = (i << 4) + y;

                            // Initialize data
                            obfuscate = false;
                            specialObfuscate = false;

                            // Check if the block should be obfuscated for the default engine modes
                            if (OrebfuscatorConfig.isObfuscated(blockId, environment)) {
                                if (initialRadius == 0) {
                                    // Do not interfere with PH
                                    if (OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(blockY, blockId)) {
                                        if (!areAjacentBlocksTransparent(info, blockId, startX + x, blockY, startZ + z, 1)) {
                                            obfuscate = true;
                                        }
                                    } else {
                                        // Obfuscate all blocks
                                        obfuscate = true;
                                    }
                                } else {
                                    // Check if any nearby blocks are transparent
                                    if (!areAjacentBlocksTransparent(info, blockId, startX + x, blockY, startZ + z, initialRadius)) {
                                        obfuscate = true;
                                    }
                                }
                            }

                            // Check if the block should be obfuscated because of proximity check
                            if (!obfuscate && OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(blockY, blockId)) {
                                if (OrebfuscatorConfig.isProximityHiderOn(blockY, blockId)) {
                                    Block block = CalculationsUtil.getBlockAt(info.player.getWorld(), startX + x, blockY, startZ + z);
                                    if (block != null) {
                                        proximityBlocks.add(block);
                                    }
                                    obfuscate = true;
                                    if (OrebfuscatorConfig.UseSpecialBlockForProximityHider) {
                                        specialObfuscate = true;
                                    }
                                }
                            }

                            // Check if the block is obfuscated
                            if (obfuscate) {
                                if (specialObfuscate) {
                                    // Proximity hider
                                    blockId = OrebfuscatorConfig.ProximityHiderID;
                                } else {
                                    randomIncrement2 = OrebfuscatorConfig.random(incrementMax);
                                    // CalculationsUtil.increment(randomIncrement2, incrementMax);

                                    if (engineMode == 1) {
                                        // Engine mode 1, replace with stone
                                        blockId = (environment == Environment.NETHER ? 87 : 1);
                                    } else if (engineMode == 2) {
                                        // Ending mode 2, replace with random block
                                        if (randomBlocksLength > 1)
                                            randomIncrement = CalculationsUtil.increment(randomIncrement, randomBlocksLength);
                                        blockId = OrebfuscatorConfig.getRandomBlock(randomIncrement, randomAlternate, environment);
                                        randomAlternate = !randomAlternate;
                                    }
                                    // Anti texturepack and freecam
                                    if (OrebfuscatorConfig.AntiTexturePackAndFreecam) {
                                    // Add random air blocks
                                        if (randomIncrement2 == 0) {
                                            ramdomCave = 1 + OrebfuscatorConfig.random(3);
                                        }

                                        if (ramdomCave > 0) {
                                            blockId = 0;
                                            ramdomCave--;
                                        }
                                    }
                                }

                                blockMeta = 0;
                            }

                            // Check if the block should be obfuscated because of the darkness
                            if (!obfuscate && OrebfuscatorConfig.DarknessHideBlocks && OrebfuscatorConfig.isDarknessObfuscated(blockId)) {
                                if (!areAjacentBlocksBright(info, startX + x, (i << 4) + y, startZ + z, 1)) {
                                    // Hide block, setting it to air
                                    blockId = 0;
                                    blockMeta = 0;
                                }
                            }

                            chunkSetBlockIdMeta(info.buffer, index, blockId, blockMeta);
                            index++;
                        }
                    }
                }
            }
        }

        putSignsList(info.player, info.chunkX, info.chunkZ, proximityBlocks);
        ProximityHider.AddProximityBlocks(info.player, proximityBlocks);

        // If cache is still allowed
        if (info.useCache) {
            // Save cache
            int[] proximityList = new int[proximityBlocks.size() * 3];
            for (int i = 0; i < proximityBlocks.size(); i++) {
                Block b = proximityBlocks.get(i);
                if (b != null) {
                    proximityList[i * 3] = b.getX();
                    proximityList[i * 3 + 1] = b.getY();
                    proximityList[i * 3 + 2] = b.getZ();
                }
            }
            cache.Write(hash, info.buffer, proximityList);
        }

        // Free memory taken by cache quickly
        if (cache != null) {
            cache.free();
        }

        // Caching done, de-sanitize buffer
        if (OrebfuscatorConfig.UseCache) {
            RepaintChunkToBuffer(info.buffer, info);
        }

        return info.buffer;
    }

    //16 bit char for block data, including 12 bits for block id
    private static final int BLOCKID_MAX = 4096;
    private static char[] cacheMap = new char[BLOCKID_MAX];

    static {
        buildCacheMap();
    }

    public static void buildCacheMap() {
        for (int i = 0; i < 4096; i++) {
            cacheMap[i] = (char) i;
            if (OrebfuscatorConfig.isBlockTransparent((short) i) && !isBlockSpecialObfuscated(64, (char) i)) {
                cacheMap[i] = 0;
            }
        }
    }

    private static void PrepareBufferForCaching(byte[] data, int bytes) {
        for (int i = 0; i < bytes / 2; i++) {
            int blockId = chunkGetBlockId(data, i);

            blockId = cacheMap[blockId % BLOCKID_MAX];

            chunkSetBlockId(data, i, blockId);
        }
    }

    private static boolean isBlockSpecialObfuscated(int y, char id) {
        if (OrebfuscatorConfig.DarknessHideBlocks && OrebfuscatorConfig.isDarknessObfuscated(id)) {
            return true;
        }
        if (OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(y, id)) {
            return true;
        }
        return false;
    }

    private static void RepaintChunkToBuffer(byte[] data, ChunkInfo info) {
        byte[] original = info.original;
        int bytes = info.bytes;

        for (int i = 0; i < bytes / 2; i++) {
            int newId = chunkGetBlockId(data, i);
            int originalId = chunkGetBlockId(original, i);

            if (newId == 0 && originalId != 0) {
                if (OrebfuscatorConfig.isBlockTransparent((short) originalId)) {
                    if (!isBlockSpecialObfuscated(0, (char) originalId)) {
                        chunkSetBlockId(data, i, originalId);
                    }
                }
            }
        }
    }

    public static boolean areAjacentBlocksTransparent(ChunkInfo info, int currentBlockID, int x, int y, int z, int countdown) {
        int id = 0;
        boolean foundID = false;

        if (y >= info.world.getMaxHeight() || y < 0)
            return true;

        int section = info.sectionIndices[y >> 4];

        if ((info.chunkMask & (1 << (y >> 4))) > 0 && x >> 4 == info.chunkX && z >> 4 == info.chunkZ) {
            int cX = ((x % 16) < 0) ? (x % 16 + 16) : (x % 16);
            int cZ = ((z % 16) < 0) ? (z % 16 + 16) : (z % 16);

            int index = section * BLOCKS_PER_SECTION + (y % 16 << 8) + (cZ << 4) + cX;
            try {
                id = chunkGetBlockId(info.original, index);
                foundID = true;
            } catch (Exception e) {
                Orebfuscator.log(e);
            }
        }

        if (!foundID) {
            if (CalculationsUtil.isChunkLoaded(info.world, x >> 4, z >> 4)) {
                id = info.world.getBlockTypeIdAt(x, y, z);
            } else {
                id = 1;
                info.useCache = false;
            }
        }

        if (id != currentBlockID && OrebfuscatorConfig.isBlockTransparent(id)) {
            return true;
        }

        if (countdown == 0)
            return false;

        if (areAjacentBlocksTransparent(info, currentBlockID, x, y + 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, currentBlockID, x, y - 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, currentBlockID, x + 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, currentBlockID, x - 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, currentBlockID, x, y, z + 1, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(info, currentBlockID, x, y, z - 1, countdown - 1))
            return true;

        return false;
    }

    public static boolean areAjacentBlocksBright(ChunkInfo info, int x, int y, int z, int countdown) {
        if (CalculationsUtil.isChunkLoaded(info.world, x >> 4, z >> 4)) {
            if (info.world.getBlockAt(x, y, z).getLightLevel() > 0) {
                return true;
            }
        } else {
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

    /**
     * Blocks are 2-bytes aligned
     * Every 2 bytes represents the block data as:
     * First byte = lower 8 bits
     * Second byte = upper 8 bits
     *
     * @param buffer
     * @param index
     * @return
     */
    private static int chunkGetBlockData(byte[] buffer, int index) {
        index = index << 1;
        return (buffer[index] & 0xFF) | ((buffer[index + 1] & 0xFF) << 8);
    }

    private static int chunkGetBlockId(byte[] buffer, int index) {
        return chunkGetBlockData(buffer, index) >> 4;
    }

    private static int blockDataToId(int blockData) {
        return blockData >> 4;
    }

    private static int blockDataToMeta(int blockData) {
        return blockData & 0xF;
    }

    private static int blockIdMetaToData(int blockId, int blockMeta) {
        return blockMeta | (blockId << 4);
    }

    private static void chunkSetBlockId(byte[] buffer, int index, int id) {
        int blockData = chunkGetBlockData(buffer, index);

        chunkSetBlockIdMeta(buffer, index, id, blockDataToMeta(blockData));
    }

    private static void chunkSetBlockIdMeta(byte[] buffer, int index, int id, int meta) {
        int blockData = blockIdMetaToData(id, meta);
        chunkSetBlockData(buffer, index, blockData);
    }

    private static void chunkSetBlockData(byte[] buffer, int index, int data) {
        index = index << 1;
        buffer[index] = (byte) (data & 0xFF);
        buffer[index + 1] = (byte) ((data >> 8) & 0xFF);
    }
}