package com.stifflered.treeplanterplus;

import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class TreePlanter {

    private final Plugin plugin;

    public TreePlanter(Plugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> build(Location blockLoc, TreeType type) {
        World world = blockLoc.getWorld();
        BlockState oldState = blockLoc.getBlock().getState();
        blockLoc.getBlock().setType(Material.AIR);

        List<BlockState> capturedBlocks = new ArrayList<>();
        boolean generated = world.generateTree(blockLoc, ThreadLocalRandom.current(),
                type,
                (blockState) -> {
                    capturedBlocks.add(blockState);
                    return false;
                }
        );

        if (!generated) {
            oldState.update(true, false);
            throw new CannotPlantException();
        }

        // Animation
        Deque<BlockState> logs = new ArrayDeque<>();
        Deque<BlockState> leaves = new ArrayDeque<>();
        Deque<BlockState> everythingElse = new ArrayDeque<>();
        for (BlockState state : capturedBlocks) {
            Material material = state.getType();

            if (MaterialSetTag.LOGS.isTagged(material)) {
                logs.add(state);
            } else if (MaterialSetTag.LEAVES.isTagged(material)) {
                leaves.add(state);
            } else {
                everythingElse.add(state);
            }
        }

        return runBuildAnimation(logs, 2, 25)
                .thenCompose((v) -> runBuildAnimation(leaves, 3, 20))
                .thenCompose((v) -> runBuildAnimation(everythingElse, Integer.MAX_VALUE, 0));
    }

    private CompletableFuture<Void> runBuildAnimation(Deque<BlockState> states, int blocksPerTick, int tickDelay) {
        Deque<BlockState> mutableStates = new ArrayDeque<>(states);

        CompletableFuture<Void> future = new CompletableFuture<>();
        new BukkitRunnable() {

            int finishTicks = 0;

            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick; i++) {
                    if (mutableStates.isEmpty()) {
                        this.finishTicks++;
                        if (this.finishTicks >= tickDelay) {
                            this.cancel();
                            future.complete(null);
                        }
                        return;
                    }

                    BlockState log = mutableStates.pop();
                    log.update(true, false);
                    log.getWorld().playEffect(log.getLocation(), Effect.STEP_SOUND, log.getType());
                }
            }
        }.runTaskTimer(this.plugin, 0, 1);

        return future;
    }
}
