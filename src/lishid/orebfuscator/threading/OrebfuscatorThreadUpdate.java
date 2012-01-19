package lishid.orebfuscator.threading;

import java.util.concurrent.LinkedBlockingDeque;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.utils.Calculations;
import lishid.orebfuscator.utils.OrebfuscatorConfig;

import org.bukkit.block.Block;

public class OrebfuscatorThreadUpdate extends Thread implements Runnable
{
	//Global
	private static final int QUEUE_CAPACITY = 1024 * 10;
	private static final LinkedBlockingDeque<Block> queue = new LinkedBlockingDeque<Block>(QUEUE_CAPACITY);
	private static OrebfuscatorThreadUpdate thread;

	public static void Queue(Block block)
	{
		//Removed: 
		//!OrebfuscatorConfig.Enabled() || 
		
		//Dont do anything if the block is transparent
        if (OrebfuscatorConfig.isTransparent((byte)block.getTypeId()))
        {
        	return;
        }
        
        if(!OrebfuscatorConfig.getUpdateThread())
        {
	        Calculations.UpdateBlocksNearby(block);
	        return;
        }
        
		if(thread == null || thread.isInterrupted())
		{
			thread = new OrebfuscatorThreadUpdate();
			thread.setName("Orebfuscator Update Thread");
			thread.start();
		}
		while(true)
		{
			try {
				//Queue block for later processing
				queue.put(block);
				return;
			}
			catch (Exception e) { Orebfuscator.log(e); }
		}
	}
	
	public void run() {
		while (!this.isInterrupted()) {
			try {
				//Remove the first block from the queue
				Block block = queue.take();
				//Send updates on the block change
		        Calculations.UpdateBlocksNearby(block);
		        
		        //Exit if config changed to not using this thread.
		        if(!OrebfuscatorConfig.getUpdateThread() && queue.size() <= 0)
		        {
		        	thread = null;
		        	return;
		        }
			}
			catch (Exception e) { Orebfuscator.log(e); }
		}
	}
}