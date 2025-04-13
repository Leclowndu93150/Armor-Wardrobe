package com.leclowndu93150.armorwardrobe.common.items;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import com.leclowndu93150.armorwardrobe.common.container.WardrobeContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class WardrobeItem extends Item {

    public WardrobeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Debug log to verify this is being called
            System.out.println("Opening wardrobe container with stack: " + stack);

            NetworkHooks.openScreen(
                    (ServerPlayer) player,
                    new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new WardrobeContainer(windowId, playerInventory, stack),
                            Component.translatable("container." + ArmorWardrobe.MOD_ID + ".wardrobe")
                    ),
                    buf -> buf.writeItem(stack) // Make sure to write the ItemStack to the buffer
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}