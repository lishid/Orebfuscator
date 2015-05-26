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

package com.lishid.orebfuscator.internal;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.utils.ReflectionHelper;
import io.netty.channel.*;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig.EnumPlayerDigType;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

//Volatile

public class PlayerHook {

    public Channel getChannel(CraftPlayer player) throws Exception {
        Field[] fields = NetworkManager.class.getDeclaredFields();
        Field channelField = null;
        for (Field f : fields) {
            if (Channel.class.isAssignableFrom(f.getType())) {
                channelField = f;
                break;
            }
        }
        if (channelField == null) {
            throw new Exception("Cannot find netty channel field in NetworkManager!");
        }
        channelField.setAccessible(true);
        NetworkManager networkManager = (NetworkManager) player.getHandle().playerConnection.networkManager;
        return (Channel) channelField.get(networkManager);
    }

    public void HookNM(Player p) {
        try {
            CraftPlayer player = (CraftPlayer) p;
            Channel channel = getChannel(player);
            channel.pipeline().addBefore("packet_handler", "orebfuscator", new OutboundChannelHandler(player));
            channel.pipeline().addBefore("packet_handler", "orebfuscator_digcheck", new InboundChannelHandler(player));
        } catch (Exception e) {
            Orebfuscator.log(e);
        }
    }

    public class OutboundChannelHandler extends ChannelOutboundHandlerAdapter {
        CraftPlayer player;

        public OutboundChannelHandler(CraftPlayer player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
            try {
                if (packet instanceof PacketPlayOutMapChunk) {
                    Packet51 packet51 = new Packet51();
                    packet51.setPacket(packet);
                    Calculations.Obfuscate(packet51, this.player);
                }
            } catch (Exception e) {
                Orebfuscator.log(e);
            }
            super.write(ctx, packet, promise);
        }
    }

    public class InboundChannelHandler extends ChannelInboundHandlerAdapter {
        CraftPlayer player;

        public InboundChannelHandler(CraftPlayer player) {
            this.player = player;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
            try {
                if (packet instanceof PacketPlayInBlockDig) {
                    PacketPlayInBlockDig packet2 = (PacketPlayInBlockDig) packet;
                    if (packet2.c() == EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                        boolean canHit = BlockHitManager.hitBlock(player, null);
                        if (!canHit) {
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                Orebfuscator.log(e);
            }
            super.channelRead(ctx, packet);
        }
    }

    public void HookChunkQueue(Player p) {
        CraftPlayer player = (CraftPlayer) p;
        ReflectionHelper.setPrivateFinal(player.getHandle(), "chunkCoordIntPairQueue",
                new ChunkQueue(player, player.getHandle().chunkCoordIntPairQueue));
    }
}
