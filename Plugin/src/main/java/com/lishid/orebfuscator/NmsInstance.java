/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator;

import com.lishid.orebfuscator.logger.OFCLogger;
import com.lishid.orebfuscator.nms.INmsManager;

public class NmsInstance {

	private static final String currentServerVersion = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	private static final INmsManager instance = NmsInstance.createNmsManager();

	private static INmsManager createNmsManager() {
		OFCLogger.log("Searching NMS protocol for server version \"" + currentServerVersion + "\"!");

		INmsManager nmsManager = null;

		switch (currentServerVersion) {
		case "v1_14_R1":
			nmsManager = new com.lishid.orebfuscator.nms.v1_14_R1.NmsManager();
			break;
		case "v1_13_R2":
			nmsManager = new com.lishid.orebfuscator.nms.v1_13_R2.NmsManager();
			break;
		case "v1_13_R1":
			nmsManager = new com.lishid.orebfuscator.nms.v1_13_R1.NmsManager();
			break;
		case "v1_12_R1":
			nmsManager = new com.lishid.orebfuscator.nms.v1_12_R1.NmsManager();
			break;
		case "v1_11_R1":
			nmsManager = new com.lishid.orebfuscator.nms.v1_11_R1.NmsManager();
			break;
		case "v1_10_R1":
			nmsManager = new com.lishid.orebfuscator.nms.v1_10_R1.NmsManager();
			break;
		case "v1_9_R2":
			nmsManager = new com.lishid.orebfuscator.nms.v1_9_R2.NmsManager();
			break;
		case "v1_9_R1":
			nmsManager = new com.lishid.orebfuscator.nms.v1_9_R1.NmsManager();
			break;
		}

		if (nmsManager != null) {
			OFCLogger.log("NMS protocol for server version \"" + currentServerVersion + "\" found!");
			return nmsManager;
		}

		throw new NullPointerException("Server version \"" + currentServerVersion + "\" is currently not supported!");
	}

	public static INmsManager get() {
		return NmsInstance.instance;
	}

	public static String getServerVersion() {
		return NmsInstance.currentServerVersion;
	}
}
