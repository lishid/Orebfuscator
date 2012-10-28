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

import java.lang.reflect.Field;

import com.lishid.orebfuscator.Orebfuscator;

public class ReflectionHelper
{
    public static Object getPrivateField(Object object, String fieldName)
    {
        try
        {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
        return null;
    }
    
    public static void setPrivateField(Object object, String fieldName, Object value)
    {
        try
        {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
    }
    
}
