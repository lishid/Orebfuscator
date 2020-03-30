package net.imprex.orebfuscator.nms;

import org.bukkit.World;

import net.imprex.orebfuscator.util.BlockCoords;

public abstract class AbstractBlockState<T> extends BlockCoords implements BlockStateHolder {

	public final World world;

	protected final T state;

	public AbstractBlockState(int x, int y, int z, World world, T state) {
		super(x, y, z);

		this.world = world;
		this.state = state;
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getZ() {
		return this.z;
	}
}
