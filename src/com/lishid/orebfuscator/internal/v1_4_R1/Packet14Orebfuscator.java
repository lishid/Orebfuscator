package com.lishid.orebfuscator.internal.v1_4_R1;

import com.lishid.orebfuscator.hithack.BlockHitManager;

import net.minecraft.server.v1_4_R1.*;

public class Packet14Orebfuscator extends Packet14BlockDig {
    @Override
    public void handle(Connection handler) {
        if (this.e == 1 && handler instanceof PlayerConnection) {
            boolean canHit = BlockHitManager.hitBlock(((PlayerConnection) handler).getPlayer(), null);
            if (!canHit) {
                return;
            }
        }
        super.handle(handler);
    }
}
