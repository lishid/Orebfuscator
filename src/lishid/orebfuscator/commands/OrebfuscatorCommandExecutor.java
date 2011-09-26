package lishid.orebfuscator.commands;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.OrebfuscatorConfig;
import lishid.orebfuscator.utils.PermissionRelay;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OrebfuscatorCommandExecutor implements CommandExecutor {
    //private final Orebfuscator plugin;
    public OrebfuscatorCommandExecutor(Orebfuscator plugin) {
        //this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if ((sender instanceof Player) && !PermissionRelay.hasPermission((Player) sender, "Orebfuscator.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permissions.");
            return true;
        }
    	
    	if(args.length <= 0)
    	{
    		return false;
    	}
    	
    	if(args[0].equalsIgnoreCase("engine") && args.length > 1)
    	{
    		int engine = OrebfuscatorConfig.EngineMode();
			try 
			{
				engine = new Integer(args[1]);
			}
			catch (NumberFormatException e) 
			{
	    		sender.sendMessage(args[1] + " is not a number!");
	    		return true;
			}
			if(engine != 1 && engine != 2)
			{
	    		sender.sendMessage(args[1] + " is not a valid EngineMode!");
	    		return true;
			}
			else
			{
	    		OrebfuscatorConfig.SetEngineMode(engine);
	    		sender.sendMessage("[Orebfuscator] Engine set to: " + engine);
	    		return true;
			}
    	}
    	
    	if(args[0].equalsIgnoreCase("reload"))
    	{
    		OrebfuscatorConfig.Reload();
    		sender.sendMessage("[Orebfuscator] Reload complete.");
    	}
    	
    	if(args[0].equalsIgnoreCase("enable"))
    	{
    		OrebfuscatorConfig.SetEnabled(true);
    		sender.sendMessage("[Orebfuscator] Enabled.");
    	}
    	
    	if(args[0].equalsIgnoreCase("disable"))
    	{
    		OrebfuscatorConfig.SetEnabled(false);
    		sender.sendMessage("[Orebfuscator] Disabled.");
    	}
    	
    	if(args[0].equalsIgnoreCase("status"))
    	{
    		sender.sendMessage("[Orebfuscator] Orebfuscator status:");
    		sender.sendMessage("[Orebfuscator] Plugin is: " + (OrebfuscatorConfig.Enabled()?"Enabled":"Disabled"));
    		sender.sendMessage("[Orebfuscator] EngineMode: " + OrebfuscatorConfig.EngineMode());
    	}
    	
    	return true;
    }
}
