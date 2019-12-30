package com.lishid.orebfuscator.handler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;

import com.lishid.orebfuscator.api.Handler;
import com.lishid.orebfuscator.api.Orebfuscator;

public class CraftHandler implements Handler {

	private static final List<CraftHandler> CraftHandlers = new LinkedList<CraftHandler>();

	public static List<CraftHandler> getCraftHandlers() {
		return Collections.unmodifiableList(CraftHandler.CraftHandlers);
	}

	public static void destroy() {
		CraftHandler.CraftHandlers.stream().filter(CraftHandler -> CraftHandler.isEnabled()).forEach(CraftHandler::disable);
		CraftHandler.CraftHandlers.clear();
	}

	protected final Orebfuscator plugin;
	private boolean enabled = false;

	public CraftHandler(Orebfuscator plugin) {
		this.plugin = plugin;

		CraftHandler.CraftHandlers.add(this);
	}

	public void init() {
		Bukkit.getConsoleSender().sendMessage("Initialize Handler §7\"§2" + this.getClass().getSimpleName() + "§7\"§8.");
		try {
			this.onInit();
		} catch(Exception e) {
			e.printStackTrace();
			Bukkit.getConsoleSender().sendMessage("§cError by initialize Handler §7\"§c" + this.getClass().getSimpleName() + "§7\"§8.");
			return;
		}
		Bukkit.getConsoleSender().sendMessage("Initialized Handler §7\"§2" + this.getClass().getSimpleName() + "§7\"§8.");
	}

	public void enable() {
		if (!this.enableHandler()) {
			return;
		}

		Bukkit.getConsoleSender().sendMessage("Enable Handler §7\"§2" + this.getClass().getSimpleName() + "§7\"§8.");
		try {
			this.onEnable();
			this.enabled = true;
		} catch(Exception e) {
			e.printStackTrace();
			Bukkit.getConsoleSender().sendMessage("§cError by enable Handler §7\"§c" + this.getClass().getSimpleName() + "§7\"§8.");
			return;
		}
		Bukkit.getConsoleSender().sendMessage("Enabled Handler §7\"§2" + this.getClass().getSimpleName() + "§7\"§8.");
	}

	public void disable() {
		Bukkit.getConsoleSender().sendMessage("Disable Handler §7\"§2" + this.getClass().getSimpleName() + "§7\"§8.");
		try {
			this.enabled = false;
			this.onDisable();
		} catch(Exception e) {
			e.printStackTrace();
			Bukkit.getConsoleSender().sendMessage("§cError by disable Handler §7\"§c" + this.getClass().getSimpleName() + "§7\"§8.");
			return;
		}
		Bukkit.getConsoleSender().sendMessage("Disabled Handler §7\"§2" + this.getClass().getSimpleName() + "§7\"§8.");
	}

	public void reload() {
		if (this.enableHandler()) {
			if (this.enabled) {
				this.disable();
				this.enable();
			} else {
				this.enable();
			}
		} else if (this.enabled) {
			this.disable();
		}
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public Orebfuscator getPlugin() {
		return this.plugin;
	}
}