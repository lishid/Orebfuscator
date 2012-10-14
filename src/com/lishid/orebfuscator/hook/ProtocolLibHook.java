package com.lishid.orebfuscator.hook;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.obfuscation.ChunkInfo;
import com.lishid.orebfuscator.threading.ChunkCompressionThread;
import com.lishid.orebfuscator.threading.OrebfuscatorScheduler;
import com.lishid.orebfuscator.threading.OrebfuscatorSchedulerProtocolLib;
import com.lishid.orebfuscator.threading.OrebfuscatorThreadCalculation;

public class ProtocolLibHook {

	private class ChunkCompressorStream implements PacketStream {
		@Override
		public void recieveClientPacket(Player sender, PacketContainer packet)
				throws IllegalAccessException, InvocationTargetException {
			manager.recieveClientPacket(sender, packet, true);
		}
		@Override
		public void recieveClientPacket(Player sender, PacketContainer packet,
				boolean filters) throws IllegalAccessException,
				InvocationTargetException {
			manager.recieveClientPacket(sender, packet, filters);
		}
		@Override
		public void sendServerPacket(Player reciever, PacketContainer packet)
				throws InvocationTargetException {
			sendServerPacket(reciever, packet, true);
		}
		@Override
		public void sendServerPacket(Player reciever, PacketContainer packet,
				boolean filters) throws InvocationTargetException {
			
			CraftPlayer player = (CraftPlayer) reciever;
			Packet mcPacket = packet.getHandle();
			ChunkInfo[] info;
			
			if (mcPacket instanceof Packet51MapChunk) {
				info = new ChunkInfo[] { Calculations.getInfo((Packet51MapChunk) mcPacket, player) };
			} else if (mcPacket instanceof Packet56MapChunkBulk) {
				info = Calculations.getInfo((Packet56MapChunkBulk) mcPacket, player);
			} else {
				// Never mind
				manager.sendServerPacket(reciever, packet, filters);
				return;
			}
			
			ChunkCompressionThread.Queue((CraftPlayer) reciever, mcPacket, info);
		}
	}
	
	private ProtocolManager manager;
	
	private AsyncListenerHandler asyncHandler;
	private OrebfuscatorSchedulerProtocolLib scheduler;
	private ChunkCompressorStream stream;
	
	public void register(Plugin plugin) {
		
		Integer[] packets = new Integer[] { Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK };
		
		manager = ProtocolLibrary.getProtocolManager();
		stream = new ChunkCompressorStream();
		
		manager.addPacketListener(
			new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, packets) {
				@Override
				public void onPacketSending(PacketEvent event) {
					switch (event.getPacketID()) {
					case Packets.Server.MAP_CHUNK:
					case Packets.Server.MAP_CHUNK_BULK:
						// Create or remove more workers now
						scheduler.SyncThreads();
						break;
					}
				}
		});
		
		asyncHandler = manager.getAsynchronousManager().registerAsyncHandler(
			new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH, packets) {
				
				@Override
				public void onPacketSending(PacketEvent event) {
					try {
						AsyncMarker marker = event.getAsyncMarker();
						OrebfuscatorThreadCalculation thread = getWorker(marker);
						
						CraftPlayer player = (CraftPlayer) event.getPlayer();
						Packet packet = event.getPacket().getHandle();
						
						if (thread == null) {
							System.out.println("Cannot find worker " + marker.getWorkerID());
							return;
						}

						// Set priority too
						if (packet instanceof Packet51MapChunk) {
							if (scheduler.isImportant((Packet51MapChunk) packet, player))
								marker.setNewSendingIndex(0);
						} else if (packet instanceof Packet56MapChunkBulk) {	
							if (scheduler.isImportant((Packet56MapChunkBulk) packet, player))
								marker.setNewSendingIndex(0);
						} else {
							throw new IllegalArgumentException("Cannot process packet ID " + event.getPacketID());
						}
						
						marker.setPacketStream(stream);
						thread.processPacket(packet, player);
					
					} catch (Exception e) {
						Orebfuscator.log(e);
					}
				}
			});
		
		// Update thread scheduler
		scheduler = new OrebfuscatorSchedulerProtocolLib(this);
		OrebfuscatorScheduler.setScheduler(scheduler);
		scheduler.SyncThreads();
	}
	
	private OrebfuscatorThreadCalculation getWorker(AsyncMarker marker) {
		return scheduler.getCalculator(marker.getWorkerID());
	}

	public AsyncListenerHandler getAsyncHandler() {
		return asyncHandler;
	}
}
