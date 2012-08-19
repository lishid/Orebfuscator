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

package net.minecraftserverhook;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import net.minecraft.server.*;

public class NetServerHandlerProxy extends NetServerHandler
{
    public INetworkManager networkManager;
    public NetServerHandler nshInstance;
    
    public NetServerHandlerProxy(MinecraftServer minecraftserver, NetServerHandler instance)
    {
        super(minecraftserver, instance.networkManager, instance.player);
        this.nshInstance = instance;
        this.init();
    }
    
    public NetServerHandlerProxy(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer)
    {
        super(minecraftserver, networkmanager, entityplayer);
        this.nshInstance = new NetServerHandler(minecraftserver, networkmanager, entityplayer);
        this.init();
    }
    
    private void init()
    {
        networkManager = nshInstance.networkManager;
        // minecraftserver.ac().a(this.nshInstance);
    }
    
    @Override
    public CraftPlayer getPlayer()
    {
        return this.nshInstance.getPlayer();
    }
    
    @Override
    public void d()
    {
        this.nshInstance.d();
    }
    
    @Override
    public void disconnect(String s)
    {
        this.nshInstance.disconnect(s);
    }
    
    @Override
    public void a(Packet10Flying packet10flying)
    {
        this.nshInstance.a(packet10flying);
    }
    
    @Override
    public void a(double d0, double d1, double d2, float f, float f1)
    {
        this.nshInstance.a(d0, d1, d2, f, f1);
    }
    
    @Override
    public void teleport(Location dest)
    {
        this.nshInstance.teleport(dest);
    }
    
    @Override
    public void a(Packet14BlockDig packet14blockdig)
    {
        this.nshInstance.a(packet14blockdig);
    }
    
    @Override
    public void a(Packet15Place packet15place)
    {
        this.nshInstance.a(packet15place);
    }
    
    @Override
    public void a(String s, Object[] aobject)
    {
        this.nshInstance.a(s, aobject);
    }
    
    @Override
    public void onUnhandledPacket(Packet packet)
    {
        this.nshInstance.onUnhandledPacket(packet);
    }
    
    @Override
    public void sendPacket(Packet packet)
    {
        this.nshInstance.sendPacket(packet);
    }
    
    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch)
    {
        this.nshInstance.a(packet16blockitemswitch);
    }
    
    @Override
    public void a(Packet3Chat packet3chat)
    {
        this.nshInstance.a(packet3chat);
    }
    
    @Override
    public void chat(String s, boolean async)
    {
        this.nshInstance.chat(s, async);
    }
    
    @Override
    public void a(Packet18ArmAnimation packet18armanimation)
    {
        this.nshInstance.a(packet18armanimation);
    }
    
    @Override
    public void a(Packet19EntityAction packet19entityaction)
    {
        this.nshInstance.a(packet19entityaction);
    }
    
    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect)
    {
        this.nshInstance.a(packet255kickdisconnect);
    }
    
    @Override
    public int lowPriorityCount()
    {
        return this.nshInstance.lowPriorityCount();
    }
    
    @Override
    public void a(Packet7UseEntity packet7useentity)
    {
        this.nshInstance.a(packet7useentity);
    }
    
    @Override
    public void a(Packet205ClientCommand packet205clientcommand)
    {
        this.nshInstance.a(packet205clientcommand);
    }
    
    @Override
    public boolean b()
    {
        return this.nshInstance.b();
    }
    
    @Override
    public void a(Packet9Respawn packet9respawn)
    {
        this.nshInstance.a(packet9respawn);
    }
    
    @Override
    public void handleContainerClose(Packet101CloseWindow packet101closewindow)
    {
        this.nshInstance.handleContainerClose(packet101closewindow);
    }
    
    @Override
    public void a(Packet102WindowClick packet102windowclick)
    {
        this.nshInstance.a(packet102windowclick);
    }
    
    @Override
    public void a(Packet108ButtonClick packet108buttonclick)
    {
        this.nshInstance.a(packet108buttonclick);
    }
    
    @Override
    public void a(Packet107SetCreativeSlot packet107setcreativeslot)
    {
        this.nshInstance.a(packet107setcreativeslot);
    }
    
    @Override
    public void a(Packet106Transaction packet106transaction)
    {
        this.nshInstance.a(packet106transaction);
    }
    
    @Override
    public void a(Packet130UpdateSign packet130updatesign)
    {
        this.nshInstance.a(packet130updatesign);
    }
    
    @Override
    public void a(Packet0KeepAlive packet0keepalive)
    {
        this.nshInstance.a(packet0keepalive);
    }
    
    @Override
    public boolean a()
    {
        return this.nshInstance.a();
    }
    
    @Override
    public void a(Packet202Abilities packet202abilities)
    {
        this.nshInstance.a(packet202abilities);
    }
    
    @Override
    public void a(Packet203TabComplete packet203tabcomplete)
    {
        this.nshInstance.a(packet203tabcomplete);
    }
    
    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance)
    {
        this.nshInstance.a(packet204localeandviewdistance);
    }
    
    @Override
    public void a(Packet250CustomPayload packet250custompayload)
    {
        this.nshInstance.a(packet250custompayload);
    }
}