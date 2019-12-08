/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.types;

public class ChunkCoord {

	public int x;
	public int z;

	public ChunkCoord(int x, int z) {
		this.x = x;
		this.z = z;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof ChunkCoord)) {
			return false;
		}

		ChunkCoord object = (ChunkCoord) other;

		return this.x == object.x && this.z == object.z;
	}

	@Override
	public int hashCode() {
		return this.x ^ this.z;
	}

	@Override
	public String toString() {
		return this.x + " " + this.z;
	}
}
