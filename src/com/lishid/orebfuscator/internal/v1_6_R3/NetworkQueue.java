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

package com.lishid.orebfuscator.internal.v1_6_R3;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.obfuscation.Calculations;

//Volatile
import net.minecraft.server.v1_6_R3.*;

public class NetworkQueue extends ArrayList<Packet>
{
    private static final long serialVersionUID = 4252847662044263527L;
    private Player player;
    
    public NetworkQueue(Player player)
    {
        this.player = player;
    }
    
    @Override
    public boolean add(Packet packet)
    {
        if (packet.n() == 51)
        {
            IPacket51 packet51 = InternalAccessor.Instance.newPacket51();
            packet51.setPacket(packet);
            Calculations.Obfuscate(packet51, this.player);
        }
        
        return super.add(packet);
    }
}