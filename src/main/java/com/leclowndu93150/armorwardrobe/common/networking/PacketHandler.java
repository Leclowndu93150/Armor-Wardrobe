package com.leclowndu93150.armorwardrobe.common.networking;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import com.leclowndu93150.armorwardrobe.common.networking.packets.CycleArmorPacket;
import com.leclowndu93150.armorwardrobe.common.networking.packets.OpenWardrobeGuiPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ArmorWardrobe.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void init() {
        // Register packets
        INSTANCE.registerMessage(id++, CycleArmorPacket.class,
                CycleArmorPacket::encode,
                CycleArmorPacket::decode,
                CycleArmorPacket::handle);

        INSTANCE.registerMessage(id++, OpenWardrobeGuiPacket.class,
                OpenWardrobeGuiPacket::encode,
                OpenWardrobeGuiPacket::decode,
                OpenWardrobeGuiPacket::handle);
    }

    public static void sendToServer(Object msg) {
        INSTANCE.sendToServer(msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}