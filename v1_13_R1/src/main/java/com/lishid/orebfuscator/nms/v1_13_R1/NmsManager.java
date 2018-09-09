package com.lishid.orebfuscator.nms.v1_13_R1;

import com.lishid.orebfuscator.nms.*;
import com.lishid.orebfuscator.types.BlockCoord;
import com.lishid.orebfuscator.types.BlockState;

import net.minecraft.server.v1_13_R1.*;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class NmsManager implements INmsManager {
    private int maxLoadedCacheFiles;

    private static IBlockData getBlockData(World world, int x, int y, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        WorldServer worldServer = ((CraftWorld)world).getHandle();
        ChunkProviderServer chunkProviderServer = worldServer.getChunkProviderServer();

        if(!chunkProviderServer.isLoaded(chunkX, chunkZ)) return null;

        Chunk chunk = chunkProviderServer.getOrLoadChunkAt(chunkX, chunkZ);

        return chunk.getBlockData(x, y, z);
    }

    @Override
    public void setMaxLoadedCacheFiles(int value) { this.maxLoadedCacheFiles = value; }
    @Override
    public INBT createNBT() { return new NBT(); }

    @Override
    public IChunkCache createChunkCache() {
        return new ChunkCache(this.maxLoadedCacheFiles);
    }

    @Override
    public IChunkManager getChunkManager(World world) {
        WorldServer worldServer = ((CraftWorld)world).getHandle();
        PlayerChunkMap chunkMap = worldServer.getPlayerChunkMap();

        return new ChunkManager(chunkMap);
    }

    @Override
    public void updateBlockTileEntity(BlockCoord blockCoord, Player player) {
        CraftWorld world = (CraftWorld)player.getWorld();
        TileEntity tileEntity = world.getTileEntityAt(blockCoord.x, blockCoord.y, blockCoord.z);

        if (tileEntity == null) {
            return;
        }

        Packet<?> packet = tileEntity.getUpdatePacket();

        if (packet != null) {
            CraftPlayer player2 = (CraftPlayer)player;
            player2.getHandle().playerConnection.sendPacket(packet);
        }
    }

    @Override
    public void notifyBlockChange(World world, IBlockInfo blockInfo) {
        BlockPosition blockPosition = new BlockPosition(blockInfo.getX(), blockInfo.getY(), blockInfo.getZ());
        IBlockData blockData = ((BlockInfo)blockInfo).getBlockData();

        ((CraftWorld)world).getHandle().notify(blockPosition, blockData, blockData, 0);
    }

    @Override
    public int getBlockLightLevel(World world, int x, int y, int z) {
        return ((CraftWorld)world).getHandle().getLightLevel(new BlockPosition(x, y, z));
    }

    @Override
    public IBlockInfo getBlockInfo(World world, int x, int y, int z) {
        IBlockData blockData = getBlockData(world, x, y, z);

        return blockData != null
            ? new BlockInfo(x, y, z, blockData)
            : null;
    }

    @Override
    public BlockState getBlockState(World world, int x, int y, int z) {
        IBlockData blockData = getBlockData(world, x, y, z);

        if(blockData == null) return null;

        Block block = blockData.getBlock();

        BlockState blockState = new BlockState();
        blockState.id = Block.getCombinedId(blockData);

        // As far as I can tell toLegacyData doesn't exist in 1.13 yet.
        // The original implementation gets a list of block states from blockData, and if empty returns 0,
        // otherwise throws an exception.
        //
        // As far as I can tell BlockData only has a method to update state, and no method that returns
        // states. So we grab block states from the block instance itself.
        boolean hasEmptyState = block.getStates().d().isEmpty();
        if (!hasEmptyState) {
            throw new IllegalArgumentException("Don't know how to convert " + blockData + " back into data...");
        }
        blockState.meta = 0;

        return blockState;
    }

    @Override
    public int getBlockId(World world, int x, int y, int z) {
        IBlockData blockData = getBlockData(world, x, y, z);

        return blockData != null ? Block.getCombinedId(blockData): -1;
    }

    @Override
    public String getTextFromChatComponent(String json) {
        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(json);

        return CraftChatMessage.fromComponent(component);
    }
}
