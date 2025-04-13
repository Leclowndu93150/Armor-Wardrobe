package com.leclowndu93150.armorwardrobe.common.networking.packets;

import com.leclowndu93150.armorwardrobe.common.container.WardrobeContainer;
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
                // If the GUI isn't open, we might need a different approach,
                // but for button clicks, the GUI should be open.
                // Logging this case might be useful.
                // com.leclowndu93150.armorwardrobe.ArmorWardrobe.LOGGER.warn("Player {} tried to cycle armor without Wardrobe GUI open.", player.getName().getString());

                // Optionally, implement the temporary container logic from your original code
                // if you want cycling to work even without the GUI open (e.g., via keybind).
                // For button clicks, relying on the open container is safer.
            }
        });
        ctx.get().setPacketHandled(true);
    }
}