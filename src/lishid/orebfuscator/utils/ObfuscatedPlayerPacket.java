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

package lishid.orebfuscator.utils;

import net.minecraft.server.Packet51MapChunk;

import org.bukkit.craftbukkit.entity.CraftPlayer;

public class ObfuscatedPlayerPacket {
	public CraftPlayer player;
	public Packet51MapChunk packet;
	public ObfuscatedPlayerPacket(CraftPlayer player, Packet51MapChunk packet)
	{
		this.player = player;
		this.packet = packet;
	}
}
