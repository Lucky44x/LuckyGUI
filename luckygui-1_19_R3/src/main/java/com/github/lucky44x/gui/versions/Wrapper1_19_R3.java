package com.github.lucky44x.gui.versions;


import com.github.lucky44x.gui.abstraction.VersionWrapper;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_19_R3 extends VersionWrapper {

    public EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public void sendOpenWindowPacket(Player user, int containerID, String inventoryTitle) {
        toNMS(user).b.a(new PacketPlayOutOpenWindow(containerID, Containers.h, IChatBaseComponent.a(inventoryTitle)));
    }

    @Override
    public void sendCloseWindowPacket(Player user, int containerID) {
        toNMS(user).b.a(new PacketPlayOutCloseWindow(containerID));
    }

    @Override
    public int getNextRealContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    @Override
    public int getNextContainerId(Player player, Object container) {
        return ((AnvilContainer) container).getContainerID();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).bP = toNMS(player).bO;
    }

    @Override
    public void setActiveContainer(Player player, Object container) {
        toNMS(player).bP = (Container) container;
    }

    @Override
    public void setActiveContainerId(Object container, int containerId) {}

    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        toNMS(player).a((Container) container);
    }

    @Override
    public Inventory getInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(Player player, String title) {
        return new AnvilContainer(player, getNextRealContainerId(player), title);
    }

    private static class AnvilContainer extends ContainerAnvil {
        public AnvilContainer(Player user, int containerID, String GUITitle) {
            super(containerID, ((CraftPlayer) user).getHandle().fJ());
            this.checkReachable = false;
            setTitle(IChatBaseComponent.a(GUITitle));
        }

        @Override
        public void m() {
            super.m();
            this.w.a(0);
        }

        @Override
        public void b(EntityHuman player) {}

        @Override
        public void a(EntityHuman player, IInventory container) {}

        public int getContainerID() {
            return this.j;
        }
    }
}
