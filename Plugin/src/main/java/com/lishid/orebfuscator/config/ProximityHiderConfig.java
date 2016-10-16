/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.ArrayList;
import java.util.List;

public class ProximityHiderConfig {
	private static final Integer[] defaultProximityHiderBlockIds = new Integer[]{ 23, 52, 54, 56, 58, 61, 62, 116, 129, 130, 145, 146 };
	
    private Boolean enabled;
    private Integer distance;
    private int distanceSquared;
    private Integer specialBlockID;
    private Integer endY;
    private Boolean useSpecialBlock;
    private Boolean useYLocationProximity;
    private boolean[] proximityHiderBlocks;
    
    public void setDefaults() {
        this.enabled = true;
        this.distance = 8;
        this.distanceSquared = this.distance * this.distance;
        this.specialBlockID = 1;
        this.endY = 255;
        this.useSpecialBlock = true;
        this.useYLocationProximity = false;
        this.proximityHiderBlocks = new boolean[256];
        
        for(Integer id : defaultProximityHiderBlockIds) {
        	this.proximityHiderBlocks[id] = true;
        }
    }
    
    public void init(ProximityHiderConfig baseCfg) {
    	if(this.enabled == null) {
    		this.enabled = baseCfg.enabled;
    	}
    	
    	if(this.distance == null) {
    		this.distance = baseCfg.distance;
    		this.distanceSquared = baseCfg.distanceSquared;
    	}
    	
    	if(this.specialBlockID == null) {
    		this.specialBlockID = baseCfg.specialBlockID;
    	}
    	
    	if(this.endY == null) {
    		this.endY = baseCfg.endY;
    	}
    	
        if(this.useSpecialBlock == null) {
        	this.useSpecialBlock = baseCfg.useSpecialBlock;
        }
        
        if(this.useYLocationProximity == null) {
        	this.useYLocationProximity = baseCfg.useYLocationProximity;
        }
        
        if(this.proximityHiderBlocks == null && baseCfg.proximityHiderBlocks != null) {
        	this.proximityHiderBlocks = baseCfg.proximityHiderBlocks.clone();
        }
    }
    
    public Boolean isEnabled() {
    	return this.enabled;
    }
    
    public void setEnabled(Boolean value) {
    	this.enabled = value;
    }
    
    public Integer getDistance() {
    	return this.distance;
    }
    
    public void setDistance(Integer value) {
    	this.distance = value;
    	this.distanceSquared = this.distance != null ? this.distance * this.distance: 0;
    }
    
    public int getDistanceSquared() {
    	return this.distanceSquared;
    }

    public Integer getSpecialBlockID() {
    	return this.specialBlockID;
    }
    
    public void setSpecialBlockID(Integer value) {
    	this.specialBlockID = value;
    }
    
    public Integer getEndY() {
    	return this.endY;
    }
    
    public void setEndY(Integer value) {
    	this.endY = value;
    }
    
    public Boolean isUseSpecialBlock() {
    	return this.useSpecialBlock;
    }
    
    public void setUseSpecialBlock(Boolean value) {
    	this.useSpecialBlock = value;
    }
    
    public Boolean isUseYLocationProximity() {
    	return this.useYLocationProximity;
    }
    
    public void setUseYLocationProximity(Boolean value) {
    	this.useYLocationProximity = value;
    }
    
    public boolean[] getProximityHiderBlocks() {
    	return this.proximityHiderBlocks;
    }
    
    public void setProximityHiderBlocks(boolean[] values) {
    	this.proximityHiderBlocks = values;
    }
    
    public Integer[] getProximityHiderBlockIds() {
    	if(this.proximityHiderBlocks == null) {
    		return null;
    	}
    	
    	List<Integer> result = new ArrayList<Integer>();
    	
    	for(Integer i = 0; i < this.proximityHiderBlocks.length; i++) {
    		if(this.proximityHiderBlocks[i]) {
    			result.add(i);
    		}
    	}
    	
    	return result.toArray(new Integer[0]);
    }
    
    // Help methods
    
    public Boolean isProximityObfuscated(Integer y, Integer id) {
        if (id < 0)
            id += 256;

        return this.proximityHiderBlocks[id]
        		&& (
        				this.useYLocationProximity && y >= this.endY
        				|| !this.useYLocationProximity && y <= this.endY
        			);
    }
    
    public Boolean skipProximityHiderCheck(Integer y) {
        return this.useYLocationProximity && y < this.endY;
    }
}
