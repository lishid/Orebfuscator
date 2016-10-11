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

import com.lishid.orebfuscator.Orebfuscator;

public class BlockHitManager {
    private static HashMap<Player, PlayerBlockTracking> playersBlockTrackingStatus = new HashMap<Player, PlayerBlockTracking>();

    public static boolean hitBlock(Player player, Block block) {
        if (player.getGameMode() == GameMode.CREATIVE)
            return true;

        PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);

        if (playerBlockTracking.isBlock(block)) {
            return true;
        }

        long time = playerBlockTracking.getTimeDifference();
        playerBlockTracking.incrementHackingIndicator();
        playerBlockTracking.setBlock(block);
        playerBlockTracking.updateTime();

        int decrement = (int) (time / Orebfuscator.config.getAntiHitHackDecrementFactor());
        playerBlockTracking.decrementHackingIndicator(decrement);

        if (playerBlockTracking.getHackingIndicator() == Orebfuscator.config.getAntiHitHackMaxViolation())
            playerBlockTracking.incrementHackingIndicator(Orebfuscator.config.getAntiHitHackMaxViolation());

        if (playerBlockTracking.getHackingIndicator() > Orebfuscator.config.getAntiHitHackMaxViolation())
            return false;

        return true;
    }

    public static boolean canFakeHit(Player player) {
        PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);

        if (playerBlockTracking.getHackingIndicator() > Orebfuscator.config.getAntiHitHackMaxViolation())
            return false;

        return true;
    }

    public static boolean fakeHit(Player player) {
        PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);
        playerBlockTracking.incrementHackingIndicator();

        if (playerBlockTracking.getHackingIndicator() > Orebfuscator.config.getAntiHitHackMaxViolation())
            return false;

        return true;
    }

    public static void breakBlock(Player player, Block block) {
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);
        if (playerBlockTracking.isBlock(block)) {
            playerBlockTracking.decrementHackingIndicator(2);
        }
    }

    private static PlayerBlockTracking getPlayerBlockTracking(Player player) {
        if (!playersBlockTrackingStatus.containsKey(player)) {
            playersBlockTrackingStatus.put(player, new PlayerBlockTracking(player));
        }
        return playersBlockTrackingStatus.get(player);
    }

    public static void clearHistory(Player player) {
        playersBlockTrackingStatus.remove(player);
    }

    public static void clearAll() {
        playersBlockTrackingStatus.clear();
    }
}