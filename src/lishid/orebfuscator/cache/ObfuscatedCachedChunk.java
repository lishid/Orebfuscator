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

public class ObfuscatedCachedChunk {
	File path;
	File hashPath;
	int x;
	int z;
	public int initialRadius;
	public boolean proximityHider;
	public byte[] data;
	public int[] proximityBlockList;
	
	public ObfuscatedCachedChunk(File file, int x, int z, int initialRadius, boolean proximityHider)
	{
		this.x = x;
		this.z = z;
		this.initialRadius = initialRadius;
		this.proximityHider = proximityHider;
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
            DataInputStream stream = ObfuscatedDataCache.getInputStream(hashPath, x, z);
            if(stream != null)
            {
	            NBTTagCompound nbt = NBTCompressedStreamTools.a((DataInput)stream);
	            
	            //Check if data makes sense
	            if(nbt.getInt("X") != x || nbt.getInt("Z") != z || initialRadius != nbt.getInt("IR") || proximityHider != nbt.getBoolean("PH"))
	            	return 0L;
	            
	            //Return data
	            return nbt.getLong("Hash");
            }
        } catch (Exception e) {
        	Orebfuscator.log("Error reading Orebfuscator Chunk cache hash: " + e.getMessage());
        }
		
		return 0L;
	}
	
	public void getDataAndProximityList()
	{
        try {
            DataInputStream stream = ObfuscatedDataCache.getInputStream(path, x, z);
            if(stream != null)
            {
	            NBTTagCompound nbt = NBTCompressedStreamTools.a((DataInput)stream);
	            
	            //Check if data makes sense
	            if(nbt.getInt("X") != x || nbt.getInt("Z") != z || initialRadius != nbt.getInt("IR") || proximityHider != nbt.getBoolean("PH"))
	            	return;
	            
	            //Retrieve data
	            data = nbt.getByteArray("Data");
	            if(proximityHider)
	            	proximityBlockList = nbt.getIntArray("ProximityBlockList");
	            else
	            	proximityBlockList = new int[0];
            }
        } catch (Exception e) {
        	Orebfuscator.log("Error reading Orebfuscator Chunk cache data: " + e.getMessage());
        }
	}
	
	public void Write(long hash, byte[] data, int[] proximityBlockList) {
		setHash(hash);
		setData(data, proximityBlockList);
	}
	
	public void setHash(long hash) {
		try {
		    NBTTagCompound nbt = new NBTTagCompound();
		    nbt.setInt("X", x);
		    nbt.setInt("Z", z);
		    nbt.setInt("IR", initialRadius);
		    nbt.setBoolean("PH", proximityHider);
		    nbt.setLong("Hash", hash);

			DataOutputStream stream = ObfuscatedDataCache.getOutputStream(hashPath, x, z);
		    NBTCompressedStreamTools.a(nbt, (DataOutput)stream);
		    stream.close();
		} catch (Exception e) {
			Orebfuscator.log("Error writing Orebfuscator Chunk cache hash: " + e.getMessage());
		}
	}
	
	public void setData(byte[] data, int[] proximityBlockList) {
		try {
		    NBTTagCompound nbt = new NBTTagCompound();
		    nbt.setInt("X", x);
		    nbt.setInt("Z", z);
		    nbt.setInt("IR", initialRadius);
		    nbt.setBoolean("PH", proximityHider);
		    nbt.setByteArray("Data", data);
		    nbt.setIntArray("ProximityBlockList", proximityBlockList);

			DataOutputStream stream = ObfuscatedDataCache.getOutputStream(path, x, z);
		    NBTCompressedStreamTools.a(nbt, (DataOutput)stream);
		    stream.close();
		} catch (Exception e) {
			Orebfuscator.log("Error writing Orebfuscator Chunk cache data: " + e.getMessage());
		}
	}
}