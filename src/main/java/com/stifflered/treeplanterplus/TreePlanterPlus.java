package com.stifflered.treeplanterplus;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class TreePlanterPlus extends JavaPlugin implements Listener {

    public static TreePlanterPlus INSTANCE;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();

        INSTANCE = this;
        this.register(new TreePlanterListener(this));
    }

    @Override
    public void onDisable() {
    }

    private void register(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

}
