/**
 * @author lishid
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_13_R2;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.server.v1_13_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_13_R2.NBTTagCompound;

import com.lishid.orebfuscator.nms.INBT;

public class NBT implements INBT {
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
