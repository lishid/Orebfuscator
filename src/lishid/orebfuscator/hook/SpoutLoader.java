package lishid.orebfuscator.hook;

import lishid.orebfuscator.utils.Calculations;
import net.minecraft.server.Packet51MapChunk;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.getspout.spout.packet.standard.MCCraftPacket;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.packet.listener.PacketListener;
import org.getspout.spoutapi.packet.standard.MCPacket;

public class SpoutLoader {
	
	public static void InitializeSpout()
	{
		//Add spout listeners
		SpoutManager.getPacketManager().addListenerUncompressedChunk(new PacketListener(){
			
			//Processing a chunk packet
			public boolean checkPacket(Player player, MCPacket mcpacket)
			{
				if ((player == null) || (mcpacket == null) || (player.getWorld() == null)) return true;
				
				//Process the chunk
				if(((MCCraftPacket)mcpacket).getPacket() instanceof Packet51MapChunk)
				{
					Calculations.Obfuscate((Packet51MapChunk)((MCCraftPacket)mcpacket).getPacket(), (CraftPlayer)player, false, false);
				}
				return true;
			}
		});
	}
}
