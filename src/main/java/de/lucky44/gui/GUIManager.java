package de.lucky44.gui;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class GUIManager {

    public static GUIManager instance;
    private Plugin pluginInstance;

    private Map<GUI, Player> guis = new HashMap<>();

    public GUIManager(Plugin pluginInstance){
        this.pluginInstance = pluginInstance;
    }

    public void registerGUI(GUI toRegister, Player p){
        pluginInstance.getServer().getPluginManager().registerEvents(toRegister, pluginInstance);
        guis.put(toRegister, p);
    }

    public void close(GUI toClose){
        guis.remove(toClose);
    }
}
