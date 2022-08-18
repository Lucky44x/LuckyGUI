import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public abstract class GUI implements Listener {

    //Internal vars
    private Player user;
    private Inventory inv;

    //Styling Vars
    private String name;
    private int size;

    //region overridable methods
    public abstract void onOpen(Player user);
    public abstract void onClose();
    public abstract void onClick(int slot);
    //endregion

    //region styling methods

    public void setName(String name){
        this.name = name;
    }

    public void setSize(int size){
        this.size = size;
    }

    public void construct(){
        inv = Bukkit.createInventory(null, size, name);
    }

    public void fill(ItemStack backgroundItem){
        if(inv == null)
            return;

        ItemStack[] fill = new ItemStack[size];
        Arrays.fill(fill, backgroundItem);
        inv.setContents(fill);
    }

    public void set(ItemStack item, int slot){
        if(inv == null)
            return;

        inv.setItem(slot, item);
    }
    //endregion

    //region public methods

    public void open(Player user){
        this.user = user;
        onOpen(user);
        GUIManager.instance.registerGUI(this, user);
        user.openInventory(inv);
    }

    public void close(){
        onClose();
        GUIManager.instance.close(this);
    }

    //endregion

    //region Events
    @EventHandler
    public void onSlotClickEvent(InventoryClickEvent e){
        Player localUser = (Player)e.getWhoClicked();

        if(localUser != user)
            return;

        int slot = e.getSlot();
        onClick(slot);
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent e){
        if(((Player)e.getPlayer()) == user){
            close();
        }
    }
    //endregion
}
