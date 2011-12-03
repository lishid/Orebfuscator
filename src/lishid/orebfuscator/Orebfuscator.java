
package lishid.orebfuscator;

import lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import lishid.orebfuscator.utils.OrebfuscatorConfig;
import lishid.orebfuscator.utils.PermissionRelay;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event;

/**
 * Open other player's inventory
 *
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin {
	private final OrebfuscatorBlockListener blockListener = new OrebfuscatorBlockListener(this);
	private final OrebfuscatorEntityListener entityListener = new OrebfuscatorEntityListener(this);
	private final OrebfuscatorPlayerListener playerListener = new OrebfuscatorPlayerListener(this);
	public static boolean usingSpout = false;
	public static Orebfuscator mainPlugin;
	
    public void onEnable() {
    	//Load permissions system
        PluginManager pm = getServer().getPluginManager();
    	PermissionRelay.Setup(pm);
    	//Load configurations
    	mainPlugin = this;
    	OrebfuscatorConfig.Load();
        
        //Hook events
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Monitor, this);

		//pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Monitor, this);
		//pm.registerEvent(Event.Type.BLOCK_IGNITE, this.blockListener, Event.Priority.Monitor, this);
		//pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Monitor, this);
		
		
		//OrebfuscatorSpoutBridge
		if(pm.getPlugin("OrebfuscatorSpoutBridge") != null)
		{
			//SpoutManager.getPacketManager().addListenerUncompressedChunk(new PacketListener(this));
			System.out.println("[Orebfuscator] OrebfuscatorSpoutBridge is found, please remove it as it's no longer needed.");
			pm.disablePlugin(pm.getPlugin("OrebfuscatorSpoutBridge"));
			return;
		}
		
		//Output
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[Orebfuscator] version " + pdfFile.getVersion() + " initialization complete!" );
        
        getCommand("ofc").setExecutor(new OrebfuscatorCommandExecutor(this));
    }
    
    public void onDisable() {
    	//Save configurations
    	OrebfuscatorConfig.Save();
    	
    	//Output
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[Orebfuscator] version " + pdfFile.getVersion() + " disabled!" );
    }
}