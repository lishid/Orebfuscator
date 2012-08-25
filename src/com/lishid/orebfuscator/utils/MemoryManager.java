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
    public static int MaxCollectPercent = 70;
    public static int AutoCollectPercent = 80;
    public static long lastCollect = System.currentTimeMillis();
    
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
    }
    
    public static void Collect()
    {
        if (System.currentTimeMillis() > lastCollect + 5000)
        {
            lastCollect = System.currentTimeMillis();
            System.gc();
        }
    }
}
