package com.lishid.orebfuscator.api.utils;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;

public interface IBlockUpdate {

	public boolean needsUpdate(Block block);

	public void update(Block block);

	public void update(List<Block> blocks);

	public void updateByLocations(List<Location> locations, int updateRadius);
}