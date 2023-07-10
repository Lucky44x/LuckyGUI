package com.github.lucky44x.gui.versions;


import com.github.lucky44x.gui.abstraction.VersionWrapper;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_15_R1 extends VersionWrapper {

    public EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public void sendOpenWindowPacket(Player user, int containerID, String inventoryTitle) {
        toNMS(user)
                .playerConnection
                .sendPacket(
                        new PacketPlayOutOpenWindow(containerID, Containers.ANVIL, new ChatMessage(inventoryTitle)));
    }

    @Override
    public void sendCloseWindowPacket(Player user, int containerID) {
        toNMS(user).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerID));
    }

    @Override
    public int getNextRealContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    @Override
    public int getNextContainerId(Player player, Object container) {
        return ((AnvilContainer) container).getContainerId();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).activeContainer = toNMS(player).defaultContainer;
    }

    @Override
    public void setActiveContainer(Player player, Object container) {
        toNMS(player).activeContainer = (Container) container;
    }

    @Override
    public void setActiveContainerId(Object container, int containerId) {}

    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        ((Container) container).addSlotListener(toNMS(player));
    }

    @Override
    public Inventory getInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(Player player, String title) {
        return new AnvilContainer(player, title);
    }

    private class AnvilContainer extends ContainerAnvil {
        public AnvilContainer(Player player, String guiTitle) {
            super(
                    getNextRealContainerId(player),
                    ((CraftPlayer) player).getHandle().inventory,
                    ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(new ChatMessage(guiTitle));
        }

        @Override
        public void e() {
            super.e();
            this.levelCost.set(0);
        }

        @Override
        public void b(EntityHuman entityhuman) {}

        @Override
        protected void a(EntityHuman entityhuman, World world, IInventory iinventory) {}

        public int getContainerId() {
            return windowId;
        }
    }
}
