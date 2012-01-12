package lishid.orebfuscator.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.server.NBTCompressedStreamTools;
import net.minecraft.server.NBTTagCompound;

public class ObfuscatedChunkCache {
	File path;
	public long hash;
	int x;
	int z;
	public byte[] data = new byte[1];
	
	ObfuscatedChunkCache(File file, int x, int z)
	{
		this.x = x;
		this.z = z;
		this.path = file;
		file.mkdirs();
	}

	public ObfuscatedChunkCache Read() {
        try {
        	File file = getFile(x, z);
        	if ((file != null) && (file.exists())) {
	            FileInputStream stream = new FileInputStream(file);
	            NBTTagCompound nbt = NBTCompressedStreamTools.a(stream).getCompound("Chunk");
	            
	            //Load Data
	            x = nbt.getInt("X");
	            z = nbt.getInt("Z");
	            hash = nbt.getLong("Hash");
	            data = nbt.getByteArray("Data");
	            
	            return this;
        	}
        } catch (Exception e) {
            //e.printStackTrace();
        }
	    return null;
	}
	
	public void Write() {
		try {
        	File file = getFile(x, z);
			File tempFile = new File(path, "tmp." + Integer.toString(x) + "." + Integer.toString(z) + ".dat");
			
		    FileOutputStream stream = new FileOutputStream(tempFile);
		    NBTTagCompound nbt1 = new NBTTagCompound();
		    NBTTagCompound nbt2 = new NBTTagCompound();

		    nbt2.setInt("X", x);
		    nbt2.setInt("Z", z);
		    nbt2.setLong("Hash", hash);
		    nbt2.setByteArray("Data", data);
		    
		    nbt1.set("Chunk", nbt2);
		    NBTCompressedStreamTools.a(nbt1, stream);
		    stream.close();

		    if (file.exists()) {
		    	file.delete();
		    }
		    tempFile.renameTo(file);
		} catch (Exception e) {
		    //e.printStackTrace();
		}
	}
	
	private File getFile(int x, int z)
	{
	    String fileName = "cache." + Integer.toString(x) + "." + Integer.toString(z) + ".dat";
	    File file = new File(path, fileName);
	    return file;
	}
}
