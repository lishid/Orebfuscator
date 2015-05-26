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

package com.lishid.orebfuscator.hook;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.internal.Packet51;
import com.lishid.orebfuscator.obfuscation.Calculations;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig.EnumPlayerDigType;
import org.bukkit.plugin.Plugin;

public class ProtocolLibHook {
    private ProtocolManager manager;

    public void register(Plugin plugin) {
        manager = ProtocolLibrary.getProtocolManager();
        Integer[] packets = new Integer[]{Packets.Server.MAP_CHUNK};

        manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, packets) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketID() == Packets.Server.MAP_CHUNK) {
                    Packet51 packet = new Packet51();
                    packet.setPacket(event.getPacket().getHandle());
                    Calculations.Obfuscate(packet, event.getPlayer());
                }
            }
        });

        Integer[] packets2 = new Integer[]{Packets.Client.BLOCK_DIG};
        manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.CLIENT_SIDE, packets2) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketID() == Packets.Client.BLOCK_DIG) {
                    EnumPlayerDigType status = event.getPacket().getSpecificModifier(EnumPlayerDigType.class).read(0);
                    if (status == EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                        if (!BlockHitManager.hitBlock(event.getPlayer(), null)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        });
    }
}
