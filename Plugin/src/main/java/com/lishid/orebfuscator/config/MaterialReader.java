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
	private static class MaterialResult {
		public Integer id;
		public String name;
		
		public MaterialResult(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}
	
    private JavaPlugin plugin;
    private Logger logger;
    
    public MaterialReader(JavaPlugin plugin, Logger logger) {
    	this.plugin = plugin;
		this.logger = logger;
    }
    
    private FileConfiguration getConfig() {
    	return this.plugin.getConfig();
    }
    
    public Integer getMaterialId(String materialName) {
    	return getMaterial(materialName, null).id;
    }
	
    public Integer getMaterialIdByPath(String path, Integer defaultMaterialId, boolean withSave) {
    	boolean hasKey = getConfig().get(path) != null;
    	
    	if(!hasKey && defaultMaterialId == null) {
    		return null;
    	}
    	
    	String materialName = hasKey ? getConfig().getString(path): Integer.toString(defaultMaterialId);
    	MaterialResult material = getMaterial(materialName, defaultMaterialId);
    	
    	if(withSave || hasKey) {
    		getConfig().set(path, material.name);
    	}
		
		return material.id;
    }
    
    public Integer[] getMaterialIdsByPath(String path, Integer[] defaultMaterials, boolean withSave) {
    	List<String> list;
    	
     	if(getConfig().get(path) != null) {
    		list = getConfig().getStringList(path);
    		withSave = true;
    	} else {
    		if(defaultMaterials != null) {
        		list = new ArrayList<String>();

        		for(int materialId : defaultMaterials) {
	    			list.add(DeprecatedMethods.getMaterial(materialId).name());
	    		}
    		} else {
    			return null;
    		}
    	}
    	
    	List<Integer> result = new ArrayList<Integer>();
    	
    	for(int i = 0; i < list.size(); i++) {
    		MaterialResult material = getMaterial(list.get(i), null);
    		
    		if(material != null) {
    			list.set(i, material.name);
    			result.add(material.id);
    		}
    	}
    	
    	if(withSave) {
    		getConfig().set(path, list);
    	}
    	
    	return result.toArray(new Integer[0]);
    }
    
    private MaterialResult getMaterial(String materialName, Integer defaultMaterialId) {
    	Integer materialId;
    	String defaultMaterialName = defaultMaterialId != null ? DeprecatedMethods.getMaterial(defaultMaterialId).name(): null;
    	
		try {
    		if(Character.isDigit(materialName.charAt(0))) {
    			materialId = Integer.parseInt(materialName);
    			
    			Material obj = DeprecatedMethods.getMaterial(materialId);
    			
    			if(obj != null) {
    				materialName = obj.name();
    			} else {
    				if(defaultMaterialId != null) {
	    				this.logger.info(Globals.LogPrefix + "Material with ID = " + materialId + " is not found. Will be used default material: " + defaultMaterialName);
	    				materialId = defaultMaterialId;
	    				materialName = defaultMaterialName;
    				} else {
    					this.logger.info(Globals.LogPrefix + "Material with ID = " + materialId + " is not found. Skipped.");
    					materialId = null;
    				}
    			}
    		} else {
    			Material obj = Material.getMaterial(materialName.toUpperCase());
    			
    			if(obj != null) {
    				materialId = DeprecatedMethods.getMaterialId(obj);
    			} else {
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
		
		return materialId != null ? new MaterialResult(materialId, materialName): null;
    }
}
