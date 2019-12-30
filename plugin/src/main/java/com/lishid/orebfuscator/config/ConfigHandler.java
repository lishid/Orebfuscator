package com.lishid.orebfuscator.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.lishid.orebfuscator.CraftOrebfuscator;
import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.config.IConfigHandler;
import com.lishid.orebfuscator.api.config.IConfigManager;
import com.lishid.orebfuscator.api.logger.OFCLogger;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.handler.CraftHandler;

public class ConfigHandler extends CraftHandler implements IConfigHandler {

	private INmsManager nmsManager;

	private OrebfuscatorConfig config;
	private IConfigManager configManager;

	public ConfigHandler(Orebfuscator core) {
		super(core);
	}

	@Override
	public void onInit() {
		this.nmsManager = this.plugin.getNmsManager();

		this.createConfigExample();
		this.loadOrebfuscatorConfig();
	}

	public void loadOrebfuscatorConfig() {
		if (this.config == null) {
			this.config = new OrebfuscatorConfig();
			this.configManager = new ConfigManager(this.plugin, OFCLogger.logger, this.config);
		}

		this.configManager.load();

		this.plugin.getObfuscatedDataCacheHandler().resetCacheFolder();

		this.nmsManager.setMaxLoadedCacheFiles(this.config.getMaxLoadedCacheFiles());

		// Make sure cache is cleared if config was changed since last start
		try {
			this.plugin.getObfuscatedDataCacheHandler().checkCacheAndConfigSynchronized();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createConfigExample() {
		File outputFile = new File(this.plugin.getDataFolder(), "config.example_enabledworlds.yml");

		if (outputFile.exists())
			return;

		InputStream configStream = CraftOrebfuscator.class.getResourceAsStream("/resources/config.example_enabledworlds.yml");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(configStream));
				PrintWriter writer = new PrintWriter(outputFile)) {
			String line;

			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reloadOrebfuscatorConfig() {
		this.plugin.reloadConfig();
		this.loadOrebfuscatorConfig();
	}

	public OrebfuscatorConfig getConfig() {
		return this.config;
	}

	public IConfigManager getConfigManager() {
		return this.configManager;
	}
}