package com.github.lucky44x.gui;


import com.github.lucky44x.gui.abstraction.GUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

/**
 * The GUI-Wrapper for Chest-Based GUIs
 *
 * @author Nick Balischewski
 */
public abstract class ChestGUI extends GUI {
    private InventoryView view;
    private final int size;

    /**
     * {@inheritDoc}
     * @param size Defines the size of the Chest-GUI
     */
    public ChestGUI(Player user, int size, String title, Plugin instance) {
        super(instance, user, title);
        this.size = size;
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
    protected final Inventory createInventory() {
        inventory = Bukkit.createInventory(null, size, title);
        return inventory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void openLogic() {
        view = user.openInventory(inventory);
    }

    /**
     * Gets called when the GUI is closing
     */
    protected void onClose() {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onSlotClicked(InventoryClickEvent e) {
        if (e.getView() != view || e.getClickedInventory() != inventory) return;

        slotClickEvent(e);
    }

    /**
     * Gets called when there is a click event
     * @param e The Inventory Click Event instance
     */
    protected void slotClickEvent(InventoryClickEvent e) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected abstract void constructView();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onInventoryClosed(InventoryCloseEvent e) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSlotDragged(InventoryDragEvent e) {}
}
