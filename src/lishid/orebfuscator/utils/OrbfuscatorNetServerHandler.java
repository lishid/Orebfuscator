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
        	if(packet.k)
        	{
            	//Obfuscate packet
        		if(!OrebfuscatorCalculationThread.isRunning())
        		{
        			OrebfuscatorCalculationThread.startThread();
        		}
        		OrebfuscatorCalculationThread.Queue((Packet51MapChunk)packet, this.getPlayer());
        	}
        	else
        	{
        		//Packet already obfuscated
        		packet.k = true;
            	super.sendPacket(packet);
        	}
        }
        else
        {
        	super.sendPacket(packet);
        }
    }
}