package com.lishid.orebfuscator.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandSenderUtil {

	/**
	 * Send a message to a player
	 */
	public static void sendMessage(CommandSender target, String message) {
		target.sendMessage(ChatColor.AQUA + Globals.LOG_PREFIX + message);
	}
}
