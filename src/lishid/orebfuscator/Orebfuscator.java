
package lishid.orebfuscator;

import java.util.logging.Logger;

import lishid.chunkscrambler.ChunkScramblerWorldListener;
import lishid.chunkscrambler.ScrambledWorldChunkManager;
import lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import lishid.orebfuscator.spout.SpoutLoader;
import lishid.orebfuscator.utils.Calculations;
import lishid.orebfuscator.utils.Metrics;
import lishid.orebfuscator.utils.OrebfuscatorConfig;
import lishid.orebfuscator.utils.PermissionRelay;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Event;

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
     * Logger for debugging.
     */
    public static final Logger logger = Logger.getLogger("Minecraft.Orebfuscator");
    
	/**
     * True if this plugin is using Spout.
     */
	public static boolean usingSpout = false;

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
	
	@Override
    public void onEnable() {
    	//Load permissions system
        PluginManager pm = getServer().getPluginManager();
    	PermissionRelay.Setup(pm);
    	//Load configurations
    	instance = this;
    	OrebfuscatorConfig.load();
        
        //Orebfuscator events
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Monitor, this);
		
		//Start ChunkScrambler
		if(OrebfuscatorConfig.getUseChunkScrambler())
		{
			//ChunkScrambler events
			pm.registerEvent(Event.Type.WORLD_INIT, new ChunkScramblerWorldListener(), Event.Priority.Highest, this);
			for(World world : this.getServer().getWorlds())
				ReplaceWorldChunkManager(world);
			
			//Disable ChunkScrambler plugin if exists
			if(pm.getPlugin("ChunkScrambler") != null)
			{
				Orebfuscator.log("ChunkScrambler is integrated into Orebfuscator now. You should remove ChunkScrambler.jar from the plugins folder.");
				pm.disablePlugin(pm.getPlugin("ChunkScrambler"));
			}
			Orebfuscator.log("Integrated ChunkScrambled enabled!");
		}


		//Check if OrebfuscatorSpoutBridge exists
		if(pm.getPlugin("OrebfuscatorSpoutBridge") != null)
		{
			Orebfuscator.log("OrebfuscatorSpoutBridge is integrated into Orebfuscator now. You should remove OrebfuscatorSpoutBridge.jar from the plugins folder.");
		}
		
		//Spout events
		else if(pm.getPlugin("Spout") != null)
		{
			//Using Spout.
			try{
				SpoutLoader.InitializeSpout();
				usingSpout = true;
				Orebfuscator.log("Spout found, using Spout.");
			}catch(Exception e){
				Orebfuscator.log("Spout initialization failed. Error: " + e.getMessage());
			}
		}
		else
		{
			//Non-spout method, use Player Join to replace NetServerHandler
			pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Monitor, this);
			Orebfuscator.log("Spout not found, using non-Spout mode.");
		}
		
		//Verbose mode
		Verbose();
		//Metrics
		try
		{
			metrics = new Metrics();
			metrics.beginMeasuringPlugin(this);
		}
		catch(Exception e){ Orebfuscator.log(e); }
		
		//Output
        PluginDescriptionFile pdfFile = this.getDescription();
        Orebfuscator.log("Version " + pdfFile.getVersion() + " enabled!" );
    }
    
    @Override
    public void onDisable() {
    	//Save configurations
    	OrebfuscatorConfig.save();
    	
    	//Output
        PluginDescriptionFile pdfFile = this.getDescription();
        log("Version " + pdfFile.getVersion() + " disabled!" );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return OrebfuscatorCommandExecutor.onCommand(sender, command, label, args);
    }
    

	/**
     * Replaces world's chunk manager to be able to scramble ore location
     */
    public static void ReplaceWorldChunkManager(World world)
    {
		if(world.getEnvironment() == Environment.NORMAL)
			((CraftWorld)world).getHandle().worldProvider.c = new ScrambledWorldChunkManager(((CraftWorld)world).getHandle());
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
		logger.info("[Orebfuscator] " + text);
	}

	/**
     * Log an error
     */
	public static void log(Exception e)
	{
		logger.severe("[Orebfuscator] " + e.getMessage());
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
		target.sendMessage(ChatColor.AQUA + "[Orebfuscator] " + message);
	}
}