package com.leclowndu93150.armorwardrobe.common.networking.packets;

import com.leclowndu93150.armorwardrobe.common.container.WardrobeContainer;
import com.leclowndu93150.armorwardrobe.common.capabilities.WardrobeCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CycleArmorPacket {
    private final int setIndex;

    public CycleArmorPacket(int setIndex) {
        this.setIndex = setIndex;
    }

    public static void encode(CycleArmorPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.setIndex);
    }

    public static CycleArmorPacket decode(FriendlyByteBuf buf) {
        return new CycleArmorPacket(buf.readInt());
    }

    public static void handle(CycleArmorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            AbstractContainerMenu openContainer = player.containerMenu;

            // Check if the currently open container is the WardrobeContainer
            if (openContainer instanceof WardrobeContainer wardrobeContainer) {
                // Use the existing container instance on the server
                wardrobeContainer.cycleArmor(msg.setIndex);
            } else {
                // Handle keypress when GUI is not open
                // Find wardrobe item and cycle the armor directly
                WardrobeContainer.findPlayerWardrobeItem(player);
                // Directly cycle using capability without needing container
                player.getCapability(WardrobeCapability.CAPABILITY).ifPresent(cap -> {
                    if (cap.r(player, msg.setIndex)) {
                        // Force sync inventory to client after swap
                        player.inventoryMenu.broadcastChanges();
                    }
                });
            }

            // Always sync inventory to the client after armor changes
            player.inventoryMenu.broadcastChanges();

            // Additional sync to all tracking clients if needed
            player.getInventory().setChanged();
        });
        ctx.get().setPacketHandled(true);
    }
}