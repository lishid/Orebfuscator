package com.lishid.orebfuscator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.types.BlockState;

@SuppressWarnings("deprecation")
public class DeprecatedMethods {
    public static boolean applyPhysics(Block block) {
        // See net.minecraft.server.v1_4_5.BlockSand.canFall(World world, int i, int j, int k)

        int blockID = block.getRelative(0, -1, 0).getTypeId();

        int air = Material.AIR.getId();
        int fire = Material.FIRE.getId();
        int water = Material.WATER.getId();
        int water2 = Material.STATIONARY_WATER.getId();
        int lava = Material.LAVA.getId();
        int lava2 = Material.STATIONARY_LAVA.getId();

        return (blockID == air || blockID == fire || blockID == water || blockID == water2 || blockID == lava || blockID == lava2);
    }
    
    public static int getTypeId(Block block) {
    	return block.getTypeId();
    }
    
    public static void sendBlockChange(Player player, Location blockLocation, BlockState blockState) {
    	player.sendBlockChange(blockLocation, blockState.id, (byte)blockState.meta);
    }
}
