package com.lishid.orebfuscator.api;

public interface Handler {

	default void onInit() { }
	default void onEnable() { }
	default void onDisable() { }

	default boolean enableHandler() {
		return true;
	}

	public void init();
	public void enable();
	public void disable();

	public void reload();

	public boolean isEnabled();

	public Orebfuscator getPlugin();
}