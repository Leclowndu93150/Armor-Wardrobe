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

    private int imageWidth = 196;
    private int imageHeight = 186;

    public WardrobeScreen(WardrobeContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageWidth = 196;
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 94; // Adjust if needed based on texture layout
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        // Player Inventory Label - Position based on standard layout adjusted for custom height
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // Standard offset from bottom
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int setSpacing = 64;
        int startX = 18;

        for (int setIndex = 0; setIndex < 3; setIndex++) {
            int setX = x + startX + setIndex * setSpacing;
            int labelY = y + 6;
            guiGraphics.drawString(
                    this.font,
                    Component.translatable("gui." + ArmorWardrobe.MOD_ID + ".set" + (setIndex + 1)),
                    setX,
                    labelY,
                    0x404040,
                    false
            );

            // Draw button visual representation below the set
            int buttonX = setX - 1; // Align slightly left of slots
            int buttonY = y + 96; // Position below armor slots
            int buttonWidth = 50;
            int buttonHeight = 16; // Slightly smaller button

            // Check if mouse is hovering
            boolean isHovering = mouseX >= buttonX && mouseX < buttonX + buttonWidth &&
                    mouseY >= buttonY && mouseY < buttonY + buttonHeight;

            // Button background color changes on hover
            int mainColor = isHovering ? 0xFFC0C0C0 : 0xFF8B8B8B; // Lighter when hovered
            int frameColor = 0xFF373737;

            guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, frameColor); // Outer frame
            guiGraphics.fill(buttonX + 1, buttonY + 1, buttonX + buttonWidth - 1, buttonY + buttonHeight - 1, mainColor); // Inner color

            // Button text
            Component buttonText = Component.translatable("gui." + ArmorWardrobe.MOD_ID + ".swap");
            int textX = buttonX + (buttonWidth - this.font.width(buttonText)) / 2;
            int textY = buttonY + (buttonHeight - 8) / 2; // Center text vertically
            guiGraphics.drawString(this.font, buttonText, textX, textY, 0xFFFFFF, true); // White text with shadow
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
        // Render title using the calculated position
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        // Render player inventory label using calculated position
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        // Custom set labels are drawn in renderBg
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Only handle left-clicks for buttons
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;

            int setSpacing = 64;
            int startX = 18;

            for (int setIndex = 0; setIndex < 3; setIndex++) {
                int setX = x + startX + setIndex * setSpacing;

                // Use the *exact same coordinates and dimensions* as drawing
                int buttonX = setX - 1;
                int buttonY = y + 96;
                int buttonWidth = 50;
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