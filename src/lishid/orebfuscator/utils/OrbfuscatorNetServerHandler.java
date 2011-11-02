package lishid.orebfuscator.utils;

import org.bukkit.craftbukkit.NetServerHandlerProxy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;

public class OrbfuscatorNetServerHandler extends NetServerHandlerProxy {
	
	public OrbfuscatorNetServerHandler(MinecraftServer minecraftserver, NetServerHandler instance) {
		super(minecraftserver, instance);
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