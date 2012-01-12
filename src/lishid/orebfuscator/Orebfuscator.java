
package lishid.orebfuscator;

import lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import lishid.orebfuscator.utils.OrebfuscatorConfig;
import lishid.orebfuscator.utils.PermissionRelay;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

/**
 * Anti X-RAY
 *
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin {
	private final OrebfuscatorBlockListener blockListener = new OrebfuscatorBlockListener(this);
	private final OrebfuscatorEntityListener entityListener = new OrebfuscatorEntityListener(this);
	private final OrebfuscatorPlayerListener playerListener = new OrebfuscatorPlayerListener(this);
	public static boolean usingSpout = false;
	public static Orebfuscator mainPlugin;
	
	@Override
    public void onEnable() {
    	//Load permissions system
        PluginManager pm = getServer().getPluginManager();
    	PermissionRelay.Setup(pm);
    	//Load configurations
    	mainPlugin = this;
        
        //Hook events
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Monitor, this);

		//Spout events
		if(pm.getPlugin("Spout") != null && pm.getPlugin("OrebfuscatorSpoutBridge") == null)
		{
			System.out.println("[Orebfuscator] Error loading, Spout is found but OrebfuscatorSpoutBridge is not found.");
			pm.disablePlugin(this);
			return;
		}
		else if(pm.getPlugin("Spout") != null)
		{
			System.out.println("[Orebfuscator] OrebfuscatorSpoutBridge found, using Spout mode.");
			usingSpout = true;
        }
		else
		{
			//Non-spout method, use Player Join to replace NetServerHandler
			pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Monitor, this);
			System.out.println("[Orebfuscator] Spout not found, using Non-Spout mode.");
		}
    	OrebfuscatorConfig.Load();
    	//log();
		
		//Output
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[Orebfuscator] version " + pdfFile.getVersion() + " initialization complete!" );
    }
	/*
	public static void log()
	{
		try{
			System.out.println("Requested: " + Calculations.ChunksRequested + " Calculated: " + Calculations.ChunksCalculated + " Incomplete: " + Calculations.ChunksIncomplete);
			Calculations.ChunksRequested = 0;
			Calculations.ChunksCalculated = 0;
			Calculations.ChunksIncomplete = 0;
			Orebfuscator.mainPlugin.getServer().getScheduler().scheduleSyncDelayedTask(Orebfuscator.mainPlugin, new Runnable() {
			    public void run() {
			    	log();
			    }
			}, 40L);
		}catch(Exception e){}
	}*/

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return OrebfuscatorCommandExecutor.onCommand(sender, command, label, args);
    }
    
    @Override
    public void onDisable() {
    	//Save configurations
    	OrebfuscatorConfig.Save();
    	
    	//Output
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[Orebfuscator] version " + pdfFile.getVersion() + " disabled!" );
    }
}