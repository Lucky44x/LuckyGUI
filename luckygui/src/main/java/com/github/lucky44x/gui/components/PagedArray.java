package com.github.lucky44x.gui.components;


import com.github.lucky44x.gui.abstraction.GUI;
import com.github.lucky44x.gui.abstraction.GUIComponent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * A Paged-Array component which can be used to easily display arrays on multiple pages
 * @author Nick Balischewski
 */
public class PagedArray extends GUIComponent {

    private ItemStack[] items;
    private final int startSlot;
    private final int endSlot;
    private final int itemsPerPage;
    private final Method onClick;

    private int page;
    private boolean hasNextPage;
    private boolean hasLastPage;
    private int arrayOffset;
    private int remainingItems;

    /**
     * Returns the current page of the Paged-Array
     * @return the current page of the Paged-Array
     */
    public int getPage() {
        return page;
    }

    /**
     * Returns the current offset within the Array
     * @return the current offset within the Array
     */
    public int getArrayOffset() {
        return arrayOffset;
    }

    /**
     * Returns true if there is a next page (when there are enough elements left to fill another page)
     * @return if there is another page
     */
    public boolean hasNextPage() {
        return hasNextPage;
    }

    /**
     * Returns if there is a previous page (true when page > 0, false when page == 0)
     * @return if there is a previous page
     */
    public boolean hasLastPage() {
        return hasLastPage;
    }

    /**
     * Returns how many elements are "left" in the array (Elements.length - arrayOffset)
     * @return how many elements are left within the array before IndexOutOfBounds
     */
    public int getRemainingItems() {
        return remainingItems;
    }

    /**
     * The Constructor for the Paged-Array Component
     * @param parentGUI The GUI which created the component
     * @param items The ItemStack[] which should be displayed
     * @param startSlot The Start (upper left) Slot of the page (does not have to be first slot in inventory)
     * @param endSlot The End (bottom right) Slot of the page (does not have to be last slot in inventory)
     * @param onClick The Method which should be executed when a slot is clicked
     */
    public PagedArray(GUI parentGUI, ItemStack[] items, int startSlot, int endSlot, Method onClick) {
        super(parentGUI);

        this.onClick = onClick;
        this.items = items;
        this.startSlot = startSlot;
        this.endSlot = endSlot;

        if (endSlot < startSlot)
            throw new IllegalStateException(
                    "End-Slot (" + endSlot + ") can't be bigger than Start-Slot (" + startSlot + ")");

        itemsPerPage = endSlot - (startSlot - 1);

        calculateNewData();
    }

    /**
     * Call to update the contents of the paged-array
     * @param items the new items[]
     */
    public void updateItems(ItemStack[] items) {
        this.items = items;
        calculateNewData();
    }

    /**
     * Cycles to the next page if there is one and updates the GUI-view
     */
    public final void nextPage() {
        if (!hasNextPage) return;

        page++;

        calculateNewData();
        parentGUI.updateView();
    }

    /**
     * Cycles to the previous page if there is one and updates the GUI-view
     */
    public final void previousPage() {
        if (!hasLastPage) return;

        page--;

        calculateNewData();
        parentGUI.updateView();
    }

    private void calculateNewData() {
        arrayOffset = itemsPerPage * page;
        remainingItems = items.length - arrayOffset;

        hasNextPage = true;
        hasLastPage = true;

        if (remainingItems <= itemsPerPage) hasNextPage = false;

        if (page == 0) hasLastPage = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onGUIConstructView() {

        int maxVal = arrayOffset + itemsPerPage;
        if (remainingItems <= itemsPerPage) {
            maxVal = arrayOffset + remainingItems;
        }

        for (int i = arrayOffset; i < maxVal; i++) {
            parentGUI.setItem(startSlot + (i - arrayOffset), items[i]);
        }
    }

    /**
     * {@inheritDoc}
     * @param e The InventoryClickEvent instance of the event
     */
    @Override
    public final void onSlotClicked(InventoryClickEvent e) {
        int clickedSlot = e.getSlot();

        if (clickedSlot < startSlot || clickedSlot > endSlot) return;

        try {
            int index = arrayOffset + (clickedSlot - startSlot);

            if (onClick != null) onClick.invoke(parentGUI, e, index);

        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}
