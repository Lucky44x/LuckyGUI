package de.lucky44.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class GUIManager implements Listener {

    public static GUIManager instance;
    private Plugin pluginInstance;

    private Map<Player, GUI> guis = new HashMap<>();

    public GUIManager(Plugin pluginInstance){

        if(instance != null)
            return;

        instance = this;

        this.pluginInstance = pluginInstance;
        Bukkit.getServer().getPluginManager().registerEvents(this, pluginInstance);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Player p = (Player)e.getPlayer();
        InventoryView v = e.getView();

        if(guis.containsKey(p)){
            if(guis.get(p).v == v){
                close(p);
            }
        }
    }

    public void registerGUI(GUI toRegister, Player p){

        if(guis.containsKey(p)){
            //close(p);
        }

        guis.put(p, toRegister);
        //Bukkit.getLogger().info("[LuckyGUI] registered GUI");
    }

    public void close(Player p){
        guis.get(p).onClose();
        guis.remove(p);
        //Bukkit.getLogger().info("[LuckyGUI] de-registered GUI");
    }

    @EventHandler
    public void interactHandler(InventoryClickEvent e){
        Player user = (Player)e.getWhoClicked();
        GUI toSend = guis.get(user);
        if(toSend == null)
            return;

        int slot = e.getSlot();
        ItemStack item = e.getClickedInventory().getItem(slot);
        toSend.onClick(slot, item);

        if(!toSend.moveSlots.contains(slot))
            e.setCancelled(true);
    }
}
