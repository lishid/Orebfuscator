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

import java.io.DataInput;
import java.io.DataOutput;

public interface INBT {
    public void reset();

    public void setInt(String tag, int value);

    public void setLong(String tag, long value);

    public void setBoolean(String tag, boolean value);

    public void setByteArray(String tag, byte[] value);

    public void setIntArray(String tag, int[] value);

    public int getInt(String tag);

    public long getLong(String tag);

    public boolean getBoolean(String tag);

    public byte[] getByteArray(String tag);

    public int[] getIntArray(String tag);

    public void Read(DataInput stream);

    public void Write(DataOutput stream);
}
