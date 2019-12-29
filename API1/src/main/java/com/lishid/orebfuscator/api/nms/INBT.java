/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.api.nms;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface INBT {

	void reset();

	void setInt(String tag, int value);

	void setLong(String tag, long value);

	void setByteArray(String tag, byte[] value);

	void setIntArray(String tag, int[] value);

	int getInt(String tag);

	long getLong(String tag);

	byte[] getByteArray(String tag);

	int[] getIntArray(String tag);

	void Read(DataInput stream) throws IOException;

	void Write(DataOutput stream) throws IOException;
}
