package com.github.lucky44x.gui.versions;


import com.github.lucky44x.gui.abstraction.VersionWrapper;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_17_R1 extends VersionWrapper {

    public EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public void sendOpenWindowPacket(Player user, int containerID, String inventoryTitle) {
        toNMS(user)
                .b
                .sendPacket(
                        new PacketPlayOutOpenWindow(containerID, Containers.h, new ChatComponentText(inventoryTitle)));
    }

    @Override
    public void sendCloseWindowPacket(Player user, int containerID) {
        toNMS(user).b.sendPacket(new PacketPlayOutCloseWindow(containerID));
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
        (toNMS(player)).bV = (Container) (toNMS(player)).bU;
    }

    @Override
    public void setActiveContainer(Player player, Object container) {
        (toNMS(player)).bV = (Container) container;
    }

    @Override
    public void setActiveContainerId(Object container, int containerId) {}

    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        toNMS(player).initMenu((Container) container);
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
                    Wrapper1_17_R1.this.getNextRealContainerId(player),
                    ((CraftPlayer) player).getHandle().getInventory(),
                    ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(new ChatMessage(guiTitle));
        }

        @Override
        public void i() {
            super.i();
            this.w.set(0);
        }

        @Override
        public void b(EntityHuman player) {}

        @Override
        protected void a(EntityHuman player, IInventory container) {}

        public int getContainerId() {
            return this.j;
        }
    }
}
