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

package com.lishid.orebfuscator.obfuscation;

import org.bukkit.block.Block;

import com.lishid.orebfuscator.OrebfuscatorConfig;

public class BlockDeobfuscator
{
    public static boolean needsUpdate(Block block)
    {
        byte id = (byte) block.getTypeId();
        return !OrebfuscatorConfig.isBlockTransparent(id);
    }
    
    public static void Update(Block block)
    {
        if (!needsUpdate(block))
            return;
        
        BlockUpdate.UpdateBlocksNearby(block);
    }
}