/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;

public class PlayerBlockTracking {
    private Block block;
    private int hackingIndicator;
    private Player player;
    private long lastTime = System.currentTimeMillis();

    public PlayerBlockTracking(Player player) {
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
            Orebfuscator.log("Player \"" + this.player.getName() + "\" tried to hack with packet spamming.");
            Orebfuscator.log("Player \"" + this.player.getName() + "\" kicked.");
            this.player.kickPlayer("End of Stream");
        }
    }

    public void incrementHackingIndicator() {
        incrementHackingIndicator(1);
    }

    public void decrementHackingIndicator(int value) {
        hackingIndicator -= value;
        if (hackingIndicator < 0)
            hackingIndicator = 0;
    }

    public void updateTime() {
        lastTime = System.currentTimeMillis();
    }

    public long getTimeDifference() {
        return System.currentTimeMillis() - lastTime;
    }
}
