/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.types.BlockState;

public class DeprecatedMethods {

    public static boolean applyPhysics(Block block) {
        // See net.minecraft.server.v1_4_5.BlockSand.canFall(World world, int i, int j, int k)
    	// Updated nominally for 1.13, using isSolid() inversion.

        Material blockID = block.getRelative(0, -1, 0).getType();

        return (Material.AIR.equals(blockID) ||
        		Material.FIRE.equals(blockID) ||
        		Material.WATER.equals(blockID) ||
        		Material.LAVA.equals(blockID) ||
        		Material.CAVE_AIR.equals(blockID) ||
        		Material.VOID_AIR.equals(blockID) ||
        		!blockID.isSolid());
    }
    
    public static Material getTypeId(Block block) {
    	return block.getType();
    }
    
    public static void sendBlockChange(Player player, Location blockLocation, BlockState blockState) {
    	// 1.13!
    	player.sendBlockChange(blockLocation, Orebfuscator.nms.getBlockDataFromBlockState(blockState));
    }
}
