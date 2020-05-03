package net.imprex.orebfuscator.proximityhider;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import net.imprex.orebfuscator.Orebfuscator;
import net.imprex.orebfuscator.config.OrebfuscatorConfig;
import net.imprex.orebfuscator.config.WorldConfig;
import net.imprex.orebfuscator.util.PermissionUtil;

public class ProximityPacketListener extends PacketAdapter {

	private final ProtocolManager protocolManager;

	private final OrebfuscatorConfig config;

	private final ProximityHider proximityHider;

	public ProximityPacketListener(Orebfuscator orebfuscator) {
		super(orebfuscator, PacketType.Play.Server.UNLOAD_CHUNK);

		this.protocolManager = ProtocolLibrary.getProtocolManager();
		this.protocolManager.addPacketListener(this);

		this.config = orebfuscator.getOrebfuscatorConfig();
		this.proximityHider = orebfuscator.getProximityHider();
	}

	public void unregister() {
		this.protocolManager.removePacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Player player = event.getPlayer();
		if (PermissionUtil.canDeobfuscate(player)) {
			return;
		}

		World world = player.getWorld();
		WorldConfig worldConfig = config.world(world);
		if (worldConfig == null || !worldConfig.enabled()) {
			return;
		}

		StructureModifier<Integer> ints = event.getPacket().getIntegers();

		this.proximityHider.removeProximityChunks(player, world, ints.read(0), ints.read(1));
	}
}
