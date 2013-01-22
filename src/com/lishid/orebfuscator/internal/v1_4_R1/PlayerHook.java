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

package com.lishid.orebfuscator.internal.v1_4_R1;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.List;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.internal.IPlayerHook;
import com.lishid.orebfuscator.utils.ReflectionHelper;

import org.bukkit.entity.Player;

//Volatile
import net.minecraft.server.v1_4_R1.*;
import org.bukkit.craftbukkit.v1_4_R1.entity.*;

public class PlayerHook implements IPlayerHook
{
    @SuppressWarnings("unchecked")
    public void HookNM(Player p)
    {
        CraftPlayer player = (CraftPlayer) p;
        // Update NetworkManager's lists
        NetworkManager networkManager = (NetworkManager) player.getHandle().playerConnection.networkManager;
        
        Field[] networkFields = networkManager.getClass().getDeclaredFields();
        for (Field field : networkFields)
        {
            try
            {
                if (List.class.isAssignableFrom(field.getType()))
                {
                    List<Packet> list = new NetworkQueue(p);
                    field.setAccessible(true);
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
        
        hookPacket();
    }
    
    static boolean hookPacket = false;
    
    @SuppressWarnings("unchecked")
    private void hookPacket()
    {
        if (hookPacket)
            return;
        
        hookPacket = true;
        Packet.l.a(14, Packet14Orebfuscator.class);
        // Use reflection to add into a and c
        Field[] packetFields = Packet.class.getDeclaredFields();
        for (Field field : packetFields)
        {
            try
            {
                if (Map.class.isAssignableFrom(field.getType()))
                {
                    field.setAccessible(true);
                    @SuppressWarnings({ "rawtypes" })
                    Map packets = (Map) field.get(null);
                    packets.put(Packet14Orebfuscator.class, 14);
                }
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
        }
    }
    
    public void HookChunkQueue(Player p)
    {
        CraftPlayer player = (CraftPlayer) p;
        ReflectionHelper.setPrivateFinal(player.getHandle(), "chunkCoordIntPairQueue", new ChunkQueue(player, player.getHandle().chunkCoordIntPairQueue));
    }
}
