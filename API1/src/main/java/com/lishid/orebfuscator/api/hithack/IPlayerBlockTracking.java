package com.lishid.orebfuscator.api.hithack;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface IPlayerBlockTracking {

	public Player getPlayer();

	public int getHackingIndicator();

	public Block getBlock();

	public boolean isBlock(Block block);

	public void setBlock(Block block);

	public void incrementHackingIndicator(int value);

	public void incrementHackingIndicator();

	public void decrementHackingIndicator(int value);

	public void updateTime();

	public long getTimeDifference();
}