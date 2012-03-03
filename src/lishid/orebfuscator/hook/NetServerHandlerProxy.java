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

import net.minecraft.server.*;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class NetServerHandlerProxy extends NetServerHandler implements ICommandListener {

    public static Logger a;
    public NetworkManager networkManager;
    public boolean disconnected = false;
    public EntityPlayer player;
    public NetServerHandler nshInstance;

    public NetServerHandlerProxy(MinecraftServer minecraftserver, NetServerHandler instance) {
        super(minecraftserver, instance.networkManager, instance.player);
        this.nshInstance = instance;
        this.init();
    }

    public NetServerHandlerProxy(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
        this.nshInstance = new NetServerHandler(minecraftserver, networkmanager, entityplayer);
        this.init();
    }

    private void init(){
        a = Logger.getLogger("Minecraft");
        networkManager = nshInstance.networkManager;
        disconnected = nshInstance.disconnected;
        player = nshInstance.player;
    }

    @Override
    public CraftPlayer getPlayer() {
        return nshInstance.getPlayer();
    }

    @Override
    public void a() {
        nshInstance.a();
    }

    @Override
    public void disconnect(String s) {
        nshInstance.disconnect(s);
        this.disconnected = nshInstance.disconnected;
    }

    @Override
    public void a(Packet0KeepAlive packet0keepalive) {
        nshInstance.a(packet0keepalive);
    }

    @Override
    public void a(Packet10Flying packet10flying) {
        nshInstance.a(packet10flying);
    }

    @Override
    public void a(double d0, double d1, double d2, float f, float f1) {
        nshInstance.a(d0, d1, d2, f, f1);
    }

    @Override
    public void teleport(Location dest) {
        nshInstance.teleport(dest);
    }

    @Override
    public void a(Packet14BlockDig packet14blockdig) {
        nshInstance.a(packet14blockdig);
    }

    @Override
    public void a(Packet15Place packet15place) {
        nshInstance.a(packet15place);
    }

    @Override
    public void a(String s, Object[] aobject) {
        nshInstance.a(s, aobject);
        disconnected = nshInstance.disconnected;
    }

    @Override
    public void sendPacket(Packet packet) {
        nshInstance.sendPacket(packet);
    }

    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch) {
        nshInstance.a(packet16blockitemswitch);
    }

    @Override
    public void a(Packet3Chat packet3chat) {
        nshInstance.a(packet3chat);
    }

    @Override
    public boolean chat(String s) {
        return nshInstance.chat(s);
    }

    @Override
    public void a(Packet18ArmAnimation packet18armanimation) {
        nshInstance.a(packet18armanimation);
    }

    @Override
    public void a(Packet19EntityAction packet19entityaction) {
        nshInstance.a(packet19entityaction);
    }

    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect) {
        nshInstance.a(packet255kickdisconnect);
    }

    @Override
    public int lowPriorityCount() {
        return nshInstance.lowPriorityCount();
    }

    @Override
    public void sendMessage(String s) {
        nshInstance.sendMessage(s);
    }

    @Override
    public String getName() {
        return nshInstance.getName();
    }

    @Override
    public void a(Packet7UseEntity packet7useentity) {
        nshInstance.a(packet7useentity);
    }

    @Override
    public void a(Packet9Respawn packet9respawn) {
    	nshInstance.a(packet9respawn);
        player = nshInstance.player;
    }

    @Override
    public void handleContainerClose(Packet101CloseWindow packet101closewindow) {
    	nshInstance.handleContainerClose(packet101closewindow);
    }

    @Override
    public void a(Packet102WindowClick packet102windowclick) {
        nshInstance.a(packet102windowclick);
    }

    @Override
    public void a(Packet107SetCreativeSlot packet107setcreativeslot) {
        nshInstance.a(packet107setcreativeslot);
    }

    @Override
    public void a(Packet108ButtonClick packet108buttonclick) {
    	nshInstance.a(packet108buttonclick);
    }

    @Override
    public void a(Packet106Transaction packet106transaction) {
        nshInstance.a(packet106transaction);
    }

    @Override
    public void a(Packet130UpdateSign packet130updatesign) {
        nshInstance.a(packet130updatesign);
    }

    @Override
    public void a(Packet250CustomPayload packet) {
    	nshInstance.a(packet);
    }

    @Override
    public void onUnhandledPacket(Packet packet) {
    	nshInstance.onUnhandledPacket(packet);
    }

    @Override
    public boolean c() {
        return nshInstance.c();
    }
}