package lishid.orebfuscator;

import lishid.orebfuscator.utils.OrebfuscatorNetServerHandler;
import lishid.orebfuscator.utils.OrebfuscatorConfig;
import lishid.orebfuscator.utils.OrebfuscatorThreadUpdate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

public class OrebfuscatorPlayerListener extends PlayerListener
{
	@Override
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		TryUpdateNetServerHandler(event.getPlayer());
	}

	@Override
	public void onPlayerQuit(final PlayerQuitEvent event)
	{
		if(OrebfuscatorBlockListener.blockLog.containsKey(event.getPlayer()))
		{
			OrebfuscatorBlockListener.blockLog.remove(event.getPlayer());
		}
	}
	
	@Override
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
	
	public void TryUpdateNetServerHandler(Player player)
	{
		try
		{
			CraftPlayer cPlayer = (CraftPlayer)player;
			CraftServer server = (CraftServer)player.getServer();

			if (!(cPlayer.getHandle().netServerHandler.getClass().equals(OrebfuscatorNetServerHandler.class))) {
				OrebfuscatorNetServerHandler handler = new OrebfuscatorNetServerHandler(server.getHandle().server, cPlayer.getHandle().netServerHandler);
				Location loc = player.getLocation();
				handler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
				cPlayer.getHandle().netServerHandler = handler;
				cPlayer.getHandle().netServerHandler.networkManager.a(handler);
				server.getServer().networkListenThread.a(handler);
			}
		}
		catch (Exception e) { Orebfuscator.log(e); }
	}
}
