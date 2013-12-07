package com.lishid.orebfuscator.internal.v1_5_R2;

import com.lishid.orebfuscator.hithack.BlockHitManager;

import net.minecraft.server.v1_5_R2.*;

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
