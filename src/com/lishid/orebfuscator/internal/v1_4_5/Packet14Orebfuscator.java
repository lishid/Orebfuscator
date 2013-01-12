package com.lishid.orebfuscator.internal.v1_4_5;

import com.lishid.orebfuscator.hithack.BlockHitManager;

import net.minecraft.server.v1_4_5.*;

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
