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

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.event.OFCPlayerKickEvent;
import com.lishid.orebfuscator.api.event.OFCPlayerKickEvent.KickReason;
import com.lishid.orebfuscator.api.hithack.IPlayerBlockTracking;
import com.lishid.orebfuscator.api.logger.OFCLogger;

public class PlayerBlockTracking implements IPlayerBlockTracking {

	private final Orebfuscator plugin;
	private final Player player;

	private Block block;
	private int hackingIndicator;
	private long lastTime = System.currentTimeMillis();

	public PlayerBlockTracking(Orebfuscator plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	public Player getPlayer() {
		return this.player;
	}

	public int getHackingIndicator() {
		return hackingIndicator;
	}

	public Block getBlock() {
		return block;
	}

	public boolean isBlock(Block block) {
		if (block == null || this.block == null)
			return false;
		return block.equals(this.block);
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public void incrementHackingIndicator(int value) {
		hackingIndicator += value;
		if (hackingIndicator >= (1 << 14)) {
			Bukkit.getServer().getScheduler().runTask(this.plugin, new Runnable() {

				public void run() {
					String name = player.getName();
					OFCLogger.log("Player \"" + name + "\" tried to hack with packet spamming.");

					OFCPlayerKickEvent event = new OFCPlayerKickEvent(player, KickReason.PACKET_SPAMMING, "End of Stream");
					Bukkit.getServer().getPluginManager().callEvent(event);

					if (!event.isCancelled()) {
						player.kickPlayer(event.getMessage());
					}
				}
			});
		}
	}

	public void incrementHackingIndicator() {
		incrementHackingIndicator(1);
	}

	public void decrementHackingIndicator(int value) {
		System.out.println("hacking old: " + this.hackingIndicator);
		this.hackingIndicator -= value;
		if (this.hackingIndicator < 0) {
			this.hackingIndicator = 0;
		}
		System.out.println("hacking new: " + this.hackingIndicator);
	}

	public void updateTime() {
		this.lastTime = System.currentTimeMillis();
	}

	public long getTimeDifference() {
		return System.currentTimeMillis() - this.lastTime;
	}
}
