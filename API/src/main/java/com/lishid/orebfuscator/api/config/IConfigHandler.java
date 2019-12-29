package com.lishid.orebfuscator.api.config;

public interface IConfigHandler {

	public void loadOrebfuscatorConfig();

	public void reloadOrebfuscatorConfig();

	public IOrebfuscatorConfig getConfig();

	public IConfigManager getConfigManager();
}