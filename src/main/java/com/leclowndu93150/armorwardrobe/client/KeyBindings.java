package com.leclowndu93150.armorwardrobe.client;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String KEY_CATEGORY = "key.category." + ArmorWardrobe.MOD_ID;

    public static final KeyMapping OPEN_WARDROBE_GUI = new KeyMapping(
            "key." + ArmorWardrobe.MOD_ID + ".open_wardrobe",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F5, // You can keep one of your existing keys
            KEY_CATEGORY
    );
}