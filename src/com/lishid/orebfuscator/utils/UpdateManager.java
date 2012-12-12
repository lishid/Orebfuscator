package com.lishid.orebfuscator.utils;

import java.io.File;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.utils.Updater.UpdateResult;
import com.lishid.orebfuscator.utils.Updater.UpdateType;

public class UpdateManager
{
    public Updater updater;
    
    public void Initialize(Orebfuscator plugin, File file)
    {
        
        updater = new Updater(plugin, Orebfuscator.logger, "orebfuscator", file);
        
        // Create task to update
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                // Check for updates
                if (OrebfuscatorConfig.getCheckForUpdates())
                {
                    UpdateResult result = updater.update(UpdateType.DEFAULT);
                    if (result != UpdateResult.NO_UPDATE)
                        Orebfuscator.log(result.toString());
                }
            }
        }, 0, 20 * 60 * 1000); // Update every once a while
    }
}
