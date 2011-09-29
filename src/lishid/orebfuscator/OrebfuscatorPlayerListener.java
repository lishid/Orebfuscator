package lishid.orebfuscator;

import java.lang.reflect.Field;

import lishid.orebfuscator.utils.Calculations;
import lishid.orebfuscator.utils.OrbfuscatorNetServerHandler;
import lishid.orebfuscator.utils.OrebfuscatorConfig;

import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

public class OrebfuscatorPlayerListener extends PlayerListener
{
    Orebfuscator plugin;
    public OrebfuscatorPlayerListener(Orebfuscator plugin) {
        this.plugin = plugin;
    }
	
	@Override
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		TryUpdateNetServerHandler(event.getPlayer());
	}

	@Override
	public void onPlayerQuit(final PlayerQuitEvent event)
	{
		if(OrebfuscatorBlockListener.blockLog.containsKey(event.getPlayer().getName()))
		{
			OrebfuscatorBlockListener.blockLog.remove(event.getPlayer().getName());
		}
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.isCancelled() || !OrebfuscatorConfig.DarknessHideBlocks() || !OrebfuscatorConfig.Enabled() || 
				event.useInteractedBlock() == Result.DENY || event.getAction()!=Action.RIGHT_CLICK_BLOCK)
			return;
		if(event.getMaterial().getId() == 10 || 
				event.getMaterial().getId() == 11 || 
				event.getMaterial().getId() == 327)
			Calculations.LightingUpdate(event.getClickedBlock(), true);
	}
	
	public void TryUpdateNetServerHandler(Player player)
	{
		try
		{
			updateNetServerHandler(player);
		}
		catch(Exception e)
		{
			System.out.println("[Orebfuscator] Error updating NerServerHandler.");
			e.printStackTrace();
		}
	}

	public void updateNetServerHandler(Player player) {
		CraftPlayer cp = (CraftPlayer)player;
		CraftServer server = (CraftServer)Bukkit.getServer();

		if (!(cp.getHandle().netServerHandler.getClass().equals(OrbfuscatorNetServerHandler.class))) {
			NetServerHandler oldHandler = cp.getHandle().netServerHandler;
			Location loc = player.getLocation();
			OrbfuscatorNetServerHandler handler = new OrbfuscatorNetServerHandler(server.getHandle().server, cp.getHandle().netServerHandler.networkManager, cp.getHandle());
			handler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			cp.getHandle().netServerHandler = handler;
			NetworkManager nm = cp.getHandle().netServerHandler.networkManager;
			setNetServerHandler(nm, handler);
			oldHandler.disconnected = true;
			((CraftServer)player.getServer()).getServer().networkListenThread.a(handler);
		}
	}
	
	public void setNetServerHandler(NetworkManager nm, NetServerHandler nsh) {
		try {
			Field p = nm.getClass().getDeclaredField("p");
			p.setAccessible(true);
			p.set(nm, nsh);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		return;
	}
}
