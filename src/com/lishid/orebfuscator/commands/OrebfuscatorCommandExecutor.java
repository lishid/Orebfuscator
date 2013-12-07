/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.cache.ObfuscatedDataCache;

public class OrebfuscatorCommandExecutor {
    public static boolean DebugMode = false;

    public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ((sender instanceof Player) && !sender.hasPermission("Orebfuscator.admin")) {
            Orebfuscator.message(sender, "You do not have permissions.");
            return true;
        }

        if (args.length <= 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("engine") && args.length > 1) {
            int engine = OrebfuscatorConfig.EngineMode;
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
                OrebfuscatorConfig.setEngineMode(engine);
                Orebfuscator.message(sender, "Engine set to: " + engine);
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("updateradius") && args.length > 1) {
            int radius = OrebfuscatorConfig.UpdateRadius;
            try {
                radius = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            OrebfuscatorConfig.setUpdateRadius(radius);
            Orebfuscator.message(sender, "UpdateRadius set to: " + OrebfuscatorConfig.UpdateRadius);
            return true;
        }

        else if (args[0].equalsIgnoreCase("initialradius") && args.length > 1) {
            int radius = OrebfuscatorConfig.InitialRadius;
            try {
                radius = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            OrebfuscatorConfig.setInitialRadius(radius);
            Orebfuscator.message(sender, "InitialRadius set to: " + radius);
            return true;
        }

        else if (args[0].equalsIgnoreCase("airgen") && args.length > 1) {
            int airgen = OrebfuscatorConfig.AirGeneratorMaxChance;
            try {
                airgen = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            OrebfuscatorConfig.setAirGeneratorMaxChance(airgen);
            Orebfuscator.message(sender, "AirGeneratorMaxChance set to: " + airgen);
            return true;
        }

        else if ((args[0].equalsIgnoreCase("proximity") | args[0].equalsIgnoreCase("proximityhider")) && args.length > 1) {
            int ProximityHiderDistance = OrebfuscatorConfig.ProximityHiderDistance;
            try {
                ProximityHiderDistance = new Integer(args[1]);
            }
            catch (NumberFormatException e) {
                Orebfuscator.message(sender, args[1] + " is not a number!");
                return true;
            }
            OrebfuscatorConfig.setProximityHiderDistance(ProximityHiderDistance);
            Orebfuscator.message(sender, "ProximityHider Distance set to: " + ProximityHiderDistance);
            return true;
        }

        else if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
            boolean data = args[0].equalsIgnoreCase("enable");

            if (args[0].equalsIgnoreCase("enable") && args.length == 1) {
                OrebfuscatorConfig.setEnabled(true);
                Orebfuscator.message(sender, "Enabled.");
            }

            else if (args[0].equalsIgnoreCase("disable") && args.length == 1) {
                OrebfuscatorConfig.setEnabled(false);
                Orebfuscator.message(sender, "Disabled.");
            }

            else if (args.length > 1) {
                if (args[1].equalsIgnoreCase("darknesshide")) {
                    OrebfuscatorConfig.setDarknessHideBlocks(data);
                    Orebfuscator.message(sender, "Darkness obfuscation " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("op")) {
                    OrebfuscatorConfig.setNoObfuscationForOps(data);
                    Orebfuscator.message(sender, "Ops No-Obfuscation " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("perms") || args[1].equalsIgnoreCase("permissions")) {
                    OrebfuscatorConfig.setNoObfuscationForPermission(data);
                    Orebfuscator.message(sender, "Permissions No-Obfuscation " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("cache")) {
                    OrebfuscatorConfig.setUseCache(data);
                    Orebfuscator.message(sender, "Cache " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("axr")) {
                    OrebfuscatorConfig.setAntiTexturePackAndFreecam(data);
                    Orebfuscator.message(sender, "AntiTexturePackAndFreecam " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("notification")) {
                    OrebfuscatorConfig.setLoginNotification(data);
                    Orebfuscator.message(sender, "Login Notification " + (data ? "enabled" : "disabled") + ".");
                }
                else if (args[1].equalsIgnoreCase("world") && args.length > 2) {
                    OrebfuscatorConfig.setDisabledWorlds(args[2], !data);
                    Orebfuscator.message(sender, "World \"" + args[2] + "\" obfuscation " + (data ? "enabled" : "disabled") + ".");
                }
            }
        }

        else if (args[0].equalsIgnoreCase("reload")) {
            OrebfuscatorConfig.reload();
            Orebfuscator.message(sender, "Reload complete.");
        }

        else if (args[0].equalsIgnoreCase("status")) {
            Orebfuscator.message(sender, "Orebfuscator " + Orebfuscator.instance.getDescription().getVersion() + " is: " + (OrebfuscatorConfig.Enabled ? "Enabled" : "Disabled"));
            Orebfuscator.message(sender, "EngineMode: " + OrebfuscatorConfig.EngineMode);

            Orebfuscator.message(sender, "Caching: " + (OrebfuscatorConfig.UseCache ? "Enabled" : "Disabled"));
            Orebfuscator.message(sender, "ProximityHider: " + (OrebfuscatorConfig.UseProximityHider ? "Enabled" : "Disabled"));

            Orebfuscator.message(sender, "Initial Obfuscation Radius: " + OrebfuscatorConfig.InitialRadius);
            Orebfuscator.message(sender, "Update Radius: " + OrebfuscatorConfig.UpdateRadius);

            String disabledWorlds = OrebfuscatorConfig.getDisabledWorlds();
            Orebfuscator.message(sender, "Disabled worlds: " + (disabledWorlds.equals("") ? "None" : disabledWorlds));
        }

        else if (args[0].equalsIgnoreCase("clearcache")) {
            ObfuscatedDataCache.ClearCache();
            Orebfuscator.message(sender, "Cache cleared.");
        }

        else if (args[0].equalsIgnoreCase("debug")) {
            DebugMode = !DebugMode;
        }

        return true;
    }
}