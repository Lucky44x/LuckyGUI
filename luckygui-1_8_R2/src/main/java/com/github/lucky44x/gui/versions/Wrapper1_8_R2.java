package com.github.lucky44x.gui.versions;


import com.github.lucky44x.gui.abstraction.VersionWrapper;
import net.minecraft.server.v1_8_R2.*;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R2.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_8_R2 extends VersionWrapper {

    public EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public void sendOpenWindowPacket(Player user, int containerID, String inventoryTitle) {
        toNMS(user)
                .playerConnection
                .sendPacket(new PacketPlayOutOpenWindow(
                        containerID, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
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
        return getNextRealContainerId(player);
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
        return new AnvilContainer(toNMS(player), getNextRealContainerId(player), title);
    }

    private static class AnvilContainer extends ContainerAnvil {
        public AnvilContainer(EntityHuman entityhuman, int containerID, String GUITitle) {
            super(entityhuman.inventory, entityhuman.world, new BlockPosition(0, 0, 0), entityhuman);
        }

        @Override
        public void b(EntityHuman player) {}

        @Override
        public boolean a(EntityHuman player) {
            return true;
        }
    }
}
