package com.leclowndu93150.armorwardrobe.common;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArmorWardrobe.MOD_ID);

    // Register the creative tab
    public static final RegistryObject<CreativeModeTab> ARMOR_WARDROBE_TAB = CREATIVE_MODE_TABS.register("main_tab",
            () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(ArmorWardrobe.WARDROBE_ITEM.get()))
                    .title(net.minecraft.network.chat.Component.translatable("creativetab." + ArmorWardrobe.MOD_ID + ".main_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ArmorWardrobe.WARDROBE_ITEM.get());
                        // Add more items as needed
                    })
                    .build()
    );
}