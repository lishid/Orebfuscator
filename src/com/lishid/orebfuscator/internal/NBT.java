package com.lishid.orebfuscator.internal;

import java.io.DataInput;
import java.io.DataOutput;

import net.minecraft.server.v1_4_5.*;

public class NBT
{
    NBTTagCompound nbt = new NBTTagCompound();
    
    public void setInt(String tag, int value)
    {
        nbt.setInt(tag, value);
    }
    
    public void setBoolean(String tag, boolean value)
    {
        nbt.setBoolean(tag, value);
    }
    
    public void setLong(String tag, long value)
    {
        nbt.setLong(tag, value);
    }
    
    public void Read(DataInput stream)
    {
        NBTCompressedStreamTools.a(stream);
    }
    
    public void Write(DataOutput stream)
    {
        NBTCompressedStreamTools.a(nbt, stream);
    }
}
