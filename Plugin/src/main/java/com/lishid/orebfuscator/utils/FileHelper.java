/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileHelper {
    public static String readFile(File file) throws IOException {
    	if(!file.exists()) return null;
    	
    	StringBuilder text = new StringBuilder("");
    	
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		try {
    		String line;
    		
            while ((line = reader.readLine()) != null) { 
            	text.append(line);
            	text.append("\n");
            }
		}
		finally {
			reader.close();
		}
		
		return text.toString();
    }
    
    public static void delete(File file) {
    	if (file.isDirectory()) {
    		for (File child : file.listFiles())
    			delete(child);
    	}
    	
		file.delete();
    }
}
