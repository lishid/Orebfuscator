package com.lishid.orebfuscator.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OFCPlayerKickEvent extends Event implements Cancellable {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final Player player;
	private final KickReason reason;
	private String message;

	private boolean cancel = false;

	public OFCPlayerKickEvent(Player player, KickReason reason, String message) {
		this.player = player;
		this.reason = reason;
		this.message = message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public boolean isCancelled() {
		return this.cancel;
	}

	public String getMessage() {
		return this.message;
	}

	public Player getPlayer() {
		return this.player;
	}

	public KickReason getReason() {
		return this.reason;
	}

	@Override
	public HandlerList getHandlers() {
		return OFCPlayerKickEvent.HANDLER_LIST;
	}

	public enum KickReason {
		PACKET_SPAMMING
	}
}