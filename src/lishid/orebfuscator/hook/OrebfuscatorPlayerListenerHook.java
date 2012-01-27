package lishid.orebfuscator.hook;

import lishid.orebfuscator.Orebfuscator;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OrebfuscatorPlayerListenerHook implements Listener{
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		TryUpdateNetServerHandler(event.getPlayer());
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
