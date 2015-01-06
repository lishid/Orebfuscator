package com.lishid.orebfuscator.utils;

import java.io.File;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.utils.Updater.UpdateResult;

public class UpdateManager {
    public Updater updater;

    public void Initialize(Orebfuscator plugin, File file) {
        updater = new Updater(plugin, 32408, file);

        // Create task to update
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                // Check for updates
                if (OrebfuscatorConfig.CheckForUpdates) {
                    UpdateResult result = updater.update();
                    if (result != UpdateResult.NO_UPDATE) {
                        if (result == UpdateResult.SUCCESS) {
                            Orebfuscator.log("Update found! Downloaded new version.");
                            Orebfuscator.log("This behaviour can be disabled in the config.yml");
                        }
                        else {
                            Orebfuscator.log("Update failed, reason: " + result.toString());
                        }
                    }
                }
            }
        }, 0, 20 * 60 * 1000); // Update every once a while
    }
}
