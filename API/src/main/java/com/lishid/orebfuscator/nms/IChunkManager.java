/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms;

import java.util.HashSet;

import org.bukkit.entity.Player;

public interface IChunkManager {
	boolean resendChunk(int chunkX, int chunkZ, HashSet<Player> affectedPlayers);
}
