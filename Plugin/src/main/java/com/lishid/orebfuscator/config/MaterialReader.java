/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.orebfuscator.DeprecatedMethods;
import com.lishid.orebfuscator.utils.Globals;

public class MaterialReader {
		
    private JavaPlugin plugin;
    private Logger logger;
    
    public MaterialReader(JavaPlugin plugin, Logger logger) {
    	this.plugin = plugin;
		this.logger = logger;
    }
    
    private FileConfiguration getConfig() {
    	return this.plugin.getConfig();
    }
    
    public Material getMaterialId(String materialName) {
    	return getMaterial(materialName, null);
    }
	
    public Material getMaterialIdByPath(String path, Material defaultMaterialId, boolean withSave) {
    	boolean hasKey = getConfig().get(path) != null;
    	
    	if(!hasKey && defaultMaterialId == null) {
    		return null;
    	}
    	
    	String materialName = hasKey ? getConfig().getString(path): defaultMaterialId.name();
    	
    	Material material = Material.getMaterial(materialName);
    	
    	if(withSave || hasKey) {
    		getConfig().set(path, material.name());
    	}
		
		return material;
    }
    
    public Material[] getMaterialIdsByPath(String path, Material[] defaultMaterials, boolean withSave) {
    	List<String> list;
    	
     	if(getConfig().get(path) != null) {
    		list = getConfig().getStringList(path);
    		withSave = true;
    	} else {
    		if(defaultMaterials != null) {
        		list = new ArrayList<String>();

        		for(Material materialId : defaultMaterials) {
	    			list.add(materialId.name());
	    		}
    		} else {
    			return null;
    		}
    	}
    	
    	List<Material> result = new ArrayList<Material>();
    	
    	for(int i = 0; i < list.size(); i++) {
    		Material material = getMaterial(list.get(i), null);
    		
    		if(material != null) {
    			list.set(i, material.name());
    			result.add(material);
    		}
    	}
    	
    	if(withSave) {
    		getConfig().set(path, list);
    	}
    	
    	return result.toArray(new Material[0]);
    }
    
    private Material getMaterial(String materialName, Material defaultMaterialId) {
    	String defaultMaterialName = defaultMaterialId != null ? defaultMaterialId.name(): null;
    	Material materialId = null;
		try {
    		if(Character.isDigit(materialName.charAt(0))) {
    			this.logger.info(Globals.LogPrefix + "Material with Legacy Numeric ID = " + materialId + " is not supported. Skipped.");
    			materialId = null;
    		} else {
    			materialId = Material.getMaterial(materialName.toUpperCase());
    			
    			if(materialId == null) {
    				if(defaultMaterialId != null) {
	    				this.logger.info(Globals.LogPrefix + "Material " + materialName + " is not found. Will be used default material: " + defaultMaterialName);
	    				materialId = defaultMaterialId;
	    				materialName = defaultMaterialName;
    				} else {
    					this.logger.info(Globals.LogPrefix + "Material " + materialName + " is not found. Skipped.");
    					materialId = null;
    				}
    			}
    		}
		} catch (Exception e) {
			if(defaultMaterialId != null) {
				this.logger.info(Globals.LogPrefix + "Invalid material ID or name: " + materialName + ".  Will be used default material: " + defaultMaterialName);
				materialId = defaultMaterialId;
				materialName = defaultMaterialName;
			} else {
				this.logger.info(Globals.LogPrefix + "Invalid material ID or name: " + materialName + ". Skipped.");
				materialId = null;
			}
		}
		
		return materialId;
    }
}
