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

import java.util.WeakHashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.cache.ObfuscatedHashCache;
import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.hook.OrebfuscatorPlayerListenerHook;
import com.lishid.orebfuscator.hook.SpoutLoader;
import com.lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;
import com.lishid.orebfuscator.proximityhider.ProximityHider;
import com.lishid.orebfuscator.threading.OrebfuscatorThreadCalculation;
import com.lishid.orebfuscator.threading.OrebfuscatorThreadUpdate;
import com.lishid.orebfuscator.utils.Metrics;

/**
 * Anti X-RAY
 * 
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin
{
    
    /**
     * Block listener
     */
    private final OrebfuscatorBlockListener blockListener = new OrebfuscatorBlockListener();
    
    /**
     * Entity listener
     */
    private final OrebfuscatorEntityListener entityListener = new OrebfuscatorEntityListener();
    
    /**
     * Player listener
     */
    private final OrebfuscatorPlayerListener playerListener = new OrebfuscatorPlayerListener();
    
    /**
     * Player listener to hook to CB's NSH
     */
    private final OrebfuscatorPlayerListenerHook playerListenerHook = new OrebfuscatorPlayerListenerHook();
    
    /**
     * Logger for debugging.
     */
    public static final Logger logger = Logger.getLogger("Minecraft.Orebfuscator");
    
    /**
     * Object containing the instance of Orebfuscator.
     */
    public static Orebfuscator instance;
    
    /**
     * PluginMetrics add-on
     */
    private static Metrics metrics;
    
    /**
     * Players list
     */
    public static WeakHashMap<Player, Boolean> players = new WeakHashMap<Player, Boolean>();
    
    @Override
    public void onEnable()
    {
        instance = this;
        // Load permissions system
        PluginManager pm = getServer().getPluginManager();
        // Load configurations
        OrebfuscatorConfig.load();
        synchronized (Orebfuscator.players)
        {
            for (Player p : this.getServer().getOnlinePlayers())
            {
                players.put(p, true);
            }
        }
        
        // Orebfuscator events
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.entityListener, this);
        pm.registerEvents(this.blockListener, this);
        
        // Using Spout
        if (pm.getPlugin("Spout") != null)
        {
            // Try to load spout 10 times...
            Throwable t = null;
            boolean spoutLoaded = false;
            for (int i = 0; i < 10; i++)
            {
                try
                {
                    SpoutLoader.InitializeSpout();
                    Orebfuscator.log("Spout found, using Spout.");
                    spoutLoaded = true;
                    break;
                }
                catch (Throwable e)
                {
                    t = e;
                }
            }
            if (!spoutLoaded && t != null)
            {
                Orebfuscator.log("Spout loading error.");
                t.printStackTrace();
            }
        }
        else
        {
            // Non-spout method, use Player Join to replace NetServerHandler
            pm.registerEvents(this.playerListenerHook, this);
            Orebfuscator.log("Spout not found, using non-Spout mode.");
        }
        
        // Metrics
        try
        {
            Orebfuscator.log("Statistics features enabling...");
            metrics = new Metrics(this);
            metrics.start();
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
        
        // Load ProximityHider
        ProximityHider.Load();
        
        // Output
        PluginDescriptionFile pdfFile = this.getDescription();
        Orebfuscator.log("Version " + pdfFile.getVersion() + " enabled!");
    }
    
    @Override
    public void onDisable()
    {
        synchronized (Orebfuscator.players)
        {
            players.clear();
        }
        
        ObfuscatedHashCache.clearCache();
        ObfuscatedDataCache.clearCache();
        OrebfuscatorThreadCalculation.terminateAll();
        OrebfuscatorThreadUpdate.terminate();
        ProximityHider.terminate();
        ProximityHider.proximityHiderTracker.clear();
        ProximityHider.playersToCheck.clear();
        BlockHitManager.clearAll();
        
        Orebfuscator.instance.getServer().getScheduler().cancelAllTasks();
        
        // Output
        PluginDescriptionFile pdfFile = this.getDescription();
        log("Version " + pdfFile.getVersion() + " disabled!");
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
        logger.info("[OFC] " + text);
    }
    
    /**
     * Log an error
     */
    public static void log(Throwable e)
    {
        logger.severe("[OFC] " + e.toString());
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