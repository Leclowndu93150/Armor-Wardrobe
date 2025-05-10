package com.leclowndu93150.armorwardrobe.client;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import com.leclowndu93150.armorwardrobe.client.screen.WardrobeScreen;
import com.leclowndu93150.armorwardrobe.common.container.WardrobeContainer;
import com.leclowndu93150.armorwardrobe.common.networking.PacketHandler;
import com.leclowndu93150.armorwardrobe.common.networking.packets.OpenWardrobeGuiPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = ArmorWardrobe.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    public static void init() {
        // Register screen factory with proper type parameters
        MenuScreens.register(
                (MenuType<? extends WardrobeContainer>) ArmorWardrobe.WARDROBE_CONTAINER.get(),
                WardrobeScreen::new
        );
    }

    @Mod.EventBusSubscriber(modid = ArmorWardrobe.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBindings.OPEN_WARDROBE_GUI);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(ClientEvents::init);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // First check if our keybind was pressed, to avoid unnecessary processing
        if (!KeyBindings.OPEN_WARDROBE_GUI.consumeClick()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) return;

        // Only check Curios if our keybind was actually pressed
        AtomicBoolean hasWardrobeInCurios = new AtomicBoolean(false);

        try {
            CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.getItem() == ArmorWardrobe.WARDROBE_ITEM.get()) {
                        hasWardrobeInCurios.set(true);
                        break;
                    }
                }
            });
        } catch (Exception e) {
            // Curios might not be installed
        }

        if (hasWardrobeInCurios.get()) {
            // Send packet to open GUI
            PacketHandler.sendToServer(new OpenWardrobeGuiPacket());
        }
    }
}