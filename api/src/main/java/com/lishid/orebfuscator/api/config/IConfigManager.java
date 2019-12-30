package com.lishid.orebfuscator.api.config;

import org.bukkit.World;

public interface IConfigManager {

	public IWorldConfig getWorld(World world);

	public void load();

	public void setEngineMode(int value);
	public void setUpdateRadius(int value);
	public void setInitialRadius(int value);
	public void setProximityHiderDistance(int value);
	public void setNoObfuscationForOps(boolean value);
	public void setNoObfuscationForPermission(boolean value);
	public void setLoginNotification(boolean value);
	public void setUseCache(boolean value);
	public void setEnabled(boolean value);
}