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

package com.lishid.orebfuscator.hook;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import net.minecraft.server.v1_4_5.NetworkManager;
import net.minecraft.server.v1_4_5.Packet;

import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.utils.ReflectionHelper;

public class OrebfuscatorPlayerHook implements Listener
{
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event)
    {
        if (!Orebfuscator.usePL)
        {
            HookNM((CraftPlayer) event.getPlayer());
        }
        HookChunkQueue((CraftPlayer) event.getPlayer());
    }
    
    public static void HookChunkQueue(CraftPlayer player)
    {
        ReflectionHelper.setPrivateFinal(player.getHandle(), "chunkCoordIntPairQueue", new OrebfuscatorChunkQueue(player, player.getHandle().chunkCoordIntPairQueue));
    }
    
    /*
     * public static void HookNSH(CraftPlayer player)
     * {
     * try
     * {
     * CraftServer server = (CraftServer) player.getServer();
     * 
     * if (!(player.getHandle().netServerHandler instanceof OrebfuscatorNetServerHandler))
     * {
     * OrebfuscatorNetServerHandler handler = new OrebfuscatorNetServerHandler(server.getServer(), player.getHandle().netServerHandler);
     * player.getHandle().netServerHandler.networkManager.a(handler);
     * player.getHandle().netServerHandler = handler;
     * }
     * }
     * catch (Exception e)
     * {
     * Orebfuscator.log(e);
     * }
     * }
     */
    
    public static void HookNM(CraftPlayer player)
    {
        // Update NetworkManager's lists
        NetworkManager networkManager = (NetworkManager) player.getHandle().netServerHandler.networkManager;
        
        Field[] networkFields = networkManager.getClass().getDeclaredFields();
        for (Field field : networkFields)
        {
            try
            {
                if (List.class.isAssignableFrom(field.getType()))
                {
                    // System.out.println("Found field " + field.getName());
                    List<Packet> list = new OrebfuscatorNetworkQueue(player);
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<Packet> oldList = (List<Packet>) field.get(networkManager);
                    // Move packets to new list
                    synchronized (ReflectionHelper.getPrivateField(networkManager, "h"))
                    {
                        list.addAll(oldList);
                        oldList.clear();
                    }
                    // Replace with new list
                    field.set(networkManager, Collections.synchronizedList(list));
                }
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
}
