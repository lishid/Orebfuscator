package com.lishid.orebfuscator.api.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import com.lishid.orebfuscator.api.Handler;

public interface IObfuscatedDataCacheHandler extends Handler {

	public void resetCacheFolder();

	public File getCacheFolder();

	public void closeCacheFiles();

	public void checkCacheAndConfigSynchronized() throws IOException;

	public void clearCache() throws IOException;

	public DataInputStream getInputStream(File folder, int x, int z) throws IOException;

	public DataOutputStream getOutputStream(File folder, int x, int z) throws IOException;

	public int deleteFiles(File folder, int deleteAfterDays);
}