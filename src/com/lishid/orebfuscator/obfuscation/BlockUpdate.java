package com.lishid.orebfuscator.obfuscation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.server.Packet;
import net.minecraft.server.TileEntity;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class BlockUpdate
{
    public static void UpdateBlocksNearby(Block block)
    {
        HashSet<Block> blocks = GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.getUpdateRadius());
        
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
            UpdateBlock(nearbyBlock, players);
        }
    }
    
    public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown)
    {
        if (block == null)
            return allBlocks;
        
        AddBlockCheck(allBlocks, block);
        
        if (countdown == 0)
            return allBlocks;
        
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() + 1, block.getY(), block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() - 1, block.getY(), block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() + 1, block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() - 1, block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() + 1), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() - 1), countdown - 1);
        
        return allBlocks;
    }
    
    public static void AddBlockCheck(HashSet<Block> allBlocks, Block block)
    {
        if ((OrebfuscatorConfig.isObfuscated((byte) block.getTypeId()) || OrebfuscatorConfig.isDarknessObfuscated((byte) block.getTypeId())))
        {
            allBlocks.add(block);
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
}
