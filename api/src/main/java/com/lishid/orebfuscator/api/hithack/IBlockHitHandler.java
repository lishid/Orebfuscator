package com.lishid.orebfuscator.api.hithack;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface IBlockHitHandler {

	public boolean hitBlock(Player player, Block block);

	public boolean canFakeHit(Player player);

	public boolean fakeHit(Player player);

	public void breakBlock(Player player, Block block);

	public IPlayerBlockTracking getPlayerBlockTracking(Player player);

	public void clearHistory(Player player);

	public void clearAll();
}