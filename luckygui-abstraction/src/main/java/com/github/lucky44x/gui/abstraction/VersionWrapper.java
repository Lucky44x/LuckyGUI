package com.github.lucky44x.gui.abstraction;


import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * The Version Wrapper for enabling multi-version NMS-support
 *  - Code is basically just copied from Wesley Smith's <a href="https://github.com/WesJD/AnvilGUI/blob/master/abstraction/src/main/java/net/wesjd/anvilgui/version/VersionWrapper.java">Anvil-GUI</a>
 *
 * @author Nick Balischewski
 * @author Wesley Smith
 */
public abstract class VersionWrapper {

    public abstract void sendOpenWindowPacket(Player user, int containerID, String inventoryTitle);

    public abstract void sendCloseWindowPacket(Player user, int containerID);

    public abstract int getNextRealContainerId(Player player);

    public abstract int getNextContainerId(Player player, Object container);

    public abstract void handleInventoryCloseEvent(Player player);

    public abstract void setActiveContainerDefault(Player player);

    public abstract void setActiveContainer(Player player, Object container);

    public abstract void setActiveContainerId(Object container, int containerId);

    public abstract void addActiveContainerSlotListener(Object container, Player player);

    public abstract Inventory getInventory(Object container);

    public abstract Object newContainerAnvil(Player player, String title);
}
