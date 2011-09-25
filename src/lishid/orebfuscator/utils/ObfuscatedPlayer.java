package lishid.orebfuscator.utils;

import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.server.Packet51MapChunk;

import org.bukkit.craftbukkit.entity.CraftPlayer;

public class ObfuscatedPlayer {
	CraftPlayer player;
	Queue<Packet51MapChunk> packetQueue = new LinkedList<Packet51MapChunk>();
	public ObfuscatedPlayer(CraftPlayer player)
	{
		this.player = player;
	}
	
	public void EnQueue(Packet51MapChunk packet)
	{
		packetQueue.add(packet);
	}
}
