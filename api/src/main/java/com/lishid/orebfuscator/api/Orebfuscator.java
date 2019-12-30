package com.lishid.orebfuscator.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.lishid.orebfuscator.api.cache.IObfuscatedDataCacheHandler;
import com.lishid.orebfuscator.api.chunk.IChunkMapHandler;
import com.lishid.orebfuscator.api.config.IConfigHandler;
import com.lishid.orebfuscator.api.hithack.IBlockHitHandler;
import com.lishid.orebfuscator.api.hook.IProtocolLibHandler;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.utils.IBlockUpdate;
import com.lishid.orebfuscator.api.utils.ICalculations;
import com.lishid.orebfuscator.api.utils.IMaterialHelper;

public interface Orebfuscator extends Plugin {

	public static Orebfuscator getProvider() {
		RegisteredServiceProvider<Orebfuscator> provider = Bukkit.getServicesManager().getRegistration(Orebfuscator.class);
		return provider != null ? provider.getProvider() : null;
	}

	public INmsManager getNmsManager();

	public IConfigHandler getConfigHandler();

	public IChunkMapHandler getChunkMapHandler();

	public IObfuscatedDataCacheHandler getObfuscatedDataCacheHandler();

	public IBlockHitHandler getBlockHitHandler();

	public IProximityHiderHandler getProximityHiderHandler();

	public IProtocolLibHandler getProtocolLibHandler();

	public ICalculations getCalculations();

	public IBlockUpdate getBlockUpdate();

	public IMaterialHelper getMaterialHelper();
}