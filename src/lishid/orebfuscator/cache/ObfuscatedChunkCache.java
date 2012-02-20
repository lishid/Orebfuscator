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

package lishid.orebfuscator.cache;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;

import lishid.orebfuscator.Orebfuscator;

import net.minecraft.server.NBTCompressedStreamTools;
import net.minecraft.server.NBTTagCompound;

public class ObfuscatedChunkCache {
	File path;
	File hashPath;
	int x;
	int z;
	public int initialRadius;
	
	public ObfuscatedChunkCache(File file, int x, int z, int initialRadius)
	{
		this.x = x;
		this.z = z;
		this.initialRadius = initialRadius;
		this.path = new File(file, "data");
		this.hashPath = new File(file, "hash");
		path.mkdirs();
		hashPath.mkdirs();
	}
	
	public void Invalidate()
	{
		setHash(0L);
	}
	
	public long getHash()
	{
        try {
            DataInputStream stream = ObfuscatedRegionFileCache.getInputStream(hashPath, x, z);
            if(stream != null)
            {
	            NBTTagCompound nbt = NBTCompressedStreamTools.a((DataInput)stream);
	            
	            //Check if data makes sense
	            if(nbt.getInt("X") != x || nbt.getInt("Z") != z || initialRadius != nbt.getInt("IR"))
	            	return 0L;
	            
	            //Return data
	            return nbt.getLong("Hash");
            }
        } catch (Exception e) {
        	Orebfuscator.log("Error reading Orebfuscator Chunk cache hash: " + e.getMessage());
        }
		
		return 0L;
	}
	
	public byte[] getData()
	{
        try {
            DataInputStream stream = ObfuscatedRegionFileCache.getInputStream(path, x, z);
            if(stream != null)
            {
	            NBTTagCompound nbt = NBTCompressedStreamTools.a((DataInput)stream);
	            
	            //Check if data makes sense
	            if(nbt.getInt("X") != x || nbt.getInt("Z") != z || initialRadius != nbt.getInt("IR"))
	            	return null;
	            
	            //Return data
	            return nbt.getByteArray("Data");
            }
        } catch (Exception e) {
        	Orebfuscator.log("Error reading Orebfuscator Chunk cache data: " + e.getMessage());
        }
		
		return null;
	}
	
	public void Write(long hash, byte[] data) {
		setHash(hash);
		setData(data);
	}
	
	public void setHash(long hash) {
		try {
		    NBTTagCompound nbt = new NBTTagCompound();
		    nbt.setInt("X", x);
		    nbt.setInt("Z", z);
		    nbt.setInt("IR", initialRadius);
		    nbt.setLong("Hash", hash);

			DataOutputStream stream = ObfuscatedRegionFileCache.getOutputStream(hashPath, x, z);
		    NBTCompressedStreamTools.a(nbt, (DataOutput)stream);
		    stream.close();
		} catch (Exception e) {
			Orebfuscator.log("Error writting Orebfuscator Chunk cache hash: " + e.getMessage());
		}
	}
	
	public void setData(byte[] data) {
		try {
		    NBTTagCompound nbt = new NBTTagCompound();
		    nbt.setInt("X", x);
		    nbt.setInt("Z", z);
		    nbt.setInt("IR", initialRadius);
		    nbt.setByteArray("Data", data);

			DataOutputStream stream = ObfuscatedRegionFileCache.getOutputStream(path, x, z);
		    NBTCompressedStreamTools.a(nbt, (DataOutput)stream);
		    stream.close();
		} catch (Exception e) {
			Orebfuscator.log("Error writting Orebfuscator Chunk cache data: " + e.getMessage());
		}
	}
}