package com.lishid.orebfuscator.internal.craftbukkit;

import com.lishid.orebfuscator.hithack.BlockHitManager;

import net.minecraft.server.*;

public class Packet14Orebfuscator extends Packet14BlockDig
{
    @Override
    public void handle(NetHandler handler)
    {
        if (this.e == 1 && handler instanceof NetServerHandler)
        {
            boolean canHit = BlockHitManager.hitBlock(((NetServerHandler) handler).getPlayer(), null);
            if (!canHit)
            {
                return;
            }
        }
        super.handle(handler);
    }
}
