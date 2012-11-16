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

import net.minecraft.server.NetServerHandler;
import net.minecraftserverhook.NetServerHandlerProxy;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.hook.OrebfuscatorPlayerListenerHook;
import com.lishid.orebfuscator.hook.ProtocolLibHook;
import com.lishid.orebfuscator.hook.SpoutLoader;
import com.lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;
import com.lishid.orebfuscator.proximityhider.ProximityHider;
import com.lishid.orebfuscator.threading.ChunkCompressionThread;
import com.lishid.orebfuscator.threading.OrebfuscatorScheduler;
import com.lishid.orebfuscator.utils.Metrics;

/**
 * Anti X-RAY
 * 
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin
{
    private final OrebfuscatorBlockListener blockListener = new OrebfuscatorBlockListener();
    private final OrebfuscatorEntityListener entityListener = new OrebfuscatorEntityListener();
    private final OrebfuscatorPlayerListener playerListener = new OrebfuscatorPlayerListener();
    private final OrebfuscatorPlayerListenerHook playerListenerHook = new OrebfuscatorPlayerListenerHook();
    private static Metrics metrics;
    
    public static final Logger logger = Logger.getLogger("Minecraft.OFC");
    public static Orebfuscator instance;
    public static WeakHashMap<Player, Boolean> players = new WeakHashMap<Player, Boolean>();
    
    private ProtocolLibHook protocolHook;
    
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
        
        // ProtocolLib is compatible with Spout
        if (pm.getPlugin("ProtocolLib") != null)
        {
            Orebfuscator.log("ProtocolLib found! Using ProtocolLib.");
            protocolHook = new ProtocolLibHook();
            protocolHook.register(this);
        }
        // Using Spout
        else if (pm.getPlugin("Spout") != null)
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
                    Orebfuscator.log("Warning: Spout will disable several protection from Orebfuscator. Please use ProtocolLib.");
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
            for (Player p : players.keySet())
            {
                playerListenerHook.TryUpdateNetServerHandler(p);
            }
        }
        
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
        
        ObfuscatedDataCache.clearCache();
        OrebfuscatorScheduler.getScheduler().terminateAll();
        ChunkCompressionThread.terminate();
        ProximityHider.terminate();
        ProximityHider.proximityHiderTracker.clear();
        ProximityHider.playersToCheck.clear();
        BlockHitManager.clearAll();
        
        Orebfuscator.instance.getServer().getScheduler().cancelAllTasks();
        
        for (Player p : players.keySet())
        {
            if (p.isOnline())
            {
                CraftPlayer p2 = (CraftPlayer) p;
                if (p2.getHandle().netServerHandler instanceof NetServerHandlerProxy)
                {
                    NetServerHandler oldNetServerHandler = ((NetServerHandlerProxy) p2.getHandle().netServerHandler).nshInstance;
                    p2.getHandle().netServerHandler = oldNetServerHandler;
                    oldNetServerHandler.networkManager.a(oldNetServerHandler);
                }
            }
        }
        
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