/**
 * @author lishid
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.nms194;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.server.v1_9_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import com.lishid.orebfuscator.nms.INBT;

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
    public void Read(DataInput stream) throws IOException {
        nbt = NBTCompressedStreamTools.a((DataInputStream) stream);
    }

    @Override
    public void Write(DataOutput stream) throws IOException {
        NBTCompressedStreamTools.a(nbt, stream);
    }
}