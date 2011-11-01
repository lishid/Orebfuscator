package lishid.orebfuscator.utils;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;

public class OrbfuscatorNetServerHandler extends NetServerHandler {
	public OrbfuscatorNetServerHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer)
	{
		super(minecraftserver, networkmanager, entityplayer);
	}
	
	@Override
    public void sendPacket(Packet packet) {
        if (packet instanceof Packet51MapChunk)
        {
        	//Obfuscate packet
    		if(!OrebfuscatorCalculationThread.CheckThreads())
    		{
    			OrebfuscatorCalculationThread.SyncThreads();
    		}
    		OrebfuscatorCalculationThread.Queue((Packet51MapChunk)packet, this.getPlayer());
        }
        else
        {
        	super.sendPacket(packet);
        }
    }
}