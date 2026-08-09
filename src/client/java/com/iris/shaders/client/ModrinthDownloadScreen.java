package com.iris.shaders.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

public class ModrinthDownloadScreen extends Screen {
    private final Screen parent;
    private EditBox searchBox;
    
    public ModrinthDownloadScreen(Screen parent) {
        super(Component.literal("Download Shader (Modrinth)"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        
        int boxWidth = 200;
        int boxHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        this.searchBox = new EditBox(this.font, centerX - boxWidth / 2, centerY - 20, boxWidth, boxHeight, Component.literal("Search..."));
        this.addRenderableWidget(this.searchBox);
        
        this.addRenderableWidget(Button.builder(Component.literal("Search & Download"), button -> {
            String query = this.searchBox.getValue();
            if (!query.isEmpty()) {
                Minecraft.getInstance().setScreen(this.parent);
                new Thread(() -> {
                    IrisShadersDownloadClient.searchAndDownloadShader(query);
                }).start();
            }
        }).bounds(centerX - boxWidth / 2, centerY + 10, boxWidth, 20).build());
        
        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(centerX - boxWidth / 2, centerY + 35, boxWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xCC000000); // Dark semi-transparent background
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
