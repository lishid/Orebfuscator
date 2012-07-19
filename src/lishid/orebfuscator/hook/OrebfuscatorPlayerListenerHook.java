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

package lishid.orebfuscator.hook;

import lishid.orebfuscator.Orebfuscator;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OrebfuscatorPlayerListenerHook implements Listener{
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		TryUpdateNetServerHandler(event.getPlayer());
	}
	
	public void TryUpdateNetServerHandler(Player player)
	{
		try
		{
			CraftPlayer cPlayer = (CraftPlayer)player;
			CraftServer server = (CraftServer)player.getServer();

			if (!(cPlayer.getHandle().netServerHandler instanceof OrebfuscatorNetServerHandler)) {
				OrebfuscatorNetServerHandler handler = new OrebfuscatorNetServerHandler(server.getHandle().server, cPlayer.getHandle().netServerHandler);
				cPlayer.getHandle().netServerHandler = handler;
				cPlayer.getHandle().netServerHandler.networkManager.a(handler);
				server.getServer().networkListenThread.a(handler);
			}
		}
		catch (Exception e) { Orebfuscator.log(e); }
	}
}
