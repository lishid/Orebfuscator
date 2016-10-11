/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.Arrays;
import java.util.Collections;

public class WorldConfig {
    private boolean enabled;
    private boolean darknessHideBlocks;
    private boolean antiTexturePackAndFreecam;
    private int airGeneratorMaxChance;
    private boolean[] obfuscateBlocks;
    private boolean[] darknessBlocks;
    private Integer[] randomBlocks;
    private Integer[] randomBlocks2;
    private int mode1BlockId;
    private int[] paletteBlocks;
    private ProximityHiderConfig proximityHiderConfig;
    
    public boolean isEnabled() {
    	return this.enabled;
    }
    
    public void setEnabled(boolean value) {
    	this.enabled = value;
    }
    
    public boolean isDarknessHideBlocks() {
    	return this.darknessHideBlocks;
    }
    
    public void setDarknessHideBlocks(boolean value) {
    	this.darknessHideBlocks = value;
    }

    public boolean isAntiTexturePackAndFreecam() {
    	return this.antiTexturePackAndFreecam;
    }
    
    public void setAntiTexturePackAndFreecam(boolean value) {
    	this.antiTexturePackAndFreecam = value;
    }

    public int getAirGeneratorMaxChance() {
    	return this.airGeneratorMaxChance;
    }
    
    public void setAirGeneratorMaxChance(int value) {
    	this.airGeneratorMaxChance = value;
    }
    
    public boolean[] getObfuscateBlocks() {
    	return this.obfuscateBlocks;
    }
    
    public void setObfuscateBlocks(boolean[] values) {
    	this.obfuscateBlocks = values;
    }
    
    public boolean[] getDarknessBlocks() {
    	return this.darknessBlocks;
    }
    
    public void setDarknessBlocks(boolean[] values) {
    	this.darknessBlocks = values;
    }

    public Integer[] getRandomBlocks() {
    	return this.randomBlocks;
    }

    public Integer[] getRandomBlocks2() {
    	return this.randomBlocks2;
    }

    public void setRandomBlocks(Integer[] values) {
    	this.randomBlocks = values;
    	this.randomBlocks2 = values;
    }
    
    public void shuffleRandomBlocks() {
        synchronized (this.randomBlocks) {
            Collections.shuffle(Arrays.asList(this.randomBlocks));
            Collections.shuffle(Arrays.asList(this.randomBlocks2));
        }
    }
    
    public int getMode1BlockId() {
    	return this.mode1BlockId;
    }

    public void setMode1BlockId(int value) {
    	this.mode1BlockId = value;
    }

    public int[] getPaletteBlocks() {
    	return this.paletteBlocks;
    }

    public void setPaletteBlocks(int[] values) {
    	this.paletteBlocks = values;
    }

    public ProximityHiderConfig getProximityHiderConfig() {
    	return this.proximityHiderConfig;
    }

    public void setProximityHiderConfig(ProximityHiderConfig value) {
    	this.proximityHiderConfig = value;
    }
    
    public WorldConfig clone() {
    	WorldConfig cfg = new WorldConfig();
        cfg.enabled = this.enabled;
        cfg.darknessHideBlocks = this.darknessHideBlocks;
        cfg.antiTexturePackAndFreecam = this.antiTexturePackAndFreecam;
        cfg.airGeneratorMaxChance = this.airGeneratorMaxChance;
        cfg.obfuscateBlocks = this.obfuscateBlocks;
        cfg.darknessBlocks = this.darknessBlocks;
        cfg.randomBlocks = this.randomBlocks;
        cfg.randomBlocks2 = this.randomBlocks2;
        cfg.paletteBlocks = this.paletteBlocks;
        cfg.proximityHiderConfig = this.proximityHiderConfig;
        
        return cfg;
    }
    
    // Helper methods
    
    public boolean isObfuscated(int id) {
        if (id < 0)
            id += 256;
        
        return this.obfuscateBlocks[id];
    }

    public boolean isDarknessObfuscated(int id) {
        if (id < 0)
            id += 256;

        return this.darknessBlocks[id];
    }

    public int getRandomBlock(int index, boolean alternate) {
        return (int)(alternate ? this.randomBlocks2[index] : this.randomBlocks[index]);
    }
}
