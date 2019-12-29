package com.lishid.orebfuscator.api.config;

import java.util.HashSet;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.nms.INmsManager;

public interface IProximityHiderConfig {

	public void setDefaults();

	public void init(IProximityHiderConfig baseCfg);

	public Boolean isEnabled();

	public void setEnabled(Boolean value);

	public Integer getDistance();

	public void setDistance(Integer value);

	public int getDistanceSquared();

	public Integer getSpecialBlockID();

	public void setSpecialBlockID(Integer value);

	public Integer getY();

	public void setY(Integer value);

	public Boolean isUseSpecialBlock();

	public void setUseSpecialBlock(Boolean value);

	public Boolean isObfuscateAboveY();

	public void setObfuscateAboveY(Boolean value);

	public void setProximityHiderBlocks(Integer[] blockIds);

	public HashSet<Integer> getProximityHiderBlocks();

	public IBlockSetting[] getProximityHiderBlockSettings();

	public void setProximityHiderBlockSettings(IBlockSetting[] value);

	public Boolean isUseFastGazeCheck();

	public void setUseFastGazeCheck(Boolean value);

	public boolean isProximityObfuscated(int y, int id);

	public Orebfuscator getPlugin();

	public INmsManager getNmsManager();

	public interface IBlockSetting extends Cloneable {

		public int getBlockId();
		public int getY();
		public boolean obfuscateAboveY();

		public IBlockSetting clone() throws CloneNotSupportedException;
	}
}