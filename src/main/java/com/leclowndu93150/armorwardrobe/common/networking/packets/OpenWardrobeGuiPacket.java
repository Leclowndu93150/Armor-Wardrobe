package com.leclowndu93150.armorwardrobe.common.networking.packets;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import com.leclowndu93150.armorwardrobe.common.container.WardrobeContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenWardrobeGuiPacket {

    // No fields needed for this packet

    public OpenWardrobeGuiPacket() {
    }

    public static void encode(OpenWardrobeGuiPacket msg, FriendlyByteBuf buf) {
        // Nothing to encode
    }

    public static OpenWardrobeGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenWardrobeGuiPacket();
    }

    public static void handle(OpenWardrobeGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Find the wardrobe item in player's inventory or curios slots
            ItemStack wardrobeStack = WardrobeContainer.findPlayerWardrobeItem(player);

            if (!wardrobeStack.isEmpty()) {
                NetworkHooks.openScreen(
                        player,
                        new SimpleMenuProvider(
                                (windowId, playerInventory, playerEntity) -> new WardrobeContainer(windowId, playerInventory, wardrobeStack),
                                Component.translatable("container." + ArmorWardrobe.MOD_ID + ".wardrobe")
                        ),
                        buf -> buf.writeItem(wardrobeStack)
                );
            }
        });

        ctx.get().setPacketHandled(true);
    }
}