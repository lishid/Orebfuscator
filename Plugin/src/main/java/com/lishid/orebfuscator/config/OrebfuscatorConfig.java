/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.Map;

import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;

public class OrebfuscatorConfig {
    // Caching
    private boolean useCache;
    private int maxLoadedCacheFiles;
    private String cacheLocation;
    private int deleteCacheFilesAfterDays;

    // Main engine config
    private boolean enabled;
    private boolean updateOnDamage;
    private int engineMode;
    private int initialRadius;
    private int updateRadius;
    private boolean noObfuscationForMetadata;
    private String noObfuscationForMetadataTagName;
    private boolean noObfuscationForOps;
    private boolean noObfuscationForPermission;
    private boolean loginNotification;

    private boolean[] transparentBlocks;
    
    private WorldConfig defaultWorld;
    private WorldConfig normalWorld;
    private WorldConfig endWorld;
    private WorldConfig netherWorld;
    private Map<String, WorldConfig> worlds;
    
    private boolean proximityHiderEnabled;
    
    private static final int antiHitHackDecrementFactor = 1000;
    private static final int antiHitHackMaxViolation = 15;
    private static final int proximityHiderRate = 500;
    private static final int chunkReloaderRate = 500;
    private static final boolean useChunkReloader = false;
    private static final long cacheCleanRate = 60 * 60 * 20;//once per hour
    
    public boolean isUseCache() {
    	return this.useCache;
    }
    
    public void setUseCache(boolean value) {
    	this.useCache = value;
    }

    public int getMaxLoadedCacheFiles() {
    	return this.maxLoadedCacheFiles;
    }
    
    public void setMaxLoadedCacheFiles(int value) {
    	this.maxLoadedCacheFiles = value;
    }

    public String getCacheLocation() {
    	return this.cacheLocation;
    }

    public void setCacheLocation(String value) {
    	this.cacheLocation = value;
    }

    public int getDeleteCacheFilesAfterDays() {
    	return this.deleteCacheFilesAfterDays;
    }

    public void setDeleteCacheFilesAfterDays(int value) {
    	this.deleteCacheFilesAfterDays = value;
    }

    public boolean isEnabled() {
    	return this.enabled;
    }
    
    public void setEnabled(boolean value) {
    	this.enabled = value;
    }

    public boolean isUpdateOnDamage() {
    	return this.updateOnDamage;
    }
    
    public void setUpdateOnDamage(boolean value) {
    	this.updateOnDamage = value;
    }

    public int getEngineMode() {
    	return this.engineMode;
    }
    
    public void setEngineMode(int value) {
    	this.engineMode = value;
    }

    public int getInitialRadius() {
    	return this.initialRadius;
    }
    
    public void setInitialRadius(int value) {
    	this.initialRadius = value;
    }

    public int getUpdateRadius() {
    	return this.updateRadius;
    }
    
    public void setUpdateRadius(int value) {
    	this.updateRadius = value;
    }

    public boolean isNoObfuscationForMetadata() {
    	return this.noObfuscationForMetadata;
    }
    
    public void setNoObfuscationForMetadata(boolean value) {
    	this.noObfuscationForMetadata = value;
    }

    public String getNoObfuscationForMetadataTagName() {
    	return this.noObfuscationForMetadataTagName;
    }
    
    public void setNoObfuscationForMetadataTagName(String value) {
    	this.noObfuscationForMetadataTagName = value;
    }

    public boolean isNoObfuscationForOps() {
    	return this.noObfuscationForOps;
    }
    
    public void setNoObfuscationForOps(boolean value) {
    	this.noObfuscationForOps = value;
    }

    public boolean isNoObfuscationForPermission() {
    	return this.noObfuscationForPermission;
    }
    
    public void setNoObfuscationForPermission(boolean value) {
    	this.noObfuscationForPermission = value;
    }

    public boolean isLoginNotification() {
    	return this.loginNotification;
    }
    
    public void setLoginNotification(boolean value) {
    	this.loginNotification = value;
    }

    public boolean[] getTransparentBlocks() {
    	return this.transparentBlocks;
    }
    
    public void setTransparentBlocks(boolean[] value) {
    	this.transparentBlocks = value;
    }
    
    public WorldConfig getDefaultWorld() {
    	return this.defaultWorld;
    }
    
    public void setDefaultWorld(WorldConfig value) {
    	this.defaultWorld = value;
    }

    public WorldConfig getNormalWorld() {
    	return this.normalWorld;
    }
    
    public void setNormalWorld(WorldConfig value) {
    	this.normalWorld = value;
    }

    public WorldConfig getEndWorld() {
    	return this.endWorld;
    }
    
    public void setEndWorld(WorldConfig value) {
    	this.endWorld = value;
    }
    
    public WorldConfig getNetherWorld() {
    	return this.netherWorld;
    }
    
    public void setNetherWorld(WorldConfig value) {
    	this.netherWorld = value;
    }
    
    public Map<String, WorldConfig> getWorlds() {
    	return this.worlds;
    }
    
    public void setWorlds(Map<String, WorldConfig> value) {
    	this.worlds = value;
    }
    
    public boolean isProximityHiderEnabled() {
    	return this.proximityHiderEnabled;
    }

    public void setProximityHiderEnabled() {
        this.proximityHiderEnabled = this.normalWorld.getProximityHiderConfig().isEnabled()
        		|| this.endWorld.getProximityHiderConfig().isEnabled()
        		|| this.netherWorld.getProximityHiderConfig().isEnabled()
        		;
        
        if(!this.proximityHiderEnabled) {
        	for(WorldConfig world : this.worlds.values()) {
        		if(world.getProximityHiderConfig().isEnabled() != null && world.getProximityHiderConfig().isEnabled()) {
        			this.proximityHiderEnabled = true;
        			break;
        		}
        	}
        }
    }

    public int getAntiHitHackDecrementFactor() {
    	return antiHitHackDecrementFactor;
    }
    
    public int getAntiHitHackMaxViolation() {
    	return antiHitHackMaxViolation;
    }
    
    public int getProximityHiderRate() {
    	return proximityHiderRate;
    }
    
    public int getChunkReloaderRate() {
    	return chunkReloaderRate;
    }
    
    public boolean isUseChunkReloader() {
    	return useChunkReloader;
    }
    
    public long getCacheCleanRate() {
    	return cacheCleanRate;
    }
    
    // Helper methods
    
    public boolean isBlockTransparent(int id) {
        if (id < 0)
            id += 256;

        if (id >= 256) {
            return false;
        }

        return this.transparentBlocks[id];
    }
    
    public boolean obfuscateForPlayer(Player player) {
        return !(playerBypassOp(player) || playerBypassPerms(player) || playerBypassMetadata(player));
    }

    public boolean playerBypassOp(Player player) {
        boolean ret = false;
        try {
            ret = this.noObfuscationForOps && player.isOp();
        } catch (Exception e) {
            Orebfuscator.log("Error while obtaining Operator status for player" + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    public boolean playerBypassPerms(Player player) {
        boolean ret = false;
        try {
            ret = this.noObfuscationForPermission && player.hasPermission("Orebfuscator.deobfuscate");
        } catch (Exception e) {
            Orebfuscator.log("Error while obtaining permissions for player" + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    public boolean playerBypassMetadata(Player player) {
        boolean ret = false;
        try {
            ret = this.noObfuscationForMetadata
            		&& player.hasMetadata(this.noObfuscationForMetadataTagName)
            		&& player.getMetadata(this.noObfuscationForMetadataTagName).get(0).asBoolean();
        } catch (Exception e) {
            Orebfuscator.log("Error while obtaining metadata for player" + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }
}