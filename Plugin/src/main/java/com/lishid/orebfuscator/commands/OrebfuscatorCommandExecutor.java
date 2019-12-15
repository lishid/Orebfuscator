/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.NmsInstance;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.config.WorldConfig;
import com.lishid.orebfuscator.logger.OFCLogger;
import com.lishid.orebfuscator.utils.Globals;
import com.lishid.orebfuscator.utils.MaterialHelper;

public class OrebfuscatorCommandExecutor implements CommandExecutor, TabCompleter {

	private static final String HELP_MESSAGE = "\n§8[§aOFC§8]"
			+ "\n§8[§aOFC§8] §7Usage§8:"
			+ "\n§8[§aOFC§8]"
			+ "\n§8[§aOFC§8] §8/§7ofc §9engine §8<§a1§8, §a2§8>"
			+ "\n§8[§aOFC§8] §8/§7ofc §9initialradius §8<§anumber§8>"
			+ "\n§8[§aOFC§8] §8/§7ofc §9updateradius §8<§anumber§8>"
			+ "\n§8[§aOFC§8] §8/§7ofc §9airgen §8<§anumber§8>"
			+ "\n§8[§aOFC§8] §8/§7ofc §9reload"
			+ "\n§8[§aOFC§8] §8/§7ofc §9status"
			+ "\n§8[§aOFC§8] §8/§7ofc §9clearcache"
			+ "\n§8[§aOFC§8] §8/§7ofc §9use §8<§ablacklist§8, §awhitelist§8>"
			+ "\n§8[§aOFC§8] §8/§7ofc §9enable §8<§avalues§8, §aworldname§8>"
			+ "\n§8[§aOFC§8] §8/§7ofc §9disable §8<§avalues§8, §aworldname§8>"
			+ "\n§8[§aOFC§8]"
			+ "\n§8[§aOFC§8] §7Values§8: §adarknesshide§8, §aop§8, §acache§8,"
			+ "\n§8[§aOFC§8]           §apermissions§8, §aaxr§8, §anotification"
			+ "\n§8[§aOFC§8]";

	private static final List<String> TAB_COMPLETE_ARGUMENTS = Arrays.asList("engine", "initialradius", "updateradius", "airgen", "reload", "status", "clearcache", "use", "enable", "disable");
	private static final Map<String, List<String>> TAB_COMPLETE_BY_ARGUMENTS = new HashMap<String, List<String>>();
	private static final List<String> TAB_COMPLETE_ZERO_TO_NINE = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
	private static final List<String> TAB_COMPLETE_EMPTY_LIST = Arrays.asList();

	private static void loadTabComplete() {
		OrebfuscatorCommandExecutor.TAB_COMPLETE_BY_ARGUMENTS.put("engine", Arrays.asList("1", "2"));
		OrebfuscatorCommandExecutor.TAB_COMPLETE_BY_ARGUMENTS.put("use", Arrays.asList("blacklist", "whitelist"));

		List<String> toggleValues = new ArrayList<String>();
		Bukkit.getWorlds().forEach(world -> toggleValues.add(world.getName()));
		toggleValues.addAll(Arrays.asList("darknesshide", "op", "cache", "permissions", "axr", "notification"));
		OrebfuscatorCommandExecutor.TAB_COMPLETE_BY_ARGUMENTS.put("enable", toggleValues);
		OrebfuscatorCommandExecutor.TAB_COMPLETE_BY_ARGUMENTS.put("disable", toggleValues);
	}

	public OrebfuscatorCommandExecutor() {
		OrebfuscatorCommandExecutor.loadTabComplete();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if ((sender instanceof Player) && !sender.hasPermission("orebfuscator.admin")) {
			OFCLogger.message(sender, "You do not have permissions.");
			return true;
		}

		if (args.length <= 0) {
			sender.sendMessage(OrebfuscatorCommandExecutor.HELP_MESSAGE);
			return true;
		}

		if (args[0].equalsIgnoreCase("engine") && args.length > 1) {
			int engine = Orebfuscator.config.getEngineMode();
			try {
				engine = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				OFCLogger.message(sender, args[1] + " is not a number!");
				return true;
			}
			if (engine != 1 && engine != 2) {
				OFCLogger.message(sender, args[1] + " is not a valid EngineMode!");
				return true;
			} else {
				Orebfuscator.configManager.setEngineMode(engine);
				OFCLogger.message(sender, "Engine set to: " + engine);
				return true;
			}
		} else if (args[0].equalsIgnoreCase("updateradius") && args.length > 1) {
			int radius = Orebfuscator.config.getUpdateRadius();
			try {
				radius = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				OFCLogger.message(sender, args[1] + " is not a number!");
				return true;
			}
			Orebfuscator.configManager.setUpdateRadius(radius);
			OFCLogger.message(sender, "UpdateRadius set to: " + Orebfuscator.config.getUpdateRadius());
			return true;
		} else if (args[0].equalsIgnoreCase("initialradius") && args.length > 1) {
			int radius = Orebfuscator.config.getInitialRadius();
			try {
				radius = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				OFCLogger.message(sender, args[1] + " is not a number!");
				return true;
			}
			Orebfuscator.configManager.setInitialRadius(radius);
			OFCLogger.message(sender, "InitialRadius set to: " + radius);
			return true;
		} else if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
			boolean data = args[0].equalsIgnoreCase("enable");

			if (args[0].equalsIgnoreCase("enable") && args.length == 1) {
				Orebfuscator.configManager.setEnabled(true);
				OFCLogger.message(sender, "Enabled.");
				return true;
			} else if (args[0].equalsIgnoreCase("disable") && args.length == 1) {
				Orebfuscator.configManager.setEnabled(false);
				OFCLogger.message(sender, "Disabled.");
				return true;
			} else if (args.length > 1) {
				if (args[1].equalsIgnoreCase("op")) {
					Orebfuscator.configManager.setNoObfuscationForOps(data);
					OFCLogger.message(sender, "Ops No-Obfuscation " + (data ? "enabled" : "disabled") + ".");
					return true;
				} else if (args[1].equalsIgnoreCase("perms") || args[1].equalsIgnoreCase("permissions")) {
					Orebfuscator.configManager.setNoObfuscationForPermission(data);
					OFCLogger.message(sender, "Permissions No-Obfuscation " + (data ? "enabled" : "disabled") + ".");
					return true;
				} else if (args[1].equalsIgnoreCase("cache")) {
					Orebfuscator.configManager.setUseCache(data);
					OFCLogger.message(sender, "Cache " + (data ? "enabled" : "disabled") + ".");
					return true;
				} else if (args[1].equalsIgnoreCase("notification")) {
					Orebfuscator.configManager.setLoginNotification(data);
					OFCLogger.message(sender, "Login Notification " + (data ? "enabled" : "disabled") + ".");
					return true;
				}
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			Orebfuscator.instance.reloadOrebfuscatorConfig();
			OFCLogger.message(sender, "Reload complete.");
			return true;
		} else if (args[0].equalsIgnoreCase("status")) {
			String status = Orebfuscator.instance.getIsProtocolLibFound()
					? (Orebfuscator.config.isEnabled() ? "Enabled" : "Disabled")
					: "ProtocolLib is not found! Plugin cannot be enabled.";

			OFCLogger.message(sender,
					"Orebfuscator " + Orebfuscator.instance.getDescription().getVersion() + " is: " + status);
			OFCLogger.message(sender, "Engine Mode: " + Orebfuscator.config.getEngineMode());

			OFCLogger.message(sender, "Caching: " + (Orebfuscator.config.isUseCache() ? "Enabled" : "Disabled"));
			OFCLogger.message(sender,
					"ProximityHider: " + (Orebfuscator.config.isProximityHiderEnabled() ? "Enabled" : "Disabled"));
			OFCLogger.message(sender, "DarknessHideBlocks: "
					+ (Orebfuscator.config.getDefaultWorld().isDarknessHideBlocks() ? "Enabled" : "Disabled"));

			OFCLogger.message(sender, "Initial Obfuscation Radius: " + Orebfuscator.config.getInitialRadius());
			OFCLogger.message(sender, "Update Radius: " + Orebfuscator.config.getUpdateRadius());

			OFCLogger.message(sender, "World by Default: "
					+ (Orebfuscator.config.getDefaultWorld().isEnabled() ? "Enabled" : "Disabled"));

			String worldNames = Orebfuscator.config.getWorldNames();

			OFCLogger.message(sender, "Worlds in List: " + (worldNames.equals("") ? "None" : worldNames));

			return true;
		} else if (args[0].equalsIgnoreCase("clearcache")) {
			try {
				ObfuscatedDataCache.clearCache();
				OFCLogger.message(sender, "Cache cleared.");
			} catch (IOException e) {
				e.printStackTrace();
			}

			return true;
		} else if (args[0].equalsIgnoreCase("obfuscateblocks")) {
			commandObfuscateBlocks(sender, args);
			return true;
		} else if (args[0].equalsIgnoreCase("ph")) {
			commandProximityHider(sender, args);
			return true;
		} else if (args[0].equalsIgnoreCase("lm")) {
			commandListMaterials(sender, args);
			return true;
		} else if (args[0].equalsIgnoreCase("tp")) {
			commandTransparentBlocks(sender, args);
			return true;
		}

		sender.sendMessage(OrebfuscatorCommandExecutor.HELP_MESSAGE);
		return true;
	}

	private static void commandObfuscateBlocks(CommandSender sender, String[] args) {
		if (args.length == 1) {
			OFCLogger.message(sender, ChatColor.RED + "World is required parameter.");
			return;
		}

		String worldName = args[1];
		World world = Bukkit.getWorld(worldName);

		if (world == null) {
			OFCLogger.message(sender, ChatColor.RED + "Specified world is not found.");
			return;
		}

		if (args.length > 2) {
			Material material = Material.getMaterial(args[2]);

			if (material == null) {
				OFCLogger.message(sender, ChatColor.RED + "Specified material is not found.");
			} else {
				int materialId = NmsInstance.get().getMaterialIds(material).iterator().next();

				if ((Orebfuscator.configManager.getWorld(world).getObfuscatedBits(materialId) & Globals.MASK_OBFUSCATE) != 0)
					OFCLogger.message(sender, material.name() + ": " + ChatColor.GREEN + "obfuscate");
				else
					OFCLogger.message(sender, material.name() + ": " + ChatColor.RED + "not obfuscate");
			}

			return;
		}

		Material[] materials = Material.values();
		ArrayList<String> blockNames = new ArrayList<>();

		for (Material material : materials) {
			if (material.isBlock()) {
				int blockId = NmsInstance.get().getMaterialIds(material).iterator().next();
				int bits = Orebfuscator.configManager.getWorld(world).getObfuscatedBits(blockId);

				if (bits != 0) {
					blockNames.add(material.name() + " " + ChatColor.WHITE + bits);
				}
			}
		}

		Collections.sort(blockNames);

		StringBuilder blocks = new StringBuilder();
		blocks.append("Obfuscate blocks:");

		if (blockNames.size() > 0) {
			for (String blockName : blockNames) {
				blocks.append(ChatColor.GREEN + "\n - " + blockName);
			}
		} else {
			blocks.append(" None");
		}

		OFCLogger.message(sender, blocks.toString());
	}

	private static void commandProximityHider(CommandSender sender, String[] args) {
		if (args.length == 1) {
			OFCLogger.message(sender, ChatColor.RED + "World is required parameter.");
			return;
		}

		WorldConfig worldConfig = null;
		String worldName = args[1];

		if (worldName.startsWith(":")) {
			if (worldName.equalsIgnoreCase(":default")) {
				worldConfig = Orebfuscator.config.getDefaultWorld();
			} else if (worldName.equalsIgnoreCase(":normal")) {
				worldConfig = Orebfuscator.config.getNormalWorld();
			} else if (worldName.equalsIgnoreCase(":nether")) {
				worldConfig = Orebfuscator.config.getNetherWorld();
			} else if (worldName.equalsIgnoreCase(":end")) {
				worldConfig = Orebfuscator.config.getEndWorld();
			}
		} else {
			World world = Bukkit.getWorld(worldName);
			worldConfig = Orebfuscator.configManager.getWorld(world);
		}

		if (worldConfig == null) {
			OFCLogger.message(sender, ChatColor.RED + "Specified world is not found.");
			return;
		}

		OFCLogger.message(sender, "ProximityHider: " + (worldConfig.getProximityHiderConfig().isEnabled() ? "Enabled" : "Disabled"));

		StringBuilder blocks = new StringBuilder();
		blocks.append("Obfuscate blocks:");

		Set<Integer> blockIds = worldConfig.getProximityHiderConfig().getProximityHiderBlocks();

		if (blockIds.size() > 0) {
			ArrayList<String> blockNames = new ArrayList<>();

			for (int id : blockIds) {
				blockNames.add(MaterialHelper.getById(id).name());
			}

			Collections.sort(blockNames);

			blockNames.forEach(blockName -> blocks.append("\n - " + blockName));
		} else {
			blocks.append(" None");
		}

		OFCLogger.message(sender, blocks.toString());
	}

	private static void commandListMaterials(CommandSender sender, String[] args) {
		Material[] materials = Material.values();

		List<String> blockNames = new ArrayList<>();

		for (Material material : materials) {
			if (material.isBlock())
				NmsInstance.get().getMaterialIds(material).stream().sorted().forEach(id -> blockNames.add(material.name() + " = " + id));
		}

		OFCLogger.message(sender, blockNames.isEmpty() ? "" : (" - " + blockNames.stream().sorted().collect(Collectors.joining("\n - "))));
	}

	private static void commandTransparentBlocks(CommandSender sender, String[] args) {
		Material[] materials = Material.values();

		List<String> transparentBlockNames = new ArrayList<>();
		List<String> nonTransparentBlockNames = new ArrayList<>();

		for (Material material : materials) {
			if (material.isBlock()) {
				int blockId = NmsInstance.get().getMaterialIds(material).iterator().next();
				boolean isTransparent = Orebfuscator.config.isBlockTransparent(blockId);

				if (isTransparent) {
					transparentBlockNames.add(material.name());
				} else {
					nonTransparentBlockNames.add(material.name());
				}
			}
		}

		Collections.sort(transparentBlockNames);
		Collections.sort(nonTransparentBlockNames);

		StringBuilder blocks = new StringBuilder();
		blocks.append("Transparent blocks:");
		transparentBlockNames.forEach(blockName -> blocks.append("\n - " + blockName));

		blocks.append("\nNon-Transparent blocks:");
		nonTransparentBlockNames.forEach(blockName -> blocks.append("\n - " + blockName));

		OFCLogger.message(sender, blocks.toString());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length > 1) {
			String arg = args[0].toLowerCase();
			switch (arg) {
			case "initialradius":
			case "updateradius":
			case "airgen":
				if (args.length > 1) {
					return OrebfuscatorCommandExecutor.TAB_COMPLETE_ZERO_TO_NINE.stream().map(number -> args[1] + number).collect(Collectors.toList());
				}

				return OrebfuscatorCommandExecutor.TAB_COMPLETE_ZERO_TO_NINE;

			default:
				return OrebfuscatorCommandExecutor.TAB_COMPLETE_BY_ARGUMENTS.getOrDefault(arg, OrebfuscatorCommandExecutor.TAB_COMPLETE_EMPTY_LIST);
			}
		}

		String arg = args.length > 0 ? args[0] : null;

		if (arg == null) {
			return OrebfuscatorCommandExecutor.TAB_COMPLETE_ARGUMENTS;
		}

		List<String> result = new ArrayList<String>();
		for (String check : OrebfuscatorCommandExecutor.TAB_COMPLETE_ARGUMENTS) {
			if (check.startsWith(arg)) {
				result.add(check);
			}
		}

		return result;
	}
}