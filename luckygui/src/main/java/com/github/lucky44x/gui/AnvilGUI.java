package com.github.lucky44x.gui;


import com.github.lucky44x.gui.abstraction.GUI;
import com.github.lucky44x.gui.abstraction.VersionWrapper;
import com.github.lucky44x.gui.versions.VersionHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

/**
 * The GUI-Wrapper for creating Anvil-Based GUIs
 * - NMS Code is basically copied from Wesley Smith's <a href="https://github.com/WesJD/AnvilGUI/">Anvil-GUI</a>
 *
 * @author Nick Balischewski
 */
public abstract class AnvilGUI extends GUI {

    private static final VersionWrapper WRAPPER = new VersionHandler().getWrapper();
    private int containerID = -1;
    private Object container;

    /**
     * Creates a new Anvil-Inventory Instance and opens it for the specified user
     * @param user The Player-Instance who will use the GUI
     * @param title The title of the Anvil-GUI
     * @param instance The Main-Plugin-Instance
     */
    public AnvilGUI(Player user, String title, Plugin instance) {
        super(instance, user, title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void closeLogic(int code) {
        if (code == 1) {
            WRAPPER.handleInventoryCloseEvent(user);
            WRAPPER.setActiveContainerDefault(user);
            WRAPPER.sendCloseWindowPacket(user, containerID);
        }

        onClose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void openLogic() {
        containerID = WRAPPER.getNextContainerId(user, container);
        if (containerID == -1) {
            Bukkit.getLogger().severe("Container ID still -1 even after init");
            return;
        }

        WRAPPER.sendOpenWindowPacket(user, containerID, title);
        WRAPPER.setActiveContainer(user, container);
        WRAPPER.setActiveContainerId(container, containerID);
        WRAPPER.addActiveContainerSlotListener(container, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onSlotClicked(InventoryClickEvent e) {
        if (e.getClickedInventory() != inventory) return;

        slotClickedEvent(e);
    }

    /**
     * Gets called when there is a click event
     * @param e The Inventory Click Event instance
     */
    protected void slotClickedEvent(InventoryClickEvent e) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onSlotDragged(InventoryDragEvent e) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onInventoryClosed(InventoryCloseEvent e) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected Inventory createInventory() {
        WRAPPER.handleInventoryCloseEvent(user);
        WRAPPER.setActiveContainerDefault(user);

        container = WRAPPER.newContainerAnvil(user, title);
        inventory = WRAPPER.getInventory(container);
        return inventory;
    }

    /**
     * Gets called when the GUI is closing
     */
    protected abstract void onClose();

    /**
     * {@inheritDoc}
     */
    @Override
    protected abstract void constructView();
}
