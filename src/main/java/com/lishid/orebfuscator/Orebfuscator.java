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

package com.lishid.orebfuscator;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.hook.ProtocolLibHook;
import com.lishid.orebfuscator.internal.MinecraftInternals;
import com.lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;

/**
 * Orebfuscator Anti X-RAY
 *
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin {

    public static final Logger logger = Logger.getLogger("Minecraft.OFC");
    public static Orebfuscator instance;
    public static boolean usePL = false;
    public static boolean useSpigot = false;

    @Override
    public void onEnable() {
        // Get plugin manager
        PluginManager pm = getServer().getPluginManager();

        instance = this;
        // Load configurations
        OrebfuscatorConfig.load();

        // Orebfuscator events
        pm.registerEvents(new OrebfuscatorPlayerListener(), this);
        pm.registerEvents(new OrebfuscatorEntityListener(), this);
        pm.registerEvents(new OrebfuscatorBlockListener(), this);

        if (pm.getPlugin("ProtocolLib") != null) {
            Orebfuscator.log("ProtocolLib found! Hooking into ProtocolLib.");
            (new ProtocolLibHook()).register(this);
            usePL = true;
        }

        /* NoLagg is deprecated now
        if (pm.getPlugin("NoLagg") != null && !usePL) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    Orebfuscator.log("WARNING! NoLagg Absolutely NEED ProtocolLib to work with Orebfuscator!");
                }
            }, 0, 60 * 1000);// Warn every minute
        }
        */

        // Disable spigot's built-in orebfuscator since it has limited functionality
        try {
            Class.forName("org.spigotmc.AntiXray");
            Orebfuscator.log("Spigot found! Automatically disabling built-in AntiXray.");
            for (World world : getServer().getWorlds()) {
                MinecraftInternals.tryDisableSpigotAntiXray(world);
            }
        } catch (Exception e) {
            // Spigot not found
        }
    }

    @Override
    public void onDisable() {
        ObfuscatedDataCache.clearCache();
        BlockHitManager.clearAll();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return OrebfuscatorCommandExecutor.onCommand(sender, command, label, args);
    }

    public void runTask(Runnable task) {
        if (this.isEnabled()) {
            getServer().getScheduler().runTask(this, task);
        }
    }

    /**
     * Log an information
     */
    public static void log(String text) {
        logger.info("[OFC] " + text);
    }

    /**
     * Log an error
     */
    public static void log(Throwable e) {
        logger.severe("[OFC] " + e.toString());
        e.printStackTrace();
    }

    /**
     * Send a message to a player
     */
    public static void message(CommandSender target, String message) {
        target.sendMessage(ChatColor.AQUA + "[OFC] " + message);
    }
}
