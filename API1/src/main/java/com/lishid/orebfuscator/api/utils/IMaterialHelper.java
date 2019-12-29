package com.lishid.orebfuscator.api.utils;

import org.bukkit.Material;

public interface IMaterialHelper {

	public Material getById(int combinedBlockId);

	public int getMaxId();
}