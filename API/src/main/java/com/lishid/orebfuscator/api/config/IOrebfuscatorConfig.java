package com.lishid.orebfuscator.api.config;

import java.util.Map;

import org.bukkit.entity.Player;

public interface IOrebfuscatorConfig {

	public boolean isUseCache();

	public void setUseCache(boolean value);

	public int getMaxLoadedCacheFiles();

	public void setMaxLoadedCacheFiles(int value);

	public String getCacheLocation();

	public void setCacheLocation(String value);

	public int getDeleteCacheFilesAfterDays();

	public void setDeleteCacheFilesAfterDays(int value);

	public boolean isEnabled();

	public void setEnabled(boolean value);

	public boolean isUpdateOnDamage();

	public void setUpdateOnDamage(boolean value);

	public int getEngineMode();

	public void setEngineMode(int value);

	public int getInitialRadius();

	public void setInitialRadius(int value);

	public int getUpdateRadius();

	public void setUpdateRadius(int value);

	public boolean isNoObfuscationForMetadata();

	public void setNoObfuscationForMetadata(boolean value);

	public String getNoObfuscationForMetadataTagName();

	public void setNoObfuscationForMetadataTagName(String value);

	public boolean isNoObfuscationForOps();

	public void setNoObfuscationForOps(boolean value);

	public boolean isNoObfuscationForPermission();

	public void setNoObfuscationForPermission(boolean value);

	public boolean isLoginNotification();

	public void setLoginNotification(boolean value);

	public void setTransparentBlocks(byte[] transparentBlocks);

	public IWorldConfig getDefaultWorld();

	public void setDefaultWorld(IWorldConfig value);

	public IWorldConfig getNormalWorld();

	public void setNormalWorld(IWorldConfig value);

	public IWorldConfig getEndWorld();

	public void setEndWorld(IWorldConfig value);

	public IWorldConfig getNetherWorld();

	public void setNetherWorld(IWorldConfig value);

	public String getWorldNames();

	public IWorldConfig getWorld(String name);

	public void setWorlds(Map<String, IWorldConfig> value);

	public boolean isProximityHiderEnabled();

	public void setProximityHiderEnabled();

	public int getAntiHitHackDecrementFactor();

	public int getAntiHitHackMaxViolation();

	public int getProximityHiderRate();

	public long getCacheCleanRate();

	public boolean isBlockTransparent(int id);

	public boolean obfuscateForPlayer(Player player);

	public boolean playerBypassOp(Player player);

	public boolean playerBypassPerms(Player player);

	public boolean playerBypassMetadata(Player player);
}