package com.stifflered.treeplanterplus;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class TreePlanterListener implements Listener {

    private final TreePlanterPlus plugin;

    public TreePlanterListener(TreePlanterPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTreePlant(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();;
        BlockVector3 blockVector3 = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        ApplicableRegionSet regions = container.get(BukkitAdapter.adapt(event.getPlayer().getWorld())).getApplicableRegions(blockVector3);
        for (ProtectedRegion protectedRegion : regions) {
            if (!protectedRegion.getId().equals("tree_plant_area")) {
                return;
            }
        }

        TreeType type = switch (event.getItemInHand().getType()) {
            case OAK_SAPLING -> TreeType.TREE;
            case BIRCH_SAPLING -> TreeType.BIRCH;
            case SPRUCE_SAPLING -> TreeType.REDWOOD;
            case JUNGLE_SAPLING -> TreeType.JUNGLE;
            case ACACIA_SAPLING -> TreeType.ACACIA;
            case DARK_OAK_SAPLING -> TreeType.DARK_OAK;
            case MANGROVE_PROPAGULE -> TreeType.MANGROVE;
            default -> null;
        };


        if (type != null) {
            TreePlanter planter = new TreePlanter(this.plugin);
            try {
                planter.build(event.getBlockPlaced().getLocation(), type);
            } catch (CannotPlantException exception) {
                event.getPlayer().sendMessage(Component.text("Cannot place a tree here!", NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }

}
