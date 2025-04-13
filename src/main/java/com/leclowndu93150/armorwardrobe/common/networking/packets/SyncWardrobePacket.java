package com.leclowndu93150.armorwardrobe.common.networking.packets;

import com.leclowndu93150.armorwardrobe.common.items.WardrobeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncWardrobePacket {
    private final CompoundTag data;

    public SyncWardrobePacket(CompoundTag data) {
        this.data = data;
    }

    public static void encode(SyncWardrobePacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static SyncWardrobePacket decode(FriendlyByteBuf buf) {
        return new SyncWardrobePacket(buf.readNbt());
    }

    public static void handle(SyncWardrobePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if (player == null) return;

            // This packet is typically used to sync wardrobe data from server to client
            // Implementation depends on how you want to handle data syncing
            // For example, you could update the capability or container data

            // In this implementation, we'll assume the data contains the wardrobe item's slot
            // and the updated NBT data for that item
            if (msg.data.contains("Slot")) {
                int slot = msg.data.getInt("Slot");

                // Find the item in the player's inventory
                if (slot >= 0 && slot < player.getInventory().getContainerSize()) {
                    ItemStack stack = player.getInventory().getItem(slot);

                    // If it's a wardrobe item, update its NBT
                    if (stack.getItem() instanceof WardrobeItem) {
                        CompoundTag itemData = msg.data.getCompound("ItemData");
                        stack.setTag(itemData);
                    }
                }
            }
        });

        ctx.get().setPacketHandled(true);
    }
}