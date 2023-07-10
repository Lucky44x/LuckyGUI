package com.github.lucky44x.gui.abstraction;


import com.github.lucky44x.luckyutil.config.LangConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * The parent-GUI class
 * - Code for the Anvil-GUI part is basically copied from Wesley Smith's <a href="https://github.com/WesJD/AnvilGUI">Anvil-GUI</a>
 *
 * @author Nick Balischewski
 */
public abstract class GUI {

    /**
     * Gets called when the GUI is supposed to close
     * The exitCode parameter will tell a NMS based GUI whether to send packets or not
     * @param exitCode the Exit-Code of the GUI (ignore if not working with packets)
     */
    protected abstract void closeLogic(int exitCode);

    /**
     * Gets called when the GUI is supposed to open
     */
    protected abstract void openLogic();

    /**
     * Gets called when the GUI receives a click-event
     * @param e The {@link InventoryClickEvent InventoryClickEvent} instance
     */
    protected abstract void onSlotClicked(InventoryClickEvent e);

    /**
     * Gets called when the GUI receives a drag-event
     * @param e The {@link InventoryDragEvent InventoryDragEvent} instance
     */
    protected abstract void onSlotDragged(InventoryDragEvent e);

    /**
     * Gets called when the GUI receives a close-event
     * @param e The {@link InventoryCloseEvent InventoryClickEvent} instance
     */
    protected abstract void onInventoryClosed(InventoryCloseEvent e);

    /**
     * Gets called when the GUI is supposed to create a new {@link Inventory Inventory} instance
     * @return The newly created Inventory-Instance
     */
    protected abstract Inventory createInventory();

    /**
     * Gets called after the GUI has finished initializing and is ready for receiving items
     */
    protected abstract void constructView();

    private final EventListener listener;
    protected final Plugin instance;
    private final Set<Integer> interactableSlots = new HashSet<>();
    private boolean open;
    private boolean initialized;

    private HashMap<String, GUIComponent> components = new HashMap<>();

    /**
     * The {@link Inventory Inventory} instance of the GUI
     */
    protected Inventory inventory;

    /**
     * The {@link Player Player} instance of the player who uses the GUI
     */
    @LangConfig.LangData(langKey = "[USER]", stringMethodNames = "getName")
    protected Player user;

    /**
     * The title of the GUI
     */
    protected String title;

    /**
     * The Constructor of the GUI Super-Class
     * @param instance The Plugin's main instance, (preferably an instance of {@link org.bukkit.plugin.java.JavaPlugin})
     * @param user The Player who will use the GUI
     * @param title The title of the GUI
     */
    public GUI(Plugin instance, Player user, String title) {
        this.instance = instance;
        this.user = user;
        this.title = title;

        listener = new EventListener();
        instance.getServer().getPluginManager().registerEvents(listener, instance);
    }

    /**
     * Call when subclass constructor is finished, to start GUI-Construction
     * (Called from the Constructor of Sub-Classes)
     */
    protected final void finishInit() {
        initialized = true;
        onLoad();
        title = ChatColor.translateAlternateColorCodes('&', title);
        this.inventory = createInventory();
        updateView();
    }

    /**
     * Called at the start of finishInit
     */
    protected abstract void onLoad();

    /**
     * "Redraws" the GUI so no new GUI-Object has to be created
     */
    public final void updateView() {
        // closeWithoutHandlers();

        if (!initialized)
            throw new IllegalStateException("GUI of type " + getClass().getSimpleName()
                    + " has not been initialized... please call finishInit() after your constructor");

        constructView();
        for (GUIComponent component : components.values()) {
            component.onGUIConstructView();
        }
        if (!open) openLogic();

        open = true;
    }

    /**
     * Returns the open state of the GUI
     * @return is the GUI open?
     */
    public final boolean isOpen() {
        return open;
    }

    /**
     * Returns the user of the GUI
     * @return the {@link Player Player} instance of the GUI's user
     */
    public final Player getUser() {
        return user;
    }

    /**
     * Returns the Inventory Instance
     * @return the inventory instance of the GUI
     */
    public final Inventory getInventory() {
        return inventory;
    }

    /**
     * Closes the GUI
     */
    public final void close() {
        close(0);
    }

    /**
     * Sets the intractability of a slot within the GUI's inventory
     * (true => Items can be moved | false => Items are locked)
     * @param slot The slot which should be set
     * @param interactable Should the slot be interactable
     */
    protected void setInteractable(int slot, boolean interactable) {
        if (interactableSlots.contains(slot)) {
            if (!interactable) interactableSlots.remove(slot);
        } else {
            if (interactable) interactableSlots.add(slot);
        }
    }

    /**
     * Sets the item at slot in the GUI
     * @param slot The slot which should be set
     * @param item The item which should be set
     */
    public void setItem(int slot, ItemStack item) {

        if (item == null) {
            item = new ItemStack(Material.AIR);
        }

        inventory.setItem(slot, item);
    }

    /**
     * Return the item which is at the specified slot
     * (returns null if there is no item or the slot was outside the inventory bounds)
     * @param slot The slot which should be returned
     * @return Teh Item which was at the specified slot
     */
    protected ItemStack getItem(int slot) {
        if (slot < 0 || slot > inventory.getSize()) return null;

        return inventory.getItem(slot);
    }

    /**
     * Registers the specified GUIComponent to the GUI-Events
     * @param component the GUIComponent which is supposed to be registered
     */
    protected final void registerComponent(String name, GUIComponent component) {
        this.components.put(name, component);
    }

    /**
     * Returns the registered GUIComponent instance or null if there is none
     * @param name The name under which the component was registered
     * @return The corresponding component instance
     */
    protected final GUIComponent getComponent(String name) {
        return components.getOrDefault(name, null);
    }

    /**
     * Closes the GUI with a specific Exit-Code
     * @param code the exit code
     */
    public final void close(int code) {
        if (!open) return;

        HandlerList.unregisterAll(listener);

        closeLogic(code);
        open = false;
    }

    private void closeWithoutHandlers() {
        if (!open) return;

        closeLogic(1);
        open = false;
    }

    private class EventListener implements Listener {

        @EventHandler
        public void onInventoryClicked(InventoryClickEvent e) {
            if (e.getClickedInventory() == null) {
                return;
            }

            if (!e.getClickedInventory().equals(inventory)) {
                return;
            }

            final Player clicker = (Player) e.getWhoClicked();

            if (clicker != user) {
                return;
            }

            final Inventory clickedInventory = e.getClickedInventory();

            if (clickedInventory != null
                    && clickedInventory.equals(clicker.getInventory())
                    && e.getClick().equals(ClickType.DOUBLE_CLICK)) {
                e.setCancelled(true);
                return;
            }

            final int rawSlot = e.getRawSlot();
            if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                e.setCancelled(true);
            }

            e.setCancelled(!interactableSlots.contains(rawSlot));

            for (GUIComponent component : components.values()) {
                component.onSlotClicked(e);
            }

            onSlotClicked(e);
        }

        @EventHandler
        public void onInventoryDragged(InventoryDragEvent e) {
            if (!e.getInventory().equals(inventory)) return;

            for (int slot : e.getRawSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    e.setCancelled(!interactableSlots.contains(slot));
                }
            }

            onSlotDragged(e);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (open && e.getInventory().equals(inventory)) {
                close(1);
            }
        }
    }
}
