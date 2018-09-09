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

import java.io.*;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.orebfuscator.cache.CacheCleaner;
import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.config.ConfigManager;
import com.lishid.orebfuscator.config.OrebfuscatorConfig;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.hook.ProtocolLibHook;
import com.lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;
import com.lishid.orebfuscator.nms.INmsManager;
import com.lishid.orebfuscator.utils.Globals;

/**
 * Orebfuscator Anti X-RAY
 *
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin {

    public static final Logger logger = Logger.getLogger("Minecraft.OFC");
    public static Orebfuscator instance;
    public static OrebfuscatorConfig config;
    public static ConfigManager configManager;
    
    public static INmsManager nms;
    
    private boolean isProtocolLibFound;
    public boolean getIsProtocolLibFound() {
    	return this.isProtocolLibFound;
    }

	@Override
    public void onEnable() {
        // Get plugin manager
        PluginManager pm = getServer().getPluginManager();

        instance = this;
        nms = createNmsManager();
        
        // Load configurations
        loadOrebfuscatorConfig();

        makeConfigExample();
        
        this.isProtocolLibFound = pm.getPlugin("ProtocolLib") != null;

        if (!this.isProtocolLibFound) {
            Orebfuscator.log("ProtocolLib is not found! Plugin cannot be enabled.");
            return;
        }
        
        // Orebfuscator events
        pm.registerEvents(new OrebfuscatorPlayerListener(), this);
        pm.registerEvents(new OrebfuscatorEntityListener(), this);
        pm.registerEvents(new OrebfuscatorBlockListener(), this);

        (new ProtocolLibHook()).register(this);
        
        // Run CacheCleaner
        getServer().getScheduler().runTaskTimerAsynchronously(this, new CacheCleaner(), 0, config.getCacheCleanRate());
    }
    
    public void loadOrebfuscatorConfig() {
    	if(config == null) {
    		config = new OrebfuscatorConfig();
    		configManager = new ConfigManager(this, logger, config);
    	}
    	
    	configManager.load();

        ObfuscatedDataCache.resetCacheFolder();

        nms.setMaxLoadedCacheFiles(config.getMaxLoadedCacheFiles());
        if (config.shouldPrecalcBlockStates()) {
            nms.preCalculateBlockStates();
        }
        
        //Make sure cache is cleared if config was changed since last start
        try {
			ObfuscatedDataCache.checkCacheAndConfigSynchronized();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private void makeConfigExample() {
        File outputFile = new File(getDataFolder(), "config.example_enabledworlds.yml");

        if(outputFile.exists()) return;

        InputStream configStream = Orebfuscator.class.getResourceAsStream("/resources/config.example_enabledworlds.yml");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(configStream));
                PrintWriter writer = new PrintWriter(outputFile)
            )
        {
            String line;

            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void reloadOrebfuscatorConfig() {
    	reloadConfig();
    	loadOrebfuscatorConfig();
    }

    private static INmsManager createNmsManager() {

        String serverVersion = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        if(serverVersion.equals("v1_13_R2")) {
            return new com.lishid.orebfuscator.nms.v1_13_R2.NmsManager();
        }
        if(serverVersion.equals("v1_13_R1")) {
            return new com.lishid.orebfuscator.nms.v1_13_R1.NmsManager();
        }
        else return null;
    }

    @Override
    public void onDisable() {
        ObfuscatedDataCache.closeCacheFiles();
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
        target.sendMessage(ChatColor.AQUA + Globals.LogPrefix + message);
    }
}
