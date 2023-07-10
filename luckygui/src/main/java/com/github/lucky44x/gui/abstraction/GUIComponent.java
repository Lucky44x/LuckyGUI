package com.github.lucky44x.gui.abstraction;


import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Super-Class for all Components
 *
 * @author Nick Balischewski
 */
public abstract class GUIComponent {

    /**
     * The GUI which created the component
     */
    protected GUI parentGUI;

    /**
     * The mandatory Constructor for every GUIComponent Sub-Class
     * @param parentGUI The GUI which created this Component Instance
     */
    public GUIComponent(GUI parentGUI) {
        this.parentGUI = parentGUI;
    }

    /**
     * Gets called by the parent-GUI when there was a click
     * @param e The InventoryClickEvent-Instance of the event
     */
    protected void onSlotClicked(InventoryClickEvent e) {}

    /**
     * Gets called by the parent-GUI when the GUI is "constructing" it's User Interface
     */
    protected abstract void onGUIConstructView();
}
