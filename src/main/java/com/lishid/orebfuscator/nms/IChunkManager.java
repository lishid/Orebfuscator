/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms;

public interface IChunkManager {
	boolean canResendChunk(int chunkX, int chunkZ);
	void resendChunk(int chunkX, int chunkZ);
}
