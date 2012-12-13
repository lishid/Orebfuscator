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

package com.lishid.orebfuscator.internal;

import org.bukkit.Server;

import com.lishid.orebfuscator.Orebfuscator;

public class InternalAccessor
{
    public static InternalAccessor Instance;
    private String version;
    
    /*
     * Returns false if version not supported
     */
    public static boolean Initialize(Server server)
    {
        Instance = new InternalAccessor();
        String packageName = server.getClass().getPackage().getName();
        Instance.version = packageName.substring(packageName.lastIndexOf('.') + 1);
        
        try
        {
            Class.forName("com.lishid.orebfuscator.internal." + Instance.version + ".PlayerHook");
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    public void PrintError()
    {
        Orebfuscator.log("Orebfuscator encountered an error with the CraftBukkit version \"" + Instance.version + "\". Please look for an updated version of Orebfuscator.");
    }
    
    public INBT newNBT()
    {
        return (INBT) createObject(INBT.class, "NBT");
    }
    
    public IChunkCache newChunkCache()
    {
        return (IChunkCache) createObject(IChunkCache.class, "ChunkCache");
    }
    
    public IPacket51 newPacket51()
    {
        return (IPacket51) createObject(IPacket51.class, "Packet51");
    }
    
    public IPacket56 newPacket56()
    {
        return (IPacket56) createObject(IPacket56.class, "Packet56");
    }
    
    public IPlayerHook newPlayerHook()
    {
        return (IPlayerHook) createObject(IPlayerHook.class, "PlayerHook");
    }
    
    public IBlockTransparency newBlockTransparency()
    {
        return (IBlockTransparency) createObject(IBlockTransparency.class, "BlockTransparency");
    }
    
    public IMinecraftWorldServer newMinecraftWorldServer()
    {
        return (IMinecraftWorldServer) createObject(IMinecraftWorldServer.class, "MinecraftWorldServer");
    }
    
    private Object createObject(Class<? extends Object> assignableClass, String className)
    {
        try
        {
            Class<?> internalClass = Class.forName("com.lishid.orebfuscator.internal." + version + "." + className);
            if (assignableClass.isAssignableFrom(internalClass))
            {
                return internalClass.getConstructor().newInstance();
            }
        }
        catch (Exception e)
        {
            PrintError();
            Orebfuscator.log(e);
        }
        
        return null;
    }
}
