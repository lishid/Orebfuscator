package net.imprex.orebfuscator.chunk;

import java.util.BitSet;

import org.bukkit.World;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

public class ChunkStruct {

	public final World world;
	public final int chunkX;
	public final int chunkZ;

	public final boolean isOverworld;

	public final BitSet sectionMask;
	public final byte[] data;

	public ChunkStruct(PacketContainer packet, World world) {
		StructureModifier<Integer> packetInteger = packet.getIntegers();
		StructureModifier<BitSet> packetBitSet = packet.getSpecificModifier(BitSet.class);
		StructureModifier<byte[]> packetByteArray = packet.getByteArrays();

		this.world = world;
		this.chunkX = packetInteger.read(0);
		this.chunkZ = packetInteger.read(1);
		this.data = packetByteArray.read(0);
		this.isOverworld = world.getEnvironment() == World.Environment.NORMAL;
		
		if (ChunkCapabilities.hasDynamicHeight()) {
			this.sectionMask = packetBitSet.read(0);
		} else {
			this.sectionMask = convertIntToBitSet(packetInteger.read(2));
		}
	}

	public boolean isEmpty() {
		return this.sectionMask.isEmpty();
	}

	private BitSet convertIntToBitSet(int value) {
		BitSet bitSet = new BitSet();
		for (int index = 0; value != 0; index++) {
			if ((value & 1) == 1) {
				bitSet.set(index);
			}
			value >>>= 1;
		}
		return bitSet;
	}
}
