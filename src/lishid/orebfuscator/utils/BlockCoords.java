package lishid.orebfuscator.utils;

public class BlockCoords {
	int x;
	int y;
	int z;
	int index;
	BlockCoords(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	BlockCoords(int index, int x, int y, int z)
	{
		this.index = index;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static boolean Equals(BlockCoords a, BlockCoords b)
	{
		if(a == null || b == null)
			return false;
		return a.x == b.x && a.y == b.y && a.z == b.z;
	}
}
