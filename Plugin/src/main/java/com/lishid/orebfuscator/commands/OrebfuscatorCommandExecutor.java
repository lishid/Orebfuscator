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

import com.lishid.orebfuscator.config.WorldConfig;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.DeprecatedMethods;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.cache.ObfuscatedDataCache;

public class OrebfuscatorCommandExecutor {
	
	/**
	 * Added for 1.13 to allow Integer encoding of Material for the transient lookup arrays.
	 */
	private static final Material[] translation = Material.values();

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

        else if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
            boolean data = args[0].equalsIgnoreCase("enable");

            if (args[0].equalsIgnoreCase("enable") && args.length == 1) {
                Orebfuscator.configManager.setEnabled(true);
                Orebfuscator.message(sender, "Enabled.");
                return true;
            }

            else if (args[0].equalsIgnoreCase("disable") && args.length == 1) {
                Orebfuscator.configManager.setEnabled(false);
                Orebfuscator.message(sender, "Disabled.");
                return true;
            }

            else if (args.length > 1) {
                if (args[1].equalsIgnoreCase("op")) {
                	Orebfuscator.configManager.setNoObfuscationForOps(data);
                    Orebfuscator.message(sender, "Ops No-Obfuscation " + (data ? "enabled" : "disabled") + ".");
                    return true;
                }
                else if (args[1].equalsIgnoreCase("perms") || args[1].equalsIgnoreCase("permissions")) {
                	Orebfuscator.configManager.setNoObfuscationForPermission(data);
                    Orebfuscator.message(sender, "Permissions No-Obfuscation " + (data ? "enabled" : "disabled") + ".");
                    return true;
                }
                else if (args[1].equalsIgnoreCase("cache")) {
                	Orebfuscator.configManager.setUseCache(data);
                    Orebfuscator.message(sender, "Cache " + (data ? "enabled" : "disabled") + ".");
                    return true;
                }
                else if (args[1].equalsIgnoreCase("notification")) {
                	Orebfuscator.configManager.setLoginNotification(data);
                    Orebfuscator.message(sender, "Login Notification " + (data ? "enabled" : "disabled") + ".");
                    return true;
                }
            }
        }
        
        else if (args[0].equalsIgnoreCase("reload")) {
            Orebfuscator.instance.reloadOrebfuscatorConfig();
            Orebfuscator.message(sender, "Reload complete.");
            return true;
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

            Orebfuscator.message(sender, "World by Default: " + (Orebfuscator.config.getDefaultWorld().isEnabled() ? "Enabled" : "Disabled"));

            String worldNames = Orebfuscator.config.getWorldNames();

            Orebfuscator.message(sender, "Worlds in List: " + (worldNames.equals("") ? "None" : worldNames));

            return true;
        }

        else if (args[0].equalsIgnoreCase("clearcache")) {
            try {
				ObfuscatedDataCache.clearCache();
	            Orebfuscator.message(sender, "Cache cleared.");
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            return true;
        }
        
        else if (args[0].equalsIgnoreCase("obfuscateblocks")) {
        	if(args.length == 1) {
        		Orebfuscator.message(sender, ChatColor.RED + "World is required parameter.");
        	} else {
	        	String worldName = args[1];
	        	World world = Bukkit.getWorld(worldName);
	        	
	        	if(world == null) {
	        		Orebfuscator.message(sender, ChatColor.RED + "Specified world is not found.");
	        	} else {
	        		if(args.length > 2) {
	        			Material material = Material.getMaterial(args[2]);
	        			
	        			if(material == null) {
	        				Orebfuscator.message(sender, ChatColor.RED + "Specified material is not found.");
	        			} else {	        			
		        			if(Orebfuscator.configManager.getWorld(world).isObfuscated(material))
		        				Orebfuscator.message(sender, material.name() + ": " + ChatColor.GREEN + "obfuscate");
		        			else
		        				Orebfuscator.message(sender, material.name() + ": " + ChatColor.RED + "not obfuscate");
	        			}
	        		} else {
		        		boolean[] blocks = Orebfuscator.configManager.getWorld(world).getObfuscateAndProximityBlocks();
		        		
		        		Orebfuscator.message(sender, ChatColor.GREEN + "Obfuscate blocks:");
		        		
		        		for(int i = 0; i < blocks.length; i++) {
		        			if(blocks[i]) {
		        				Orebfuscator.message(sender, " - " + translation[i].name());
		        			}
		        		}
	        		}
	        	}
        	}
        	
        	return true;
        }
        
        return false;
    }
}