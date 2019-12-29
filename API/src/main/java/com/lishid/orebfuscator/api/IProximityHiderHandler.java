package com.lishid.orebfuscator.api;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.types.BlockCoord;

public interface IProximityHiderHandler extends Handler {

	public void addProximityBlocks(Player player, int chunkX, int chunkZ, ArrayList<BlockCoord> blocks);

	public void clearPlayer(Player player);

	public void clearBlocksForOldWorld(Player player);

	public void addPlayerToCheck(Player player, Location location);

	public void addPlayersToReload(HashSet<Player> players);
}