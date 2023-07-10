package com.github.lucky44x.gui;


import com.github.lucky44x.gui.abstraction.GUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

/**
 * The GUI-Wrapper for Non-Chest and Non-Anvil Based GUIs
 *
 * @author Nick Balischewski
 */
public abstract class GenericGUI extends GUI {

    private InventoryView view;
    private final InventoryType type;

    /**
     * {@inheritDoc}
     * @param type The Inventory type of the Generic-GUI
     */
    public GenericGUI(Plugin instance, Player user, String title, InventoryType type) {
        super(instance, user, title);
        this.type = type;

        finishInit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void closeLogic(int exitCode) {
        if (user.getOpenInventory() == view) {
            user.closeInventory();
        }

        onClose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void openLogic() {
        view = user.openInventory(inventory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSlotClicked(InventoryClickEvent e) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSlotDragged(InventoryDragEvent e) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onInventoryClosed(InventoryCloseEvent e) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Inventory createInventory() {
        inventory = Bukkit.createInventory(null, type);
        return inventory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected abstract void constructView();

    /**
     * Gets called when the GUI is closing
     */
    protected void onClose() {}
    ;
}
