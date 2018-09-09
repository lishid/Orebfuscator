/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.types.BlockState;

public class WorldConfig {
	private String name;
    private Boolean enabled;
    private Boolean darknessHideBlocks;
    private Boolean antiTexturePackAndFreecam;
    private Boolean bypassObfuscationForSignsWithText;
    private Integer airGeneratorMaxChance;    
    private boolean[] obfuscateBlocks;
    private boolean[] obfuscateAndProximityBlocks;
    private boolean[] darknessBlocks;
    private Material[] randomBlocks;
    private Material[] randomBlocks2;
    private Material mode1BlockId;
    private int[] paletteBlocks; // 1.13: super crazy "combined" registry super-id magic values. SCARY
    private ProximityHiderConfig proximityHiderConfig;
    private boolean initialized;
    
	/**
	 * Added for 1.13 to allow Integer encoding of Material for the transient lookup arrays.
	 */
	private static final Material[] translation = Material.values();

    
    public WorldConfig() {
    	this.proximityHiderConfig = new ProximityHiderConfig();
    }
    
    public void setDefaults() {
		this.enabled = true;
		this.darknessHideBlocks = false;
		this.antiTexturePackAndFreecam = true;
		this.bypassObfuscationForSignsWithText = false;
		this.airGeneratorMaxChance = 43;
		this.obfuscateBlocks = new boolean[translation.length];
		
		this.darknessBlocks = new boolean[translation.length];
		/*this.darknessBlocks[52] = true;
		this.darknessBlocks[54] = true;*/
		this.darknessBlocks[Material.SPAWNER.ordinal()] = true;
		this.darknessBlocks[Material.CHEST.ordinal()] = true;
		// 1.13 TODO: Are there other dark blocks related to hidden treasures that should by default now be hidden?

		this.randomBlocks = new Material[0];
		this.randomBlocks2 = this.randomBlocks;
		
	    this.mode1BlockId = Material.STONE; //1;
	    this.paletteBlocks = null;

	    this.proximityHiderConfig.setDefaults();
    }
    
    public void init(WorldConfig baseWorld) {
    	if(this.initialized) {
    		return;
    	}
    	
    	if(baseWorld != null) {
	    	if(this.enabled == null) {
	    		this.enabled = baseWorld.enabled;
	    	}
	        
	    	if(this.darknessHideBlocks == null) {
	    		this.darknessHideBlocks = baseWorld.darknessHideBlocks;
	    	}
	    	
	    	if(this.antiTexturePackAndFreecam == null) {
	    		this.antiTexturePackAndFreecam = baseWorld.antiTexturePackAndFreecam;
	    	}
	    	
	    	if(this.bypassObfuscationForSignsWithText == null) {
	    		this.bypassObfuscationForSignsWithText = baseWorld.bypassObfuscationForSignsWithText;
	    	}

	    	if(this.airGeneratorMaxChance == null) {
	    		this.airGeneratorMaxChance = baseWorld.airGeneratorMaxChance;
	    	}
	    	
	    	if(this.obfuscateBlocks == null) {
	    		this.obfuscateBlocks = baseWorld.obfuscateBlocks != null ? baseWorld.obfuscateBlocks.clone(): null;
	    	}
	    	
	    	if(this.darknessBlocks == null) {
	    		this.darknessBlocks = baseWorld.darknessBlocks != null ? baseWorld.darknessBlocks.clone(): null;
	    	}
	    	
	    	if(this.randomBlocks == null) {
		        this.randomBlocks = baseWorld.randomBlocks != null ? baseWorld.randomBlocks.clone(): null;
		        this.randomBlocks2 = baseWorld.randomBlocks2 != null ? baseWorld.randomBlocks2.clone(): null;
	    	}
	    	
	    	if(this.mode1BlockId == null) {
	    		this.mode1BlockId = baseWorld.mode1BlockId;
	    	}
	    	
	  		this.proximityHiderConfig.init(baseWorld.proximityHiderConfig);
	        setObfuscateAndProximityBlocks();
    	}
        
        setPaletteBlocks();
        
        this.initialized = true;
    }
    
    public boolean isInitialized() {
    	return this.initialized;
    }

    public String getName() {
    	return this.name;
	}

	public void setName(String value) {
    	this.name = value;
	}
    
    public Boolean isEnabled() {
    	return this.enabled;
    }
    
    public void setEnabled(Boolean value) {
    	this.enabled = value;
    }
    
    public Boolean isDarknessHideBlocks() {
    	return this.darknessHideBlocks;
    }
    
    public void setDarknessHideBlocks(Boolean value) {
    	this.darknessHideBlocks = value;
    }

    public Boolean isAntiTexturePackAndFreecam() {
    	return this.antiTexturePackAndFreecam;
    }
    
    public void setAntiTexturePackAndFreecam(Boolean value) {
    	this.antiTexturePackAndFreecam = value;
    }

    public Boolean isBypassObfuscationForSignsWithText() {
    	return this.bypassObfuscationForSignsWithText;
    }
    
    public void setBypassObfuscationForSignsWithText(Boolean value) {
    	this.bypassObfuscationForSignsWithText = value;
    }

    public Integer getAirGeneratorMaxChance() {
    	return this.airGeneratorMaxChance;
    }
    
    public void setAirGeneratorMaxChance(Integer value) {
    	this.airGeneratorMaxChance = value;
    }
    
    public boolean[] getObfuscateBlocks() {
    	return this.obfuscateBlocks;
    }
    
    public void setObfuscateBlocks(boolean[] values) {
    	this.obfuscateBlocks = values;
    }
    
    public Material[] getObfuscateBlockIds() {
    	if(this.obfuscateBlocks == null) {
    		return null;
    	}
    	
    	List<Material> result = new ArrayList<Material>();
    	
    	for(int i = 0; i < this.obfuscateBlocks.length; i++) {
    		if(this.obfuscateBlocks[i]) {
    			result.add(translation[i]);
    		}
    	}
    	
    	return result.toArray(new Material[0]);
    }
    
    private void setObfuscateAndProximityBlocks() {
    	this.obfuscateAndProximityBlocks = new boolean[translation.length];
    	
    	boolean isProximityHiderEnabled = this.proximityHiderConfig != null && this.proximityHiderConfig.isEnabled();
    	int[] proximityHiderBlocks = isProximityHiderEnabled ? this.proximityHiderConfig.getProximityHiderBlockMatrix(): null;
    	
    	for(int i = 0; i < this.obfuscateAndProximityBlocks.length; i++) {
    		this.obfuscateAndProximityBlocks[i] =
    				this.obfuscateBlocks[i]
    				|| isProximityHiderEnabled && proximityHiderBlocks[i] != 0
    				;
    	}
    }
    
    public boolean[] getObfuscateAndProximityBlocks() {
    	return this.obfuscateAndProximityBlocks;
    }
    
    public boolean[] getDarknessBlocks() {
    	return this.darknessBlocks;
    }
    
    public void setDarknessBlocks(boolean[] values) {
    	this.darknessBlocks = values;
    }

    public Material[] getDarknessBlockIds() {
    	if(this.darknessBlocks == null) {
    		return null;
    	}
    	
    	List<Material> result = new ArrayList<Material>();
    	
    	for(int i = 0; i < this.darknessBlocks.length; i++) {
    		if(this.darknessBlocks[i]) {
    			result.add(translation[i]);
    		}
    	}
    	
    	return result.toArray(new Material[0]);
    }

    public Material[] getRandomBlocks() {
    	return this.randomBlocks;
    }

    public void setRandomBlocks(Material[] values) {
    	this.randomBlocks = values;
    	this.randomBlocks2 = values;
    }
    
    public void shuffleRandomBlocks() {
        synchronized (this.randomBlocks) {
            Collections.shuffle(Arrays.asList(this.randomBlocks));
            Collections.shuffle(Arrays.asList(this.randomBlocks2));
        }
    }
    
    public Material getMode1BlockId() {
    	return this.mode1BlockId;
    }

    public void setMode1BlockId(Material value) {
    	this.mode1BlockId = value;
    }

    public int[] getPaletteBlocks() {
    	return this.paletteBlocks;
    }
    
    private void setPaletteBlocks() {
    	if(this.randomBlocks == null) {
    		return;
    	}
    	
    	HashSet<Integer> map = new HashSet<Integer>();
    	BlockState helper = new BlockState();
    	
    	Orebfuscator.nms.setBlockStateFromMaterial(Material.AIR, helper);
    	map.add(helper.id);
    	
    	Orebfuscator.nms.setBlockStateFromMaterial(this.mode1BlockId, helper);
    	map.add(helper.id);
    	
    	if(this.proximityHiderConfig.isUseSpecialBlock()) {
    		Orebfuscator.nms.setBlockStateFromMaterial(this.proximityHiderConfig.getSpecialBlockID(), helper);
    		map.add(helper.id);
    	}
    	
    	for(Material id : this.randomBlocks) {
    		if(id != null) {
    			Orebfuscator.nms.setBlockStateFromMaterial(id, helper);
    			map.add(helper.id);
    		}
    	}
    	
    	int[] paletteBlocks = new int[map.size()];
    	int index = 0;
    	
    	for(Integer id : map) {
    		paletteBlocks[index++] = id;
    	}
    	
    	this.paletteBlocks = paletteBlocks;
    }

    public ProximityHiderConfig getProximityHiderConfig() {
    	return this.proximityHiderConfig;
    }
    
    // Helper methods
    
    public boolean isObfuscated(Material id) {
        return this.obfuscateAndProximityBlocks[id.ordinal()];
    }

    public boolean isDarknessObfuscated(Material id) {
        return this.darknessBlocks[id.ordinal()];
    }

    public Material getRandomBlock(int index, boolean alternate) {
        return (alternate ? this.randomBlocks2[index] : this.randomBlocks[index]);
    }
}
