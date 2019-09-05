package com.lishid.orebfuscator.logger;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.lishid.orebfuscator.utils.Globals;

public class OFCLogger {

	public static final Logger logger = Logger.getLogger("Minecraft.OFC");

	/**
	 * Log an information
	 */
	public static void log(String text) {
		logger.info(Globals.LogPrefix + text);
	}

	/**
	 * Log an error
	 */
	public static void log(Throwable e) {
		logger.severe(Globals.LogPrefix + e.toString());
		e.printStackTrace();
	}

	/**
	 * Send a message to a player
	 */
	public static void message(CommandSender target, String message) {
		target.sendMessage(String.format("%s%s%s", ChatColor.AQUA, Globals.LogPrefix, message));
	}
}