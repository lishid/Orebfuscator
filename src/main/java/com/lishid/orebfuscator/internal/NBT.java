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

import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

//Volatile

public class NBT {
    NBTTagCompound nbt = new NBTTagCompound();

    public void reset() {
        nbt = new NBTTagCompound();
    }

    public void setInt(String tag, int value) {
        nbt.setInt(tag, value);
    }

    public void setLong(String tag, long value) {
        nbt.setLong(tag, value);
    }

    public void setBoolean(String tag, boolean value) {
        nbt.setBoolean(tag, value);
    }

    public void setByteArray(String tag, byte[] value) {
        nbt.setByteArray(tag, value);
    }

    public void setIntArray(String tag, int[] value) {
        nbt.setIntArray(tag, value);
    }

    public int getInt(String tag) {
        return nbt.getInt(tag);
    }

    public long getLong(String tag) {
        return nbt.getLong(tag);
    }

    public boolean getBoolean(String tag) {
        return nbt.getBoolean(tag);
    }

    public byte[] getByteArray(String tag) {
        return nbt.getByteArray(tag);
    }

    public int[] getIntArray(String tag) {
        return nbt.getIntArray(tag);
    }

    public void Read(DataInput stream) throws IOException {
        nbt = NBTCompressedStreamTools.a((DataInputStream) stream);
    }

    public void Write(DataOutput stream) throws IOException {
        NBTCompressedStreamTools.a(nbt, stream);
    }

}
