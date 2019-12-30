package com.lishid.orebfuscator.api.config;

import java.util.HashSet;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.nms.INmsManager;

public interface IWorldConfig {

	public void setDefaults();

	public void init(IWorldConfig baseWorld);

	public Boolean isInitialized();

	public String getName();

	public void setName(String value);

	public Boolean isEnabled();

	public void setEnabled(Boolean value);

	public Boolean isDarknessHideBlocks();

	public void setDarknessHideBlocks(Boolean value);

	public Boolean isAntiTexturePackAndFreecam();

	public void setAntiTexturePackAndFreecam(Boolean value);

	public Boolean isBypassObfuscationForSignsWithText();

	public void setBypassObfuscationForSignsWithText(Boolean value);

	public Integer getAirGeneratorMaxChance();

	public void setAirGeneratorMaxChance(Integer value);

	public HashSet<Integer> getObfuscateBlocks();

	public void setObfuscateBlocks(HashSet<Integer> value);

	public void setObfuscateBlocks(Integer[] value);

	public byte[] getObfuscateAndProximityBlocks();

	public HashSet<Integer> getDarknessBlocks();

	public void setDarknessBlocks(HashSet<Integer> values);

	public Integer[] getRandomBlocks();

	public Integer[] getRandomBlocks2();

	public void setRandomBlocks(Integer[] values);

	public void shuffleRandomBlocks();

	public Integer getMode1BlockId();

	public void setMode1BlockId(Integer value);

	public int[] getPaletteBlocks();

	public IProximityHiderConfig getProximityHiderConfig();

	public int getObfuscatedBits(int id);

	public int getRandomBlock(int index, boolean alternate);

	public Orebfuscator getPlugin();

	public INmsManager getNmsManager();
}