package lishid.orebfuscator.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;

public class OrebfuscatorNetServerHandler extends NetServerHandlerProxy {
	
	public OrebfuscatorNetServerHandler(MinecraftServer minecraftserver, NetServerHandler instance) {
		super(minecraftserver, instance);
	}

	@Override
    public void sendPacket(Packet packet) {
        if (packet instanceof Packet51MapChunk)
        {
        	//Obfuscate packet
			OrebfuscatorThreadCalculation.SyncThreads();
    		OrebfuscatorThreadCalculation.Queue((Packet51MapChunk)packet, this.getPlayer());
        }
        else
        {
        	super.sendPacket(packet);
        }
    }
}