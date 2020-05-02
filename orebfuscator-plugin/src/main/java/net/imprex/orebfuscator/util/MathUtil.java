package net.imprex.orebfuscator.util;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Location;
import org.bukkit.World;

import net.imprex.orebfuscator.NmsInstance;
import net.imprex.orebfuscator.nms.BlockStateHolder;

public class MathUtil {

	public static boolean areAjacentBlocksTransparent(World world, int x, int y, int z, boolean check, int depth) {
		if (y >= world.getMaxHeight() || y < 0) {
			return true;
		}

		if (check) {
			int blockData = NmsInstance.get().loadChunkAndGetBlockId(world, x, y, z);
			if (blockData >= 0 && MaterialUtil.isTransparent(blockData)) {
				return true;
			}
		}

		if (depth == 0) {
			return false;
		}

		if (MathUtil.areAjacentBlocksTransparent(world, x, y + 1, z, true, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksTransparent(world, x, y - 1, z, true, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksTransparent(world, x + 1, y, z, true, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksTransparent(world, x - 1, y, z, true, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksTransparent(world, x, y, z + 1, true, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksTransparent(world, x, y, z - 1, true, depth - 1)) {
			return true;
		}

		return false;
	}

	public static boolean areAjacentBlocksBright(World world, int x, int y, int z, int depth) {
		if (NmsInstance.get().getBlockLightLevel(world, x, y, z) > 0) {
			return true;
		}

		if (depth == 0) {
			return false;
		}

		if (MathUtil.areAjacentBlocksBright(world, x, y + 1, z, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksBright(world, x, y - 1, z, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksBright(world, x + 1, y, z, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksBright(world, x - 1, y, z, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksBright(world, x, y, z + 1, depth - 1)) {
			return true;
		}
		if (MathUtil.areAjacentBlocksBright(world, x, y, z - 1, depth - 1)) {
			return true;
		}

		return false;
	}

	private static ConcurrentLinkedQueue<Long> avgTimes = new ConcurrentLinkedQueue<>();
	private static double calls = 0;
	private static DecimalFormat formatter = new DecimalFormat("###,###,###,###.00");

	public static boolean doFastCheck(Location block, Location eyes, World player) {
		long time = System.nanoTime();
		try {
			return doFastCheck0(block, eyes, player);
		} finally {
			long diff = System.nanoTime() - time;

			avgTimes.add(diff);
			if (avgTimes.size() > 1000) {
				avgTimes.poll();
			}

			if (calls++ % 100 == 0) {
				System.out.println("Fast avg: "
						+ formatter.format(
								((double) avgTimes.stream().reduce(0L, Long::sum) / (double) avgTimes.size()) / 1000D)
						+ "μs");
			}
		}
	}

	/**
	 * Basic idea here is to take some rays from the considered block to the
	 * player's eyes, and decide if any of those rays can reach the eyes unimpeded.
	 *
	 * @param block  the starting block
	 * @param eyes   the destination eyes
	 * @param player the player world we are testing for
	 * @return true if unimpeded path, false otherwise
	 */
	public static boolean doFastCheck0(Location block, Location eyes, World player) {
		double ex = eyes.getX();
		double ey = eyes.getY();
		double ez = eyes.getZ();
		double x = block.getBlockX();
		double y = block.getBlockY();
		double z = block.getBlockZ();
		return // midfaces
		MathUtil.fastAABBRayCheck(x, y, z, x, y + 0.5, z + 0.5, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 0.5, y, z + 0.5, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 0.5, y + 0.5, z, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 0.5, y + 1.0, z + 0.5, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 0.5, y + 0.5, z + 1.0, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 1.0, y + 0.5, z + 0.5, ex, ey, ez, player) ||
				// corners
				MathUtil.fastAABBRayCheck(x, y, z, x, y, z, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 1, y, z, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x, y + 1, z, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 1, y + 1, z, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x, y, z + 1, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 1, y, z + 1, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x, y + 1, z + 1, ex, ey, ez, player)
				|| MathUtil.fastAABBRayCheck(x, y, z, x + 1, y + 1, z + 1, ex, ey, ez, player);
	}

	public static boolean fastAABBRayCheck(double bx, double by, double bz, double x, double y, double z, double ex,
			double ey, double ez, World world) {
		double fx = ex - x;
		double fy = ey - y;
		double fz = ez - z;
		double absFx = Math.abs(fx);
		double absFy = Math.abs(fy);
		double absFz = Math.abs(fz);
		double s = Math.max(absFx, Math.max(absFy, absFz));

		if (s < 1) {
			return true; // on top / inside
		}

		double lx, ly, lz;

		fx = fx / s; // units of change along vector
		fy = fy / s;
		fz = fz / s;

		while (s > 0) {
			ex = ex - fx; // move along vector, we start _at_ the eye and move towards b
			ey = ey - fy;
			ez = ez - fz;
			lx = Math.floor(ex);
			ly = Math.floor(ey);
			lz = Math.floor(ez);
			if (lx == bx && ly == by && lz == bz) {
				return true; // we've reached our starting block, don't test it.
			}
			BlockStateHolder between = NmsInstance.get().getBlockState(world, (int) lx, (int) ly, (int) lz);
			if (between != null && !MaterialUtil.isTransparent(between.getBlockId())) {
				return false; // fail on first hit, this ray is "blocked"
			}
			s--; // we stop
		}
		return true;
	}
}