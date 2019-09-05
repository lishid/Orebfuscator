/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.lishid.orebfuscator.NmsInstance;
import com.lishid.orebfuscator.utils.Globals;
import com.lishid.orebfuscator.utils.MaterialHelper;

public class WorldConfig {
	private String name;
	private Boolean enabled;
	private Boolean darknessHideBlocks;
	private Boolean antiTexturePackAndFreecam;
	private Boolean bypassObfuscationForSignsWithText;
	private Integer airGeneratorMaxChance;
	private HashSet<Integer> obfuscateBlocks;
	private HashSet<Integer> darknessBlocks;
	private byte[] obfuscateAndProximityBlocks;
	private Integer[] randomBlocks;
	private Integer[] randomBlocks2;
	private Integer mode1BlockId;
	private int[] paletteBlocks;
	private ProximityHiderConfig proximityHiderConfig;
	private boolean initialized;

	public WorldConfig() {
		this.proximityHiderConfig = new ProximityHiderConfig();
	}

	public void setDefaults() {
		this.enabled = true;
		this.darknessHideBlocks = false;
		this.antiTexturePackAndFreecam = true;
		this.bypassObfuscationForSignsWithText = false;
		this.airGeneratorMaxChance = 43;
		this.obfuscateBlocks = new HashSet<>();

		this.darknessBlocks = new HashSet<>();
		for (int blockId : NmsInstance.current.getConfigDefaults().defaultDarknessBlockIds) {
			this.darknessBlocks.add(blockId);
		}

		this.randomBlocks = new Integer[0];
		this.randomBlocks2 = this.randomBlocks;

		this.mode1BlockId = NmsInstance.current.getConfigDefaults().defaultMode1BlockId;
		this.paletteBlocks = null;

		this.proximityHiderConfig.setDefaults();
	}

	public void init(WorldConfig baseWorld) {
		if (this.initialized) {
			return;
		}

		if (baseWorld != null) {
			if (this.enabled == null) {
				this.enabled = baseWorld.enabled;
			}

			if (this.darknessHideBlocks == null) {
				this.darknessHideBlocks = baseWorld.darknessHideBlocks;
			}

			if (this.antiTexturePackAndFreecam == null) {
				this.antiTexturePackAndFreecam = baseWorld.antiTexturePackAndFreecam;
			}

			if (this.bypassObfuscationForSignsWithText == null) {
				this.bypassObfuscationForSignsWithText = baseWorld.bypassObfuscationForSignsWithText;
			}

			if (this.airGeneratorMaxChance == null) {
				this.airGeneratorMaxChance = baseWorld.airGeneratorMaxChance;
			}

			if (this.obfuscateBlocks == null) {
				this.obfuscateBlocks = baseWorld.obfuscateBlocks != null
						?  baseWorld.obfuscateBlocks.stream().collect(Collectors.toCollection(HashSet::new))
						: null;
			}

			if (this.darknessBlocks == null) {
				this.darknessBlocks = baseWorld.darknessBlocks != null
						? baseWorld.darknessBlocks.stream().collect(Collectors.toCollection(HashSet::new))
						: null;
			}

			if (this.randomBlocks == null) {
				this.randomBlocks = baseWorld.randomBlocks != null ? baseWorld.randomBlocks.clone() : null;
				this.randomBlocks2 = baseWorld.randomBlocks2 != null ? baseWorld.randomBlocks2.clone() : null;
			}

			if (this.mode1BlockId == null) {
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

	public HashSet<Integer> getObfuscateBlocks() {
		return this.obfuscateBlocks;
	}

	public void setObfuscateBlocks(HashSet<Integer> value) {
		this.obfuscateBlocks = value;
	}

	public void setObfuscateBlocks(Integer[] value) {
		this.obfuscateBlocks = new HashSet<>();

		for (int id : value) {
			this.obfuscateBlocks.add(id);
		}
	}

	private void setObfuscateAndProximityBlocks() {
		this.obfuscateAndProximityBlocks = new byte[MaterialHelper.getMaxId() + 1];

		setObfuscateMask(this.obfuscateBlocks, false, false);

		if (this.darknessBlocks != null && this.darknessHideBlocks)
			setObfuscateMask(this.darknessBlocks, true, false);

		if (this.proximityHiderConfig != null && this.proximityHiderConfig.isEnabled())
			setObfuscateMask(this.proximityHiderConfig.getProximityHiderBlocks(), false, true);
	}

	private void setObfuscateMask(Set<Integer> blockIds, boolean isDarknessBlock, boolean isProximityHider) {
		for (int blockId : blockIds) {
			int bits = this.obfuscateAndProximityBlocks[blockId] | Globals.MASK_OBFUSCATE;

			if (isDarknessBlock)
				bits |= Globals.MASK_DARKNESSBLOCK;

			if (isProximityHider)
				bits |= Globals.MASK_PROXIMITYHIDER;

			if (NmsInstance.current.isTileEntity(blockId))
				bits |= Globals.MASK_TILEENTITY;

			this.obfuscateAndProximityBlocks[blockId] = (byte) bits;
		}
	}

	public byte[] getObfuscateAndProximityBlocks() {
		return this.obfuscateAndProximityBlocks;
	}

	public HashSet<Integer> getDarknessBlocks() {
		return this.darknessBlocks;
	}

	public void setDarknessBlocks(HashSet<Integer> values) {
		this.darknessBlocks = values;
	}

	public Integer[] getRandomBlocks() {
		return this.randomBlocks;
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

	public Integer getMode1BlockId() {
		return this.mode1BlockId;
	}

	public void setMode1BlockId(Integer value) {
		this.mode1BlockId = value;
	}

	public int[] getPaletteBlocks() {
		return this.paletteBlocks;
	}

	private void setPaletteBlocks() {
		if (this.randomBlocks == null)
			return;

		HashSet<Integer> map = new HashSet<Integer>();

		map.add(NmsInstance.current.getCaveAirBlockId());
		map.add(this.mode1BlockId);

		if (this.proximityHiderConfig.isUseSpecialBlock())
			map.add(this.proximityHiderConfig.getSpecialBlockID());

		for (Integer id : this.randomBlocks) {
			if (id != null) {
				map.add(id);
			}
		}

		int[] paletteBlocks = new int[map.size()];
		int index = 0;

		for (Integer id : map) {
			paletteBlocks[index++] = id;
		}

		this.paletteBlocks = paletteBlocks;
	}

	public ProximityHiderConfig getProximityHiderConfig() {
		return this.proximityHiderConfig;
	}

	// Helper methods

	public int getObfuscatedBits(int id) {
		return this.obfuscateAndProximityBlocks[id];
	}

	public int getRandomBlock(int index, boolean alternate) {
		return alternate ? this.randomBlocks2[index] : this.randomBlocks[index];
	}
}
