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

package com.lishid.orebfuscator.hithack;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.config.IOrebfuscatorConfig;
import com.lishid.orebfuscator.api.hithack.IBlockHitHandler;
import com.lishid.orebfuscator.api.hithack.IPlayerBlockTracking;
import com.lishid.orebfuscator.handler.CraftHandler;

public class BlockHitHandler extends CraftHandler implements IBlockHitHandler {

	private final HashMap<Player, PlayerBlockTracking> playersBlockTrackingStatus = new HashMap<Player, PlayerBlockTracking>();

	private IOrebfuscatorConfig config;

	public BlockHitHandler(Orebfuscator plugin) {
		super(plugin);
	}

	@Override
	public void onInit() {
		this.config = this.plugin.getConfigHandler().getConfig();
	}

	@Override
	public void onDisable() {
		this.clearAll();
	}

	public boolean hitBlock(Player player, Block block) {
		if (player.getGameMode() == GameMode.CREATIVE)
			return true;

		IPlayerBlockTracking playerBlockTracking = this.getPlayerBlockTracking(player);

		if (playerBlockTracking.isBlock(block)) {
			return true;
		}

		long time = playerBlockTracking.getTimeDifference();
		playerBlockTracking.incrementHackingIndicator();
		playerBlockTracking.setBlock(block);
		playerBlockTracking.updateTime();

		int decrement = (int) (time / this.config.getAntiHitHackDecrementFactor());
		playerBlockTracking.decrementHackingIndicator(decrement);

		if (playerBlockTracking.getHackingIndicator() == this.config.getAntiHitHackMaxViolation()) {
			playerBlockTracking.incrementHackingIndicator(this.config.getAntiHitHackMaxViolation());
		}

		if (playerBlockTracking.getHackingIndicator() > this.config.getAntiHitHackMaxViolation()) {
			return false;
		}

		return true;
	}

	public boolean canFakeHit(Player player) {
		IPlayerBlockTracking playerBlockTracking = this.getPlayerBlockTracking(player);

		if (playerBlockTracking.getHackingIndicator() > this.config.getAntiHitHackMaxViolation())
			return false;

		return true;
	}

	public boolean fakeHit(Player player) {
		IPlayerBlockTracking playerBlockTracking = this.getPlayerBlockTracking(player);
		playerBlockTracking.incrementHackingIndicator();

		if (playerBlockTracking.getHackingIndicator() > this.config.getAntiHitHackMaxViolation())
			return false;

		return true;
	}

	public void breakBlock(Player player, Block block) {
		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		IPlayerBlockTracking playerBlockTracking = this.getPlayerBlockTracking(player);
		if (playerBlockTracking.isBlock(block)) {
			playerBlockTracking.decrementHackingIndicator(2);
		}
	}

	public IPlayerBlockTracking getPlayerBlockTracking(Player player) {
		PlayerBlockTracking blockTracking = playersBlockTrackingStatus.get(player);
		if (blockTracking == null) {
			blockTracking = new PlayerBlockTracking(this.plugin, player);
			playersBlockTrackingStatus.put(player, blockTracking);
		}
		return blockTracking;
	}

	public void clearHistory(Player player) {
		playersBlockTrackingStatus.remove(player);
	}

	public void clearAll() {
		playersBlockTrackingStatus.clear();
	}
}