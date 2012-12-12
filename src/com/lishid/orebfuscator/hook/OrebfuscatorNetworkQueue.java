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

import java.util.ArrayList;

import net.minecraft.server.v1_4_5.*;

import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;

import com.lishid.orebfuscator.obfuscation.Calculations;

public class OrebfuscatorNetworkQueue extends ArrayList<Packet>
{
    private static final long serialVersionUID = 4252847662044263527L;
    private CraftPlayer player;
    
    public OrebfuscatorNetworkQueue(CraftPlayer player)
    {
        this.player = player;
    }
    
    @Override
    public boolean add(Packet packet){

        if (packet instanceof Packet51MapChunk)
        {
            Calculations.Obfuscate((Packet51MapChunk) packet, this.player);
        }
        
        return super.add(packet);
    }
}