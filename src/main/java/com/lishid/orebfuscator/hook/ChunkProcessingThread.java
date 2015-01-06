/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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

package com.lishid.orebfuscator.hook;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.ChunkQueue;
import com.lishid.orebfuscator.internal.Packet56;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.utils.OrebfuscatorAsyncQueue;

public class ChunkProcessingThread extends Thread {
    private static OrebfuscatorAsyncQueue<ChunkProcessingOrder> queue = new OrebfuscatorAsyncQueue<ChunkProcessingThread.ChunkProcessingOrder>();

    private static LinkedList<ChunkProcessingThread> threads = new LinkedList<ChunkProcessingThread>();

    static class ChunkProcessingOrder {
        Packet56 packet;
        Player player;
        ChunkQueue output;

        public ChunkProcessingOrder(Packet56 packet, Player player, ChunkQueue output) {
            this.packet = packet;
            this.player = player;
            this.output = output;
        }
    }

    public synchronized static void KillAll() {
        for (ChunkProcessingThread thread : threads) {
            thread.kill.set(true);
            thread.interrupt();
        }
        threads.clear();
        queue.clear();
    }

    public synchronized static void SyncThreads() {
        // Return as soon as possible
        if (threads.size() == OrebfuscatorConfig.ProcessingThreads) {
            return;
        }

        // Less threads? Kill one
        else if (threads.size() > OrebfuscatorConfig.ProcessingThreads) {
            threads.getLast().kill.set(true);
            threads.getLast().interrupt();
            threads.removeLast();
            return;
        }

        // More threads? Start new one
        else {
            ChunkProcessingThread thread = new ChunkProcessingThread();
            thread.setName("Orebfuscator Processing Thread");
            try {
                thread.setPriority(OrebfuscatorConfig.OrebfuscatorPriority);
            }
            catch (Exception e) {
                thread.setPriority(Thread.MIN_PRIORITY);
            }
            thread.start();
            threads.add(thread);
        }
    }

    public static void Queue(Packet56 packet, Player player, ChunkQueue output) {
        SyncThreads();
        queue.queue(new ChunkProcessingOrder(packet, player, output));
    }

    AtomicBoolean kill = new AtomicBoolean(false);

    @Override
    public void run() {
        while (!Thread.interrupted() && !kill.get()) {
            try {
                ChunkProcessingOrder order = queue.dequeue();
                Calculations.Obfuscate(order.packet, order.player);
                order.output.FinishedProcessing(order.packet);
                Thread.sleep(1);
            }
            catch (InterruptedException e) {
                // If interrupted then exit
            }
            catch (Exception e) {
                Orebfuscator.log(e);
            }
        }
    }
}
