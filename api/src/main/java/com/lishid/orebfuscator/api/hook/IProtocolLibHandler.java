package com.lishid.orebfuscator.api.hook;

import com.comphenix.protocol.ProtocolManager;
import com.lishid.orebfuscator.api.Handler;

public interface IProtocolLibHandler extends Handler {

	public ProtocolManager getManager();
}