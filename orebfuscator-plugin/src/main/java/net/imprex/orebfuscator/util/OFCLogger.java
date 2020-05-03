package net.imprex.orebfuscator.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.lishid.orebfuscator.utils.Globals;

public class OFCLogger {

	public static final Logger LOGGER = Logger.getLogger("Minecraft.OFC");

	public static void warn(String message) {
		OFCLogger.LOGGER.warning(message);
	}

	/**
	 * Log an information
	 */
	public static void log(String message) {
		OFCLogger.LOGGER.info(Globals.LOG_PREFIX + message);
	}

	/**
	 * Log with a specified level
	 */
	public static void log(Level level, String message) {
		OFCLogger.LOGGER.log(level, message);
	}

	/**
	 * Log an error
	 */
	public static void log(Throwable e) {
		OFCLogger.LOGGER.severe(Globals.LOG_PREFIX + e.toString());
		e.printStackTrace();
	}
}