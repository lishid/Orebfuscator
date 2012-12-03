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

package com.lishid.orebfuscator;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.hook.OrebfuscatorPlayerHook;
import com.lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;
import com.lishid.orebfuscator.proximityhider.ProximityHider;
import com.lishid.orebfuscator.utils.Metrics;

/**
 * Orebfuscator Anti X-RAY
 * 
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin
{
    private static Metrics metrics;
    
    public static final Logger logger = Logger.getLogger("Minecraft.OFC");
    public static Orebfuscator instance;
    
    @Override
    public void onEnable()
    {
        instance = this;
        // Load permissions system
        PluginManager pm = getServer().getPluginManager();
        // Load configurations
        OrebfuscatorConfig.load();
        
        // Orebfuscator events
        pm.registerEvents(new OrebfuscatorPlayerListener(), this);
        pm.registerEvents(new OrebfuscatorEntityListener(), this);
        pm.registerEvents(new OrebfuscatorBlockListener(), this);
        
        pm.registerEvents(new OrebfuscatorPlayerHook(), this);
        /*
        if (pm.getPlugin("ProtocolLib") != null)
        {
            Orebfuscator.log("ProtocolLib found! Hooking into ProtocolLib.");
            (new ProtocolLibHook()).register(this);
        }
        */
        // Metrics
        try
        {
            metrics = new Metrics(this);
            metrics.start();
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
    }
    
    @Override
    public void onDisable()
    {
        ObfuscatedDataCache.clearCache();
        ProximityHider.terminate();
        ProximityHider.proximityHiderTracker.clear();
        ProximityHider.playersToCheck.clear();
        BlockHitManager.clearAll();
        Orebfuscator.instance.getServer().getScheduler().cancelAllTasks();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return OrebfuscatorCommandExecutor.onCommand(sender, command, label, args);
    }
    
    /**
     * Log an information
     */
    public static void log(String text)
    {
        logger.info(ChatColor.AQUA + "[OFC] " + text);
    }
    
    /**
     * Log an error
     */
    public static void log(Throwable e)
    {
        logger.severe(ChatColor.AQUA + "[OFC] " + e.toString());
        e.printStackTrace();
    }
    
    /**
     * Send a message to a player
     */
    public static void message(CommandSender target, String message)
    {
        target.sendMessage(ChatColor.AQUA + "[OFC] " + message);
    }
}