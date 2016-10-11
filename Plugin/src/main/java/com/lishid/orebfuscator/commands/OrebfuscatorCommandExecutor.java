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

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.cache.ObfuscatedDataCache;

public class OrebfuscatorCommandExecutor {
    public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ((sender instanceof Player) && !sender.hasPermission("Orebfuscator.admin")) {
            Orebfuscator.message(sender, "You do not have permissions.");
            return true;
        }

        if (args.length <= 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("engine") && args.length > 1) {
            int engine = Orebfuscator.config.getEngineMode();
            try {
                engine = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            if (engine != 1 && engine != 2) {
                Orebfuscator.message(sender, args[1] + " is not a valid EngineMode!");
                return true;
            }
            else {
                Orebfuscator.configManager.setEngineMode(engine);
                Orebfuscator.message(sender, "Engine set to: " + engine);
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("updateradius") && args.length > 1) {
            int radius = Orebfuscator.config.getUpdateRadius();
            try {
                radius = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            Orebfuscator.configManager.setUpdateRadius(radius);
            Orebfuscator.message(sender, "UpdateRadius set to: " + Orebfuscator.config.getUpdateRadius());
            return true;
        }

        else if (args[0].equalsIgnoreCase("initialradius") && args.length > 1) {
            int radius = Orebfuscator.config.getInitialRadius();
            try {
                radius = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            Orebfuscator.configManager.setInitialRadius(radius);
            Orebfuscator.message(sender, "InitialRadius set to: " + radius);
            return true;
        }

        else if (args[0].equalsIgnoreCase("airgen") && args.length > 1) {
            int airgen = Orebfuscator.config.getDefaultWorld().getAirGeneratorMaxChance();
            try {
                airgen = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            Orebfuscator.configManager.setAirGeneratorMaxChance(airgen);
            Orebfuscator.message(sender, "AirGeneratorMaxChance set to: " + airgen);
            return true;
        }

        else if ((args[0].equalsIgnoreCase("proximity") | args[0].equalsIgnoreCase("proximityhider")) && args.length > 1) {
            int ProximityHiderDistance = Orebfuscator.config.getDefaultWorld().getProximityHiderConfig().getDistance();
            try {
                ProximityHiderDistance = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            Orebfuscator.configManager.setProximityHiderDistance(ProximityHiderDistance);
            Orebfuscator.message(sender, "ProximityHider Distance set to: " + ProximityHiderDistance);
            return true;
        }

        else if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
            boolean data = args[0].equalsIgnoreCase("enable");

            if (args[0].equalsIgnoreCase("enable") && args.length == 1) {
                Orebfuscator.configManager.setEnabled(true);
                Orebfuscator.message(sender, "Enabled.");
            }

            else if (args[0].equalsIgnoreCase("disable") && args.length == 1) {
                Orebfuscator.configManager.setEnabled(false);
                Orebfuscator.message(sender, "Disabled.");
            }

            else if (args.length > 1) {
                if (args[1].equalsIgnoreCase("darknesshide")) {
                    Orebfuscator.configManager.setDarknessHideBlocks(data);
                    Orebfuscator.message(sender, "Darkness obfuscation " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("op")) {
                	Orebfuscator.configManager.setNoObfuscationForOps(data);
                    Orebfuscator.message(sender, "Ops No-Obfuscation " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("perms") || args[1].equalsIgnoreCase("permissions")) {
                	Orebfuscator.configManager.setNoObfuscationForPermission(data);
                    Orebfuscator.message(sender, "Permissions No-Obfuscation " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("cache")) {
                	Orebfuscator.configManager.setUseCache(data);
                    Orebfuscator.message(sender, "Cache " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("axr")) {
                	Orebfuscator.configManager.setAntiTexturePackAndFreecam(data);
                    Orebfuscator.message(sender, "AntiTexturePackAndFreecam " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("notification")) {
                	Orebfuscator.configManager.setLoginNotification(data);
                    Orebfuscator.message(sender, "Login Notification " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("world") && args.length > 2) {
                    Orebfuscator.configManager.setWorldEnabled(args[2], data);
                    Orebfuscator.message(sender, "World \"" + args[2] + "\" obfuscation " + (data ? "enabled" : "disabled") + ".");
                }
            }
        }
        
        else if (args[0].equalsIgnoreCase("use") && args.length > 1) {
            if (args[1].equalsIgnoreCase("blacklist")) {
                Orebfuscator.configManager.setUseWorldsAsBlacklist(true);
                Orebfuscator.message(sender, "Use worlds as blacklist.");
            }
            else if (args[1].equalsIgnoreCase("whitelist")) {
                Orebfuscator.configManager.setUseWorldsAsBlacklist(false);
                Orebfuscator.message(sender, "Use worlds as whitelist.");
            }
        }

        else if (args[0].equalsIgnoreCase("reload")) {
            Orebfuscator.instance.reloadOrebfuscatorConfig();
            Orebfuscator.message(sender, "Reload complete.");
        }

        else if (args[0].equalsIgnoreCase("status")) {
        	String status = Orebfuscator.instance.getIsProtocolLibFound()
        			? (Orebfuscator.config.isEnabled() ? "Enabled" : "Disabled")
        			: "ProtocolLib is not found! Plugin cannot be enabled.";
        	
            Orebfuscator.message(sender, "Orebfuscator " + Orebfuscator.instance.getDescription().getVersion() + " is: " + status);
            Orebfuscator.message(sender, "Engine Mode: " + Orebfuscator.config.getEngineMode());

            Orebfuscator.message(sender, "Caching: " + (Orebfuscator.config.isUseCache() ? "Enabled" : "Disabled"));
            Orebfuscator.message(sender, "ProximityHider: " + (Orebfuscator.config.isProximityHiderEnabled() ? "Enabled" : "Disabled"));
            Orebfuscator.message(sender, "DarknessHideBlocks: " + (Orebfuscator.config.getDefaultWorld().isDarknessHideBlocks() ? "Enabled": "Disabled"));

            Orebfuscator.message(sender, "Initial Obfuscation Radius: " + Orebfuscator.config.getInitialRadius());
            Orebfuscator.message(sender, "Update Radius: " + Orebfuscator.config.getUpdateRadius());
            
            String worlds = "";

            for(World world : Orebfuscator.config.getWorlds().keySet()) {
            	if(worlds.length() > 0) {
            		worlds += ", ";
            	}
            	
            	worlds += world.getName();
            }
            
            Orebfuscator.message(sender, "Worlds: " + (worlds.equals("") ? "None" : worlds));
            Orebfuscator.message(sender, "Use worlds as: " + (Orebfuscator.config.getDefaultWorld().isEnabled() ? "Blacklist" : "Whitelist"));
        }

        else if (args[0].equalsIgnoreCase("clearcache")) {
            try {
				ObfuscatedDataCache.clearCache();
	            Orebfuscator.message(sender, "Cache cleared.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        return true;
    }
}