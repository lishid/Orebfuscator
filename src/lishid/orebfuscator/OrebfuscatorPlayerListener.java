package lishid.orebfuscator;

import lishid.orebfuscator.threading.OrebfuscatorThreadUpdate;
import lishid.orebfuscator.utils.OrebfuscatorConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

public class OrebfuscatorPlayerListener implements Listener
{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if(OrebfuscatorConfig.playerBypassOp(player))
		{
			Orebfuscator.message(player, "Orebfuscator bypassed because you are OP.");
		}
		else if(OrebfuscatorConfig.playerBypassPerms(player))
		{
			Orebfuscator.message(player, "Orebfuscator bypassed because you have permissions.");
		}
		synchronized(Orebfuscator.players)
		{
			Orebfuscator.players.put(player, true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event)
	{
		synchronized(Orebfuscator.players)
		{
			Orebfuscator.players.remove(event.getPlayer());
		}
		if(OrebfuscatorBlockListener.blockLog.containsKey(event.getPlayer()))
		{
			OrebfuscatorBlockListener.blockLog.remove(event.getPlayer());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if( event.getAction() != Action.RIGHT_CLICK_BLOCK ||
				event.isCancelled() || event.useInteractedBlock() == Result.DENY || 
				!OrebfuscatorConfig.getEnabled() || !OrebfuscatorConfig.getUpdateOnHoe())
			return;
		
		if(event.getItem() != null && event.getItem().getType() != null && 
				((event.getItem().getType() == Material.WOOD_HOE) || (event.getItem().getType() == Material.IRON_HOE) 
				|| (event.getItem().getType() == Material.GOLD_HOE) || (event.getItem().getType() == Material.DIAMOND_HOE)) && 
				(event.getMaterial() == Material.DIRT || event.getMaterial() == Material.GRASS))
		{
			OrebfuscatorThreadUpdate.Queue(event.getClickedBlock());
		}
	}
}
