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

package com.lishid.orebfuscator.utils;

public class MemoryManager
{
    public static int MaxCollectPercent = 80;
    public static int AutoCollectPercent = 90;
    public static int cooldown = 0;
    
    public static void CheckAndCollect()
    {
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        long max = Runtime.getRuntime().maxMemory();
        
        if (((float) used * 100 / total) > AutoCollectPercent)
        {
            Collect();
        }
        else if (((float) used * 100 / max) > MaxCollectPercent)
        {
            Collect();
        }
        else
        {
            cooldown = 0;
        }
    }
    
    public static void Collect()
    {
        if (cooldown <= 0)
        {
            cooldown = 5;
            // Orebfuscator.log("Memory is low, performing optimizations.");
            System.gc();
        }
        else
        {
            cooldown--;
        }
    }
}
