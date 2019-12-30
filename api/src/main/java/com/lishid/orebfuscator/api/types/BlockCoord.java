/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.api.types;

public class BlockCoord {

	public int x;
	public int y;
	public int z;

	public BlockCoord(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof BlockCoord)) {
			return false;
		}

		BlockCoord object = (BlockCoord) other;

		return this.x == object.x && this.y == object.y && this.z == object.z;
	}

	@Override
	public int hashCode() {
		return this.x ^ this.y ^ this.z;
	}

	@Override
	public String toString() {
		return this.x + " " + this.y + " " + this.z;
	}
}
