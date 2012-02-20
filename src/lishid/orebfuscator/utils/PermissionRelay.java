/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package lishid.orebfuscator.utils;

import lishid.orebfuscator.Orebfuscator;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PermissionRelay {
    public static PermissionHandler handler;
    
    public static void Setup(PluginManager pm)
    {
        if (handler == null) {
            if (pm.getPlugin("Permissions") != null) {
            	handler = ((Permissions) pm.getPlugin("Permissions")).getHandler();
            } else {
                Orebfuscator.log("Permission system not detected, defaulting to OP");
            }
        }
    }
    
	public static boolean hasPermission(Player player, String permission)
	{
		if (handler == null) {
			return player.isOp() ? true : player.hasPermission(permission);
        }else{
        	return handler.has(player, permission);
        }
	}

}
