/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

public class ProximityHiderConfig {
    private boolean enabled;
    private int distance;
    private int specialBlockID;
    private int endY;
    private boolean useSpecialBlock;
    private boolean useYLocationProximity;
    private boolean[] proximityHiderBlocks;
    
    public boolean isEnabled() {
    	return this.enabled;
    }
    
    public void setEnabled(boolean value) {
    	this.enabled = value;
    }
    
    public int getDistance() {
    	return this.distance;
    }
    
    public void setDistance(int value) {
    	this.distance = value;
    }
    
    public int getSpecialBlockID() {
    	return this.specialBlockID;
    }
    
    public void setSpecialBlockID(int value) {
    	this.specialBlockID = value;
    }
    
    public int getEndY() {
    	return this.endY;
    }
    
    public void setEndY(int value) {
    	this.endY = value;
    }
    
    public boolean isUseSpecialBlock() {
    	return this.useSpecialBlock;
    }
    
    public void setUseSpecialBlock(boolean value) {
    	this.useSpecialBlock = value;
    }
    
    public boolean isUseYLocationProximity() {
    	return this.useYLocationProximity;
    }
    
    public void setUseYLocationProximity(boolean value) {
    	this.useYLocationProximity = value;
    }
    
    public boolean[] getProximityHiderBlocks() {
    	return this.proximityHiderBlocks;
    }
    
    public void setProximityHiderBlocks(boolean[] values) {
    	this.proximityHiderBlocks = values;
    }
    
    public ProximityHiderConfig clone() {
    	ProximityHiderConfig cfg = new ProximityHiderConfig();
        cfg.enabled = this.enabled;
        cfg.distance = this.distance;
        cfg.specialBlockID = this.specialBlockID;
        cfg.endY = this.endY;
        cfg.useSpecialBlock = this.useSpecialBlock;
        cfg.useYLocationProximity = this.useYLocationProximity;
        cfg.proximityHiderBlocks = this.proximityHiderBlocks;
        
        return cfg;
    }
    
    // Help methods
    
    public boolean isProximityObfuscated(int y, int id) {
        if (id < 0)
            id += 256;

        return this.proximityHiderBlocks[id]
        		&& (
        				this.useYLocationProximity && y >= this.endY
        				|| !this.useYLocationProximity && y <= this.endY
        			);
    }
    
    public boolean skipProximityHiderCheck(int y) {
        return this.useYLocationProximity && y < this.endY;
    }
}
