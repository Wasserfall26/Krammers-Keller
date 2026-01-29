package com.Wasserfall_26.KK;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.Desktop;
import java.net.URI;

public class GuiModSettings extends GuiScreen {

    private static final String DEBUG_PASSWORD = "key";
    private GuiTextField debugPasswordField;
    private boolean showDebugPasswordField = false;
    private int passwordAttempts = 0;
    private long lastAttemptTime = 0;

    private EmotesPanel emotesPanel;


    private static final String DISCORD_LINK = "https://discord.com/invite/eN7bhSwp5v";
    private static final String GITHUB_LINK = "https://github.com/Wasserfall26/Krammers-Keller";
    private static final String YOUTUBE_LINK = "https://www.youtube.com/@KrammersKeller";
    private static final String EMAIL = "krammerskeller@gmail.com";

    private enum Category {
        GENERAL("General", 0),
        DUNGEONS("Dungeons", 1),
        EXTRAS("Extras", 2),
        EMOTES("Emotes", 3),
        SOCIALS("Socials", 4);

        private final String name;
        private final int id;

        Category(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }

    private Category selectedCategory = Category.GENERAL;
    private List<SettingButton> settingButtons = new ArrayList<>();

    @Override
    public void initGui() {
        this.buttonList.clear();
        settingButtons.clear();

        int sidebarWidth = 420;
        int headerHeight = 135;
        int buttonHeight = 90;
        int buttonSpacing = 105;

        int startY = headerHeight + 22;
        for (Category cat : Category.values()) {
            GuiButton btn = new CategoryButton(cat.id, 30, startY,
                    sidebarWidth - 60, buttonHeight, cat.name, cat);
            this.buttonList.add(btn);
            startY += buttonSpacing;
        }

        debugPasswordField = new GuiTextField(999, this.fontRendererObj,
                this.width / 2 - 100, this.height / 2 - 10, 200, 20);
        debugPasswordField.setMaxStringLength(50);
        debugPasswordField.setFocused(false);


        if (emotesPanel == null) {
            emotesPanel = new EmotesPanel();
        }

        loadSettingsForCategory();
    }

    @Override
    public void onGuiClosed() {
        KK.config.saveConfig();
    }

    @Override
    public void setWorldAndResolution(net.minecraft.client.Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        if (!settingButtons.isEmpty()) {
            loadSettingsForCategory();
        }
    }

    private void loadSettingsForCategory() {
        settingButtons.clear();


        if (selectedCategory == Category.EMOTES && emotesPanel != null) {
            emotesPanel.reset();
        }

        int sidebarWidth = 420;
        int headerHeight = 135;
        int settingHeight = 70;
        int settingSpacing = 80;

        int contentStartX = sidebarWidth + 30;
        int contentWidth = 800;
        int yPos = headerHeight + 82;

        switch (selectedCategory) {
            case GENERAL:
                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Toggle All Features",
                        () -> ModConfig.allFeaturesEnabled,
                        (val) -> {
                            ModConfig.toggleAllFeatures(val);
                            loadSettingsForCategory();
                        },
                        "Master toggle - Disables all features when off, restores previous states when on"));
                yPos += settingSpacing;

                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Chat Switcher",
                        () -> ModConfig.chatSwitcherEnabled,
                        (val) -> ModConfig.chatSwitcherEnabled = val,
                        "Allows switching between All- Party- and Guild-Chat"));
                yPos += settingSpacing;

                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Custom Cape",
                        () -> ModConfig.capeEnabled,
                        (val) -> ModConfig.capeEnabled = val,
                        "Renders a Custom Cape for all mod users"));
                yPos += settingSpacing;

                settingButtons.add(new PasswordToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Debug Mode",
                        () -> ModConfig.debugMode,
                        (val) -> {
                            if (val && !ModConfig.debugMode) {
                                showDebugPasswordField = true;
                            } else {
                                ModConfig.debugMode = false;
                            }
                        },
                        "Shows Debug Messages"));
                break;

            case DUNGEONS:
                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Auto Wish",
                        () -> ModConfig.autoWishEnabled,
                        (val) -> ModConfig.autoWishEnabled = val,
                        "Wishes when reading 'Maxor is enraged', 'Core Entrance opening' or 'wish' in chat"));
                yPos += settingSpacing;

                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Low HP Cry",
                        () -> ModConfig.autoWishOnLowHpEnabled,
                        (val) -> ModConfig.autoWishOnLowHpEnabled = val,
                        "Sends '/pc wish' when low HP"));
                yPos += settingSpacing;

                settingButtons.add(new SliderSetting(contentStartX, yPos, contentWidth, settingHeight, "Low HP Percentage",
                        () -> ModConfig.lowHpThreshold,
                        (val) -> ModConfig.lowHpThreshold = val,
                        1.0f, 99.0f,
                        "HP percent for Cry Message (currently: %.0f%%)"));
                yPos += settingSpacing;

                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Blood Room Progress",
                        () -> ModConfig.bloodRoomProgressEnabled,
                        (val) -> ModConfig.bloodRoomProgressEnabled = val,
                        "Calculates remaining Blood mobs"));
                yPos += settingSpacing;

                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Wither Key Tracer",
                        () -> ModConfig.witherKeyTracerEnabled,
                        (val) -> ModConfig.witherKeyTracerEnabled = val,
                        "Shows lines to wither keys in dungeons"));
                break;

            case EXTRAS:
                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Message Triggers",
                        () -> ModConfig.chatTriggersEnabled,
                        (val) -> ModConfig.chatTriggersEnabled = val,
                        "Activates Sending Messages for specific Triggers"));
                yPos += settingSpacing;

                settingButtons.add(new ButtonSetting(contentStartX, yPos, contentWidth, settingHeight,
                        "Configure Triggers",
                        "Opens Menu for adding or removing certain Message Triggers"));
                yPos += settingSpacing;

                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Button & Lever Helper",
                        () -> ModConfig.buttonHelperEnabled,
                        (val) -> ModConfig.buttonHelperEnabled = val,
                        "HANDLE WITH CARE this might be considered a CHEAT!!! Makes Button and Lever hitboxes as big as the Block they are placed on"));
                yPos += settingSpacing;

                settingButtons.add(new ToggleSetting(contentStartX, yPos, contentWidth, settingHeight, "Starred Mob ESP",
                        () -> ModConfig.starredMobEspEnabled,
                        (val) -> ModConfig.starredMobEspEnabled = val,
                        "Lets you see starred mobs through walls"));
                yPos += settingSpacing;

                settingButtons.add(new ColorSliderSetting(contentStartX, yPos, contentWidth, settingHeight, "ESP Box Color",
                        () -> ModConfig.espColorHue,
                        (val) -> ModConfig.espColorHue = val,
                        0.0f, 300.0f,
                        "Color of the ESP boxes (Red → Yellow → Green → Cyan → Blue → Purple)"));

                break;

            case EMOTES:
                break;

            case SOCIALS:
                settingButtons.add(new SocialLinkButton(contentStartX, yPos, contentWidth, settingHeight,
                        "§9Discord Server",
                        "Join my Discord Server!",
                        DISCORD_LINK,
                        0xFF5865F2));
                yPos += settingSpacing;


                settingButtons.add(new SocialLinkButton(contentStartX, yPos, contentWidth, settingHeight,
                        "§aGitHub Repository",
                        "You can find updated versions and Source Code here",
                        GITHUB_LINK,
                        0xFF00aa00));
                yPos += settingSpacing;


                settingButtons.add(new SocialLinkButton(contentStartX, yPos, contentWidth, settingHeight,
                        "§cYouTube Channel",
                        "Showcase Video I guess?!?",
                        YOUTUBE_LINK,
                        0xFFFF0000));
                yPos += settingSpacing;


                settingButtons.add(new SocialLinkButton(contentStartX, yPos, contentWidth, settingHeight,
                        "§eContact Email",
                        "Send me an Email with Suggestions or Fixes, greatly appreciated :)",
                        EMAIL,
                        0xFFFFaa00));
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof CategoryButton) {
            CategoryButton catBtn = (CategoryButton) button;
            selectedCategory = catBtn.category;
            loadSettingsForCategory();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (selectedCategory == Category.EMOTES && emotesPanel != null) {
            int wheel = org.lwjgl.input.Mouse.getEventDWheel();
            if (wheel != 0) {
                emotesPanel.handleScroll(wheel);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (showDebugPasswordField) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                showDebugPasswordField = false;
                debugPasswordField.setText("");
                return;
            }

            if (keyCode == Keyboard.KEY_RETURN) {
                checkDebugPassword();
                return;
            }

            debugPasswordField.textboxKeyTyped(typedChar, keyCode);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (showDebugPasswordField) {
            debugPasswordField.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            if (selectedCategory == Category.EMOTES && emotesPanel != null) {
                int sidebarWidth = 420;
                int headerHeight = 135;
                int contentStartX = sidebarWidth + 30;
                int contentWidth = 800;
                int startY = headerHeight + 82;

                emotesPanel.handleClick(mouseX, mouseY, contentStartX, startY, contentWidth);
            } else {
                for (SettingButton setting : settingButtons) {
                    if (setting.isHovered(mouseX, mouseY)) {
                        setting.onClick();
                        break;
                    }
                }
            }
        }
    }

    private void checkDebugPassword() {
        String input = debugPasswordField.getText().trim();

        if (input.equals(DEBUG_PASSWORD)) {
            ModConfig.debugMode = true;
            showDebugPasswordField = false;
            debugPasswordField.setText("");
            passwordAttempts = 0;

            net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new net.minecraft.util.ChatComponentText("§a§l✓ Debug Mode active!")
            );
        } else {
            passwordAttempts++;
            lastAttemptTime = System.currentTimeMillis();
            debugPasswordField.setText("");

            if (passwordAttempts >= 3) {
                showDebugPasswordField = false;
                passwordAttempts = 0;

            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int sidebarWidth = 420;
        int headerHeight = 135;

        drawRect(0, 0, this.width, this.height, 0xC0000000);
        drawRect(0, 0, this.width, headerHeight - 6, 0xFF0a0a0a);
        drawRect(0, headerHeight - 6, this.width, headerHeight, 0xFFFFaa00);

        GlStateManager.pushMatrix();
        GlStateManager.scale(9.0f, 9.0f, 9.0f);
        this.drawCenteredString(this.fontRendererObj, "§6§lKRAMMERS KELLER",
                (int)(this.width / 18.0f), 2, 0xFFFFFFFF);
        GlStateManager.popMatrix();

        drawRect(0, headerHeight, sidebarWidth, this.height, 0xF0101010);
        drawRect(sidebarWidth, headerHeight, this.width, this.height, 0xF0151515);
        drawRect(sidebarWidth, headerHeight, sidebarWidth + 3, this.height, 0xFF202020);

        GlStateManager.pushMatrix();
        GlStateManager.scale(3.75f, 3.75f, 3.75f);
        this.drawString(this.fontRendererObj, "§e§l" + selectedCategory.name,
                (int)((sidebarWidth + 30) / 3.75f), (int)((headerHeight + 15) / 3.75f), 0xFFFFaa00);
        GlStateManager.popMatrix();

        drawRect(sidebarWidth + 30, headerHeight + 63,
                this.width - 30, headerHeight + 67, 0xFF303030);


        if (selectedCategory == Category.EMOTES && emotesPanel != null) {
            int contentStartX = sidebarWidth + 30;
            int contentWidth = 800;
            emotesPanel.draw(this, mouseX, mouseY, contentStartX, headerHeight, contentWidth, this.width, this.height);
        } else {
            for (SettingButton setting : settingButtons) {
                setting.draw(this, mouseX, mouseY);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (!showDebugPasswordField && selectedCategory != Category.EMOTES) {
            for (SettingButton setting : settingButtons) {
                if (setting.isHovered(mouseX, mouseY)) {
                    drawHoveringText(setting.getTooltip(), mouseX, mouseY);
                    break;
                }
            }
        }

        if (showDebugPasswordField) {
            drawRect(0, 0, this.width, this.height, 0xD0000000);

            int boxWidth = 400;
            int boxHeight = 150;
            int boxX = this.width / 2 - boxWidth / 2;
            int boxY = this.height / 2 - boxHeight / 2;

            drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF1a1a1a);
            drawRect(boxX, boxY, boxX + boxWidth, boxY + 3, 0xFFFFaa00);

            GlStateManager.pushMatrix();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            this.drawCenteredString(this.fontRendererObj, "§6§lDebug Mode",
                    (int)((this.width / 2) / 2.0f), (int)((boxY + 20) / 2.0f), 0xFFFFFFFF);
            GlStateManager.popMatrix();

            this.drawCenteredString(this.fontRendererObj, "§7Enter Password to activate Debug Mode:",
                    this.width / 2, boxY + 55, 0xFFFFFFFF);

            debugPasswordField.xPosition = this.width / 2 - 100;
            debugPasswordField.yPosition = boxY + 75;
            debugPasswordField.drawTextBox();

            String hint = passwordAttempts > 0 ?
                    "§cWrong Password! (" + passwordAttempts + "/3)" :
                    "§7Press Enter to continue or ESC to cancel";
            this.drawCenteredString(this.fontRendererObj, hint,
                    this.width / 2, boxY + 110, 0xFFFFFFFF);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        for (SettingButton setting : settingButtons) {
            if (setting instanceof SliderSetting) {
                ((SliderSetting) setting).dragging = false;
            }
            if (setting instanceof ColorSliderSetting) {
                ((ColorSliderSetting) setting).dragging = false;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private class CategoryButton extends GuiButton {
        private Category category;

        public CategoryButton(int id, int x, int y, int width, int height, String text, Category category) {
            super(id, x, y, width, height, text);
            this.category = category;
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                        mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                boolean selected = selectedCategory == this.category;

                int color = selected ? 0xFF1a1a1a : (hovered ? 0xFF181818 : 0xFF101010);
                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width,
                        this.yPosition + this.height, color);

                if (selected) {
                    drawRect(this.xPosition, this.yPosition, this.xPosition + 2,
                            this.yPosition + this.height, 0xFFFFaa00);
                }

                int textColor = selected ? 0xFFFFaa00 : (hovered ? 0xFFCCCCCC : 0xFF888888);
                String displayText = selected ? "§l" + this.displayString : this.displayString;
                GlStateManager.pushMatrix();
                GlStateManager.scale(3.3f, 3.3f, 3.3f);
                drawString(mc.fontRendererObj, displayText,
                        (int)((this.xPosition + 22) / 3.3f), (int)((this.yPosition + 28) / 3.3f), textColor);
                GlStateManager.popMatrix();
            }
        }
    }

    private interface SettingButton {
        void draw(GuiScreen screen, int mouseX, int mouseY);
        boolean isHovered(int mouseX, int mouseY);
        void onClick();
        List<String> getTooltip();
    }

    private class SocialLinkButton implements SettingButton {
        private int x, y, width, height;
        private String name, description, link;
        private int accentColor;

        public SocialLinkButton(int x, int y, int width, int height, String name, String description, String link, int accentColor) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.name = name;
            this.description = description;
            this.link = link;
            this.accentColor = accentColor;
        }

        @Override
        public void draw(GuiScreen screen, int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY);


            int bgColor = hovered ? 0xFF1a1a1a : 0xFF121212;
            drawRect(x, y, x + width, y + height, bgColor);

            if (hovered) {

                drawRect(x, y, x + width, y + 3, accentColor);
                drawRect(x, y + height - 3, x + width, y + height, accentColor);
            }


            GlStateManager.pushMatrix();
            GlStateManager.scale(1.875f, 1.875f, 1.875f);
            GuiModSettings.this.drawString(GuiModSettings.this.fontRendererObj, name,
                    (int)((x + 22) / 1.875f), (int)((y + 20) / 1.875f), 0xFFFFFFFF);
            GlStateManager.popMatrix();


            int buttonX = x + width - 120;
            int buttonY = y + 20;
            int buttonWidth = 100;
            int buttonHeight = 30;

            int buttonColor = hovered ? accentColor : 0xFF444444;
            drawRect(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, buttonColor);

            String buttonText = link.contains("@gmail") ? "§fCopy" : "§fOpen";
            int textWidth = GuiModSettings.this.fontRendererObj.getStringWidth(buttonText);
            GuiModSettings.this.drawString(GuiModSettings.this.fontRendererObj, buttonText,
                    buttonX + buttonWidth / 2 - textWidth / 2, buttonY + 10, 0xFFFFFFFF);
        }

        @Override
        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        @Override
        public void onClick() {
            if (link.contains("@gmail")) {

                try {
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new java.awt.datatransfer.StringSelection(link), null);

                    net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new net.minecraft.util.ChatComponentText("§a✓ Email copied to clipboard: §f" + link)
                    );
                } catch (Exception e) {
                    net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new net.minecraft.util.ChatComponentText("§c✗ Failed to copy: " + e.getMessage())
                    );
                }
            } else {
                try {
                    Desktop.getDesktop().browse(new URI(link));
                    net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new net.minecraft.util.ChatComponentText("§a✓ Link opened in Browser!")
                    );
                } catch (Exception e) {
                    net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new net.minecraft.util.ChatComponentText("§c✗ Failed to open: " + e.getMessage())
                    );
                }
            }
        }

        @Override
        public List<String> getTooltip() {
            List<String> tooltip = new ArrayList<>();
            tooltip.add(name);
            tooltip.add("§7" + description);
            tooltip.add("");
            tooltip.add("§8" + link);
            return tooltip;
        }
    }

    private class PasswordToggleSetting implements SettingButton {
        private int x, y, width, height;
        private String name;
        private java.util.function.Supplier<Boolean> getter;
        private java.util.function.Consumer<Boolean> setter;
        private String description;

        public PasswordToggleSetting(int x, int y, int width, int height, String name,
                                     java.util.function.Supplier<Boolean> getter,
                                     java.util.function.Consumer<Boolean> setter,
                                     String description) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.name = name; this.getter = getter; this.setter = setter; this.description = description;
        }

        @Override
        public void draw(GuiScreen screen, int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY);
            boolean enabled = getter.get();
            int bgColor = hovered ? 0xFF1a1a1a : 0xFF121212;
            drawRect(x, y, x + width, y + height, bgColor);
            if (hovered) {
                drawRect(x, y, x + width, y + 3, 0xFF303030);
                drawRect(x, y + height - 3, x + width, y + height, 0xFF303030);
            }
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.875f, 1.875f, 1.875f);
            String displayName = "§6\uD83D\uDD12 §r" + name;
            GuiModSettings.this.drawString(GuiModSettings.this.fontRendererObj, displayName,
                    (int)((x + 22) / 1.875f), (int)((y + 20) / 1.875f), 0xFFFFFFFF);
            GlStateManager.popMatrix();
            int toggleX = x + width - 100, toggleY = y + 20, toggleWidth = 80, toggleHeight = 30;
            int toggleBgColor = enabled ? 0xFF00aa00 : 0xFF444444;
            drawRect(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, toggleBgColor);
            int circleX = enabled ? toggleX + toggleWidth - 25 : toggleX + 8, circleY = toggleY + 4;
            drawRect(circleX, circleY, circleX + 22, circleY + 22, 0xFFFFFFFF);
        }
        @Override public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
        @Override public void onClick() { setter.accept(!getter.get()); }
        @Override public List<String> getTooltip() {
            List<String> tooltip = new ArrayList<>();
            tooltip.add("§e" + name); tooltip.add("§7" + description);
            return tooltip;
        }
    }

    private class ToggleSetting implements SettingButton {
        private int x, y, width, height;
        private String name;
        private java.util.function.Supplier<Boolean> getter;
        private java.util.function.Consumer<Boolean> setter;
        private String description;

        public ToggleSetting(int x, int y, int width, int height, String name,
                             java.util.function.Supplier<Boolean> getter,
                             java.util.function.Consumer<Boolean> setter,
                             String description) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.name = name; this.getter = getter; this.setter = setter; this.description = description;
        }

        @Override
        public void draw(GuiScreen screen, int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY);
            boolean enabled = getter.get();
            int bgColor = hovered ? 0xFF1a1a1a : 0xFF121212;
            drawRect(x, y, x + width, y + height, bgColor);
            if (hovered) {
                drawRect(x, y, x + width, y + 3, 0xFF303030);
                drawRect(x, y + height - 3, x + width, y + height, 0xFF303030);
            }
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.875f, 1.875f, 1.875f);
            GuiModSettings.this.drawString(GuiModSettings.this.fontRendererObj, name,
                    (int)((x + 22) / 1.875f), (int)((y + 20) / 1.875f), 0xFFFFFFFF);
            GlStateManager.popMatrix();
            int toggleX = x + width - 100, toggleY = y + 20, toggleWidth = 80, toggleHeight = 30;
            int toggleBgColor = enabled ? 0xFF00aa00 : 0xFF444444;
            drawRect(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, toggleBgColor);
            int circleX = enabled ? toggleX + toggleWidth - 25 : toggleX + 8, circleY = toggleY + 4;
            drawRect(circleX, circleY, circleX + 22, circleY + 22, 0xFFFFFFFF);
        }
        @Override public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
        @Override public void onClick() { setter.accept(!getter.get()); }
        @Override public List<String> getTooltip() {
            List<String> tooltip = new ArrayList<>();
            tooltip.add("§e" + name); tooltip.add("§7" + description);
            return tooltip;
        }
    }

    private class ButtonSetting implements SettingButton {
        private int x, y, width, height;
        private String name, description;
        public ButtonSetting(int x, int y, int width, int height, String name, String description) {
            this.x = x; this.y = y; this.width = width; this.height = height; this.name = name; this.description = description;
        }
        @Override public void draw(GuiScreen screen, int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY);
            int bgColor = hovered ? 0xFF1a5a1a : 0xFF124512;
            drawRect(x, y, x + width, y + height, bgColor);
            if (hovered) {
                drawRect(x, y, x + width, y + 3, 0xFF30aa30);
                drawRect(x, y + height - 3, x + width, y + height, 0xFF30aa30);
            }
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.875f, 1.875f, 1.875f);
            int textWidth = GuiModSettings.this.fontRendererObj.getStringWidth(name);
            int textX = (int)((x + width / 2) / 1.875f) - textWidth / 2;
            GuiModSettings.this.drawString(GuiModSettings.this.fontRendererObj, "§a" + name,
                    textX, (int)((y + 20) / 1.875f), 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }
        @Override public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
        @Override public void onClick() {
            if (name.equals("Configure Triggers")) {
                net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new GuiTriggerSettings(GuiModSettings.this));
            }
        }
        @Override public List<String> getTooltip() {
            List<String> tooltip = new ArrayList<>();
            tooltip.add("§e" + name); tooltip.add("§7" + description);
            return tooltip;
        }
    }

    private class SliderSetting implements SettingButton {
        private int x, y, width, height;
        private String name, description;
        private java.util.function.Supplier<Float> getter;
        private java.util.function.Consumer<Float> setter;
        private float min, max;
        public boolean dragging = false;

        public SliderSetting(int x, int y, int width, int height, String name,
                             java.util.function.Supplier<Float> getter,
                             java.util.function.Consumer<Float> setter,
                             float min, float max, String description) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.name = name; this.getter = getter; this.setter = setter;
            this.min = min; this.max = max; this.description = description;
        }

        @Override public void draw(GuiScreen screen, int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY);
            float value = getter.get();
            int bgColor = hovered ? 0xFF1a1a1a : 0xFF121212;
            drawRect(x, y, x + width, y + height, bgColor);
            if (hovered) {
                drawRect(x, y, x + width, y + 3, 0xFF303030);
                drawRect(x, y + height - 3, x + width, y + height, 0xFF303030);
            }
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.875f, 1.875f, 1.875f);
            String displayText = String.format(description, value);
            GuiModSettings.this.drawString(GuiModSettings.this.fontRendererObj, displayText,
                    (int)((x + 22) / 1.875f), (int)((y + 20) / 1.875f), 0xFFFFFFFF);
            GlStateManager.popMatrix();
            int sliderX = x + width - 320, sliderY = y + 20, sliderWidth = 300, sliderHeight = 30;
            drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + sliderHeight, 0xFF444444);
            float percentage = (value - min) / (max - min);
            int fillWidth = (int)(sliderWidth * percentage);
            drawRect(sliderX, sliderY, sliderX + fillWidth, sliderY + sliderHeight, 0xFFFFaa00);
            if (dragging) {
                int relativeX = mouseX - sliderX;
                float newPercentage = Math.max(0, Math.min(1, relativeX / (float)sliderWidth));
                float newValue = min + (max - min) * newPercentage;
                setter.accept(newValue);
            }
        }
        @Override public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
        @Override public void onClick() { dragging = true; }
        @Override public List<String> getTooltip() {
            List<String> tooltip = new ArrayList<>();
            tooltip.add("§e" + name); tooltip.add("§7Drag the slider to change its Value");
            return tooltip;
        }
    }

    private class ColorSliderSetting implements SettingButton {
        private int x, y, width, height;
        private String name, description;
        private java.util.function.Supplier<Float> getter;
        private java.util.function.Consumer<Float> setter;
        private float min, max;
        public boolean dragging = false;

        public ColorSliderSetting(int x, int y, int width, int height, String name,
                                  java.util.function.Supplier<Float> getter,
                                  java.util.function.Consumer<Float> setter,
                                  float min, float max, String description) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.name = name; this.getter = getter; this.setter = setter;
            this.min = min; this.max = max; this.description = description;
        }

        @Override
        public void draw(GuiScreen screen, int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY);
            float hue = getter.get();
            int bgColor = hovered ? 0xFF1a1a1a : 0xFF121212;
            drawRect(x, y, x + width, y + height, bgColor);
            if (hovered) {
                drawRect(x, y, x + width, y + 3, 0xFF303030);
                drawRect(x, y + height - 3, x + width, y + height, 0xFF303030);
            }


            GlStateManager.pushMatrix();
            GlStateManager.scale(1.875f, 1.875f, 1.875f);
            GuiModSettings.this.drawString(GuiModSettings.this.fontRendererObj, name,
                    (int)((x + 22) / 1.875f), (int)((y + 20) / 1.875f), 0xFFFFFFFF);
            GlStateManager.popMatrix();


            int sliderX = x + width - 320;
            int sliderY = y + 20;
            int sliderWidth = 300;
            int sliderHeight = 30;


            int segments = 60;
            for (int i = 0; i < segments; i++) {
                float segmentHue = min + (max - min) * ((float) i / segments);
                int[] rgb = hueToRgb(segmentHue);
                int color = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];

                int segX = sliderX + (i * sliderWidth / segments);
                int segWidth = (sliderWidth / segments) + 1;
                drawRect(segX, sliderY, segX + segWidth, sliderY + sliderHeight, color);
            }


            float percentage = (hue - min) / (max - min);
            int indicatorX = sliderX + (int) (sliderWidth * percentage);
            drawRect(indicatorX - 2, sliderY - 5, indicatorX + 2, sliderY + sliderHeight + 5, 0xFFFFFFFF);


            int[] currentRgb = hueToRgb(hue);
            int previewColor = 0xFF000000 | (currentRgb[0] << 16) | (currentRgb[1] << 8) | currentRgb[2];
            int previewSize = 25;
            int previewX = x + width - 350;
            int previewY = y + 22;
            drawRect(previewX, previewY, previewX + previewSize, previewY + previewSize, previewColor);
            drawRect(previewX, previewY, previewX + previewSize, previewY + previewSize - 22, 0x80000000);

            if (dragging) {
                int relativeX = mouseX - sliderX;
                float newPercentage = Math.max(0, Math.min(1, relativeX / (float) sliderWidth));
                float newValue = min + (max - min) * newPercentage;
                setter.accept(newValue);
            }
        }

        private int[] hueToRgb(float hue) {

            float h = hue / 60.0f;
            int sector = (int) Math.floor(h);
            float fraction = h - sector;

            int[] rgb = new int[3];

            switch (sector % 6) {
                case 0:
                    rgb[0] = 255;
                    rgb[1] = (int) (255 * fraction);
                    rgb[2] = 0;
                    break;
                case 1:
                    rgb[0] = (int) (255 * (1 - fraction));
                    rgb[1] = 255;
                    rgb[2] = 0;
                    break;
                case 2:
                    rgb[0] = 0;
                    rgb[1] = 255;
                    rgb[2] = (int) (255 * fraction);
                    break;
                case 3:
                    rgb[0] = 0;
                    rgb[1] = (int) (255 * (1 - fraction));
                    rgb[2] = 255;
                    break;
                case 4:
                    rgb[0] = (int) (255 * fraction);
                    rgb[1] = 0;
                    rgb[2] = 255;
                    break;
                case 5:
                    rgb[0] = 255;
                    rgb[1] = 0;
                    rgb[2] = (int) (255 * (1 - fraction));
                    break;
            }

            return rgb;
        }

        @Override
        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        @Override
        public void onClick() {
            dragging = true;
        }

        @Override
        public List<String> getTooltip() {
            List<String> tooltip = new ArrayList<>();
            tooltip.add("§e" + name);
            tooltip.add("§7" + description);
            return tooltip;
        }
    }
}