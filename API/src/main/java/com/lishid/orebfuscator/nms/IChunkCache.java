/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public interface IChunkCache {

	DataInputStream getInputStream(File folder, int x, int z) throws IOException;

	DataOutputStream getOutputStream(File folder, int x, int z) throws IOException;

	void closeCacheFiles();
}
