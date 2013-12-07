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

package com.lishid.orebfuscator.internal.v1_4_R1;

import java.io.DataInput;
import java.io.DataOutput;

import com.lishid.orebfuscator.internal.INBT;

//Volatile
import net.minecraft.server.v1_4_R1.*;

public class NBT implements INBT {
    NBTTagCompound nbt = new NBTTagCompound();

    @Override
    public void reset() {
        nbt = new NBTTagCompound();
    }

    @Override
    public void setInt(String tag, int value) {
        nbt.setInt(tag, value);
    }

    @Override
    public void setLong(String tag, long value) {
        nbt.setLong(tag, value);
    }

    @Override
    public void setBoolean(String tag, boolean value) {
        nbt.setBoolean(tag, value);
    }

    @Override
    public void setByteArray(String tag, byte[] value) {
        nbt.setByteArray(tag, value);
    }

    @Override
    public void setIntArray(String tag, int[] value) {
        nbt.setIntArray(tag, value);
    }

    @Override
    public int getInt(String tag) {
        return nbt.getInt(tag);
    }

    @Override
    public long getLong(String tag) {
        return nbt.getLong(tag);
    }

    @Override
    public boolean getBoolean(String tag) {
        return nbt.getBoolean(tag);
    }

    @Override
    public byte[] getByteArray(String tag) {
        return nbt.getByteArray(tag);
    }

    @Override
    public int[] getIntArray(String tag) {
        return nbt.getIntArray(tag);
    }

    @Override
    public void Read(DataInput stream) {
        nbt = NBTCompressedStreamTools.a(stream);
    }

    @Override
    public void Write(DataOutput stream) {
        NBTCompressedStreamTools.a(nbt, stream);
    }

}
