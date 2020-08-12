package net.imprex.orebfuscator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

public final class MinecraftVersion {

	private static final Pattern VERSION_PATTERN = Pattern.compile("org\\.bukkit\\.craftbukkit\\.(v(\\d+)_(\\d+)_R(\\d+))");

	private final String nmsVersion;
	private final int majorVersion;
	private final int minorVersion;

	private MinecraftVersion() {
		String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
		Matcher matcher = VERSION_PATTERN.matcher(craftBukkitPackage);

		if (!matcher.find()) {
			throw new RuntimeException("Can't parse craftbukkit package version " + craftBukkitPackage);
		}

		this.nmsVersion = matcher.group(1);
		this.majorVersion = Integer.parseInt(matcher.group(2));
		this.minorVersion = Integer.parseInt(matcher.group(3));
	}

	private static final MinecraftVersion VERSION = new MinecraftVersion();

	public static String getNmsVersion() {
		return VERSION.nmsVersion;
	}

	public static int getMajorVersion() {
		return VERSION.majorVersion;
	}

	public static int getMinorVersion() {
		return VERSION.minorVersion;
	}
}
