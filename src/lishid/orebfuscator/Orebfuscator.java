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

package lishid.orebfuscator;

import java.util.WeakHashMap;
import java.util.logging.Logger;

import lishid.orebfuscator.cache.ObfuscatedHashCache;
import lishid.orebfuscator.cache.ObfuscatedDataCache;
import lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import lishid.orebfuscator.hook.OrebfuscatorPlayerListenerHook;
import lishid.orebfuscator.hook.SpoutLoader;
import lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;
import lishid.orebfuscator.obfuscation.Calculations;
import lishid.orebfuscator.proximityhider.ProximityHider;
import lishid.orebfuscator.threading.OrebfuscatorThreadCalculation;
import lishid.orebfuscator.threading.OrebfuscatorThreadUpdate;
import lishid.orebfuscator.utils.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Anti X-RAY
 *
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin {
	
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
     * Keep track of scheduled tasks
     */
	private static int lastTask = -1;

	/**
     * Players list
     */
	public static WeakHashMap<Player, Boolean> players = new WeakHashMap<Player, Boolean>();
	
	@Override
    public void onEnable() {
    	//Load permissions system
        PluginManager pm = getServer().getPluginManager();
    	//Load configurations
    	instance = this;
    	OrebfuscatorConfig.load();
		synchronized(Orebfuscator.players)
		{
			for(Player p : this.getServer().getOnlinePlayers())
			{
				players.put(p, true);
			}
		}
        
        //Orebfuscator events
		pm.registerEvents(this.playerListener, this);
		pm.registerEvents(this.entityListener, this);
		pm.registerEvents(this.blockListener, this);

		//Using Spout
		if(pm.getPlugin("Spout") != null)
		{
			//Try to load spout 10 times...
			Throwable t = null;
			boolean spoutLoaded = false;
			for(int i = 0; i < 10; i++)
			{
				try{
					SpoutLoader.InitializeSpout();
					Orebfuscator.log("Spout found, using Spout.");
					spoutLoaded = true;
					break;
				}catch(Throwable e){
					t = e;
				}
			}
			if(!spoutLoaded && t != null)
			{
				Orebfuscator.log("Spout loading error.");
				t.printStackTrace();
			}
		}
		else
		{
			//Non-spout method, use Player Join to replace NetServerHandler
			pm.registerEvents(this.playerListenerHook, this);
			Orebfuscator.log("Spout not found, using non-Spout mode.");
		}
		
		//Verbose mode
		Verbose();
		//Metrics
		try
		{
			Orebfuscator.log("Statistics features enabling...");
			metrics = new Metrics(this);
			metrics.start();
		}
		catch(Exception e){ Orebfuscator.log(e); }
		
		//Load ProximityHider
		ProximityHider.Load();
		
		//Output
        PluginDescriptionFile pdfFile = this.getDescription();
        Orebfuscator.log("Version " + pdfFile.getVersion() + " enabled!" );
    }
    
    @Override
    public void onDisable() {
		synchronized(Orebfuscator.players)
		{
	    	players.clear();
		}
		
		ObfuscatedHashCache.clearCache();
		ObfuscatedDataCache.clearCache();
		OrebfuscatorThreadCalculation.terminateAll();
		OrebfuscatorThreadUpdate.terminate();
		ProximityHider.proximityHiderTracker.clear();
		ProximityHider.playersToCheck.clear();
		OrebfuscatorBlockListener.blockLog.clear();
		
		Orebfuscator.instance.getServer().getScheduler().cancelAllTasks();
		
    	//Output
        PluginDescriptionFile pdfFile = this.getDescription();
        log("Version " + pdfFile.getVersion() + " disabled!" );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return OrebfuscatorCommandExecutor.onCommand(sender, command, label, args);
    }

	/**
     * Refresh verbose mode
     */
    public static void Verbose()
    {
		if(OrebfuscatorConfig.getVerboseMode())
		{
			try{
				lastTask = Orebfuscator.instance.getServer().getScheduler().scheduleAsyncRepeatingTask(Orebfuscator.instance, new Runnable() {
				    public void run() {
				    	Orebfuscator.log("Chunks calculated in the past 40 ticks: " + Calculations.ChunksCalculated);
						Calculations.ChunksCalculated = 0;
				    }
				}, 40L, 40L);
			}catch(Exception e){Orebfuscator.log(e);}
		}
		else if(lastTask != -1)
		{
			try{
				Orebfuscator.instance.getServer().getScheduler().cancelTask(lastTask);
			}catch(Exception e){Orebfuscator.log(e);}
		}
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
		logger.severe("[OFC] " + e.getMessage());
		e.printStackTrace();
	}

	/**
     * Verbose output
     */
	public static void verbose(String text)
	{
		log("(debug) " + text);
	}

	/**
     * Send a message to a player
     */
	public static void message(CommandSender target, String message)
	{
		target.sendMessage(ChatColor.AQUA + "[OFC] " + message);
	}
}