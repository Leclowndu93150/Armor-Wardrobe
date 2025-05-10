package com.leclowndu93150.armorwardrobe.client.screen;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import com.leclowndu93150.armorwardrobe.common.container.WardrobeContainer;
import com.leclowndu93150.armorwardrobe.common.networking.PacketHandler;
import com.leclowndu93150.armorwardrobe.common.networking.packets.CycleArmorPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WardrobeScreen extends AbstractContainerScreen<WardrobeContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ArmorWardrobe.MOD_ID, "textures/gui/wardrobe.png");
    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(ArmorWardrobe.MOD_ID, "textures/gui/swap_button.png");

    public WardrobeScreen(WardrobeContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageWidth = 196;
        this.imageHeight = 200;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw the main GUI background
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, 96);
        guiGraphics.blit(TEXTURE, x, y + 96, 0, 96, this.imageWidth, this.imageHeight - 96);

        int setSpacing = 64;
        int startX = 18;

        // Draw button textures below each set
        for (int setIndex = 0; setIndex < 3; setIndex++) {
            int slotX = x + startX + setIndex * setSpacing;
            int buttonX = slotX - 16; // Center the 48px button with the slot (18px)
            int buttonY = y + 94; // Position right after the boots slot

            boolean isHovering = mouseX >= buttonX && mouseX < buttonX + 48 &&
                    mouseY >= buttonY && mouseY < buttonY + 16;

            // Explicitly set the button texture before drawing it
            RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw the button from the texture atlas
            // Using the correct UV coordinates for the texture
            // Normal button is at y=0, hovered button is at y=16
            int vOffset = isHovering ? 16 : 0;

            // Use the GuiGraphics blit method to draw from the texture
            // Parameters: texture, x, y, u, v, width, height
            guiGraphics.blit(BUTTON_TEXTURE, buttonX, buttonY, 0, vOffset, 48, 16);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Only render the player inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Only handle left-clicks for buttons
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;

            int setSpacing = 64;
            int startX = 18;

            for (int setIndex = 0; setIndex < 3; setIndex++) {
                int slotX = x + startX + setIndex * setSpacing;
                int buttonX = slotX - 16; // Same as in renderBg
                int buttonY = y + 94;
                int buttonWidth = 48;
                int buttonHeight = 16;

                if (mouseX >= buttonX && mouseX < buttonX + buttonWidth &&
                        mouseY >= buttonY && mouseY < buttonY + buttonHeight) {

                    PacketHandler.sendToServer(new CycleArmorPacket(setIndex));
                    // Play button click sound
                    this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}