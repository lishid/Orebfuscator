package com.lishid.orebfuscator.hithack;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlayerBlockTracking
{
    private Block block;
    private int hackingIndicator;
    private Player player;
    private long lastTime = System.currentTimeMillis();
    
    public PlayerBlockTracking(Player player)
    {
        this.player = player;
    }
    
    public Player getPlayer()
    {
        return this.player;
    }
    
    public int getHackingIndicator()
    {
        return hackingIndicator;
    }
    
    public Block getBlock()
    {
        return block;
    }
    
    public boolean isBlock(Block block)
    {
        if(block == null || this.block == null)
            return false;
        return block.equals(this.block);
    }
    
    public void setBlock(Block block)
    {
        this.block = block;
    }

    public void incrementHackingIndicator(int value)
    {
        hackingIndicator += value;
    }
    
    public void incrementHackingIndicator()
    {
        incrementHackingIndicator(1);
    }
    
    public void decrementHackingIndicator(int value)
    {
        hackingIndicator -= value;
        if (hackingIndicator < 0)
            hackingIndicator = 0;
    }
    
    public void updateTime()
    {
        lastTime = System.currentTimeMillis();
    }
    
    public long getTimeDifference()
    {
        return System.currentTimeMillis() - lastTime;
    }
}
