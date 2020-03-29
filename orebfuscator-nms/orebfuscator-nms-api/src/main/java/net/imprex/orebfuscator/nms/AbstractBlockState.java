package net.imprex.orebfuscator.nms;

import org.bukkit.World;

import net.imprex.orebfuscator.util.BlockCoords;

public abstract class AbstractBlockState<T> extends BlockCoords {

	public final World world;

	protected final T state;

	public AbstractBlockState(int x, int y, int z, World world, T state) {
		super(x, y, z);

		this.world = world;
		this.state = state;
	}

	public abstract int getBlockId();

	public abstract void notifyBlockChange();
}
