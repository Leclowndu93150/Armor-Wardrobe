package com.leclowndu93150.armorwardrobe.client;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String KEY_CATEGORY = "key.category." + ArmorWardrobe.MOD_ID;

    public static final KeyMapping CYCLE_ARMOR_1 = new KeyMapping(
            "key." + ArmorWardrobe.MOD_ID + ".cycle_set1",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F5,
            KEY_CATEGORY
    );

    public static final KeyMapping CYCLE_ARMOR_2 = new KeyMapping(
            "key." + ArmorWardrobe.MOD_ID + ".cycle_set2",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            KEY_CATEGORY
    );

    public static final KeyMapping CYCLE_ARMOR_3 = new KeyMapping(
            "key." + ArmorWardrobe.MOD_ID + ".cycle_set3",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            KEY_CATEGORY
    );
}