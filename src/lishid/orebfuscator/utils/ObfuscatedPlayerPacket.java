package lishid.orebfuscator.utils;

import net.minecraft.server.Packet51MapChunk;

import org.bukkit.craftbukkit.entity.CraftPlayer;

public class ObfuscatedPlayerPacket {
	public CraftPlayer player;
	public Packet51MapChunk packet;
	public ObfuscatedPlayerPacket(CraftPlayer player, Packet51MapChunk packet)
	{
		this.player = player;
		this.packet = packet;
	}
}
