package com.lishid.orebfuscator.nms.v1_9_R2;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

import com.google.common.collect.ImmutableList;
import com.lishid.orebfuscator.types.ConfigDefaults;

import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.IBlockData;

public class ConfigDefault extends ConfigDefaults {

	public final Material[] extraTransparentBlocks;

	public ConfigDefault() {
		// Default World
		this.defaultProximityHiderBlockIds = convertMaterialsToIds(new Material[] { Material.DISPENSER,
				Material.MOB_SPAWNER, Material.CHEST, Material.HOPPER, Material.WORKBENCH, Material.FURNACE,
				Material.BURNING_FURNACE, Material.ENCHANTMENT_TABLE, Material.EMERALD_ORE, Material.ENDER_CHEST,
				Material.ANVIL, Material.TRAPPED_CHEST, Material.DIAMOND_ORE });

		this.defaultDarknessBlockIds = convertMaterialsToIds(new Material[] { Material.MOB_SPAWNER, Material.CHEST });

		this.defaultMode1BlockId = getMaterialIds(Material.STONE).iterator().next();
		this.defaultProximityHiderSpecialBlockId = getMaterialIds(Material.STONE).iterator().next();

		// The End
		this.endWorldRandomBlockIds = convertMaterialsToIds(new Material[] { Material.BEDROCK, Material.OBSIDIAN,
				Material.ENDER_STONE, Material.PURPUR_BLOCK, Material.END_BRICKS });

		this.endWorldObfuscateBlockIds = convertMaterialsToIds(new Material[] { Material.ENDER_STONE });

		this.endWorldMode1BlockId = getMaterialIds(Material.ENDER_STONE).iterator().next();
		this.endWorldRequiredObfuscateBlockIds = convertMaterialsToIds(new Material[] { Material.ENDER_STONE });

		// Nether World
		this.netherWorldRandomBlockIds = convertMaterialsToIds(new Material[] { Material.GRAVEL, Material.NETHERRACK,
				Material.SOUL_SAND, Material.NETHER_BRICK, Material.QUARTZ_ORE });

		this.netherWorldObfuscateBlockIds = convertMaterialsToIds(
				new Material[] { Material.NETHERRACK, Material.QUARTZ_ORE });

		this.netherWorldMode1BlockId = getMaterialIds(Material.NETHERRACK).iterator().next();

		this.netherWorldRequiredObfuscateBlockIds = convertMaterialsToIds(new Material[] { Material.NETHERRACK });

		// Normal World
		this.normalWorldRandomBlockIds = convertMaterialsToIds(new Material[] { Material.STONE, Material.COBBLESTONE,
				Material.WOOD, Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE, Material.LAPIS_ORE,
				Material.TNT, Material.MOSSY_COBBLESTONE, Material.OBSIDIAN, Material.DIAMOND_ORE,
				Material.REDSTONE_ORE, Material.CLAY, Material.EMERALD_ORE });

		this.normalWorldObfuscateBlockIds = convertMaterialsToIds(new Material[] { Material.GOLD_ORE, Material.IRON_ORE,
				Material.COAL_ORE, Material.LAPIS_ORE, Material.CHEST, Material.DIAMOND_ORE, Material.ENDER_CHEST,
				Material.REDSTONE_ORE, Material.CLAY, Material.EMERALD_ORE });

		this.normalWorldMode1BlockId = getMaterialIds(Material.STONE).iterator().next();

		this.normalWorldRequiredObfuscateBlockIds = convertMaterialsToIds(new Material[] { Material.STONE });

		// Extra transparent blocks
		this.extraTransparentBlocks = new Material[] { Material.ACACIA_DOOR, Material.ACACIA_FENCE,
				Material.ACACIA_FENCE_GATE, Material.ACACIA_STAIRS, Material.ANVIL, Material.BEACON, Material.BED_BLOCK,
				Material.BIRCH_DOOR, Material.BIRCH_FENCE, Material.BIRCH_FENCE_GATE, Material.BIRCH_WOOD_STAIRS,
				Material.BREWING_STAND, Material.BRICK_STAIRS, Material.CACTUS, Material.CAKE_BLOCK, Material.CAULDRON,
				Material.COBBLESTONE_STAIRS, Material.COBBLE_WALL, Material.DARK_OAK_DOOR, Material.DARK_OAK_FENCE,
				Material.DARK_OAK_FENCE_GATE, Material.DARK_OAK_STAIRS, Material.DAYLIGHT_DETECTOR,
				Material.DAYLIGHT_DETECTOR_INVERTED, Material.DRAGON_EGG, Material.ENCHANTMENT_TABLE, Material.FENCE,
				Material.FENCE_GATE, Material.GLASS, Material.HOPPER, Material.ICE, Material.IRON_DOOR_BLOCK,
				Material.IRON_FENCE, Material.IRON_PLATE, Material.IRON_TRAPDOOR, Material.JUNGLE_DOOR,
				Material.JUNGLE_FENCE, Material.JUNGLE_FENCE_GATE, Material.JUNGLE_WOOD_STAIRS, Material.LAVA,
				Material.LEAVES, Material.LEAVES_2, Material.MOB_SPAWNER, Material.NETHER_BRICK_STAIRS,
				Material.NETHER_FENCE, Material.PACKED_ICE, Material.PISTON_BASE, Material.PISTON_EXTENSION,
				Material.PISTON_MOVING_PIECE, Material.PISTON_STICKY_BASE, Material.PURPUR_SLAB, Material.PURPUR_STAIRS,
				Material.QUARTZ_STAIRS, Material.RED_SANDSTONE_STAIRS, Material.SANDSTONE_STAIRS, Material.SIGN_POST,
				Material.SLIME_BLOCK, Material.SMOOTH_STAIRS, Material.SPRUCE_DOOR, Material.SPRUCE_FENCE,
				Material.SPRUCE_FENCE_GATE, Material.SPRUCE_WOOD_STAIRS, Material.STAINED_GLASS,
				Material.STAINED_GLASS_PANE, Material.STANDING_BANNER, Material.STATIONARY_LAVA,
				Material.STATIONARY_WATER, Material.STEP, Material.STONE_PLATE, Material.STONE_SLAB2,
				Material.THIN_GLASS, Material.TRAP_DOOR, Material.WALL_BANNER, Material.WALL_SIGN, Material.WATER,
				Material.WEB, Material.WOODEN_DOOR, Material.WOOD_PLATE, Material.WOOD_STAIRS, Material.WOOD_STEP };
	}

	@SuppressWarnings("deprecation")
	public Set<Integer> getMaterialIds(Material material) {
		Set<Integer> ids = new HashSet<>();
		int blockId = material.getId() << 4;
		Block block = Block.getById(material.getId());
		ImmutableList<IBlockData> blockDataList = block.t().a();

		for (IBlockData blockData : blockDataList) {
			ids.add(blockId | block.toLegacyData(blockData));
		}

		return ids;
	}

	private Set<Integer> convertMaterialsToSet(Material[] materials) {
		Set<Integer> ids = new HashSet<>();

		for (Material material : materials) {
			ids.addAll(getMaterialIds(material));
		}

		return ids;
	}

	private int[] convertMaterialsToIds(Material[] materials) {
		Set<Integer> ids = convertMaterialsToSet(materials);

		int[] result = new int[ids.size()];
		int index = 0;

		for (int id : ids) {
			result[index++] = id;
		}

		return result;
	}
}