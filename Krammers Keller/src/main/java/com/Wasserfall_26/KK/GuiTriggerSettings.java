package com.Wasserfall_26.KK;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiTriggerSettings extends GuiScreen {

    private GuiScreen parent;
    private GuiTextField triggerField;
    private GuiTextField responseField;
    private List<TriggerEntry> triggerEntries = new ArrayList<>();
    private float scrollOffset = 0;
    private float targetScrollOffset = 0;

    private static final int HEADER_HEIGHT = 135;
    private static final int ENTRY_HEIGHT = 80;
    private static final int ENTRY_SPACING = 90;
    private static final int VISIBLE_ENTRIES = 3;
    private static final float SCROLL_SPEED = 0.2f;

    public GuiTriggerSettings(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        triggerField = new GuiTextField(0, this.fontRendererObj,
                this.width / 2 - 400, HEADER_HEIGHT + 100,
                350, 45);
        triggerField.setMaxStringLength(100);

        responseField = new GuiTextField(1, this.fontRendererObj,
                this.width / 2 - 20, HEADER_HEIGHT + 100,
                420, 45);
        responseField.setMaxStringLength(256);

        loadTriggerEntries();
    }

    private void loadTriggerEntries() {
        triggerEntries.clear();
        Map<String, ChatTriggerManager.TriggerData> triggers = ChatTriggerManager.getInstance().getTriggers();

        int yPos = HEADER_HEIGHT + 220;
        for (Map.Entry<String, ChatTriggerManager.TriggerData> entry : triggers.entrySet()) {
            triggerEntries.add(new TriggerEntry(entry.getValue(), yPos));
            yPos += ENTRY_SPACING;
        }

        clampScroll();
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, (triggerEntries.size() - VISIBLE_ENTRIES) * ENTRY_SPACING);
        targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScroll));
        scrollOffset = targetScrollOffset;
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        KK.config.saveConfig();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {

            int direction = wheel > 0 ? -1 : 1;
            targetScrollOffset += direction * ENTRY_SPACING;

            int maxScroll = Math.max(0, (triggerEntries.size() - VISIBLE_ENTRIES) * ENTRY_SPACING);
            targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScroll));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (triggerField.isFocused()) {
            triggerField.textboxKeyTyped(typedChar, keyCode);
        } else if (responseField.isFocused()) {
            responseField.textboxKeyTyped(typedChar, keyCode);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        triggerField.mouseClicked(mouseX, mouseY, mouseButton);
        responseField.mouseClicked(mouseX, mouseY, mouseButton);

        int addX = this.width / 2 + 420;
        int addY = HEADER_HEIGHT + 100;
        int addSize = 45;
        if (mouseX >= addX && mouseX <= addX + addSize &&
                mouseY >= addY && mouseY <= addY + addSize) {
            String trigger = triggerField.getText().trim();
            String response = responseField.getText().trim();

            if (!trigger.isEmpty() && !response.isEmpty()) {
                ChatTriggerManager.getInstance().addTrigger(trigger, response);
                triggerField.setText("");
                responseField.setText("");
                loadTriggerEntries();
            }
            return;
        }

        int backX = 30;
        int backY = this.height - 90;
        int backWidth = 360;
        int backHeight = 60;
        if (mouseX >= backX && mouseX <= backX + backWidth &&
                mouseY >= backY && mouseY <= backY + backHeight) {
            this.mc.displayGuiScreen(parent);
            return;
        }

        for (TriggerEntry entry : triggerEntries) {
            if (entry.isDeleteHovered(mouseX, mouseY)) {
                ChatTriggerManager.getInstance().removeTrigger(entry.data.trigger);
                loadTriggerEntries();
                break;
            }

            if (entry.isToggleHovered(mouseX, mouseY)) {
                ChatTriggerManager.getInstance().toggleTrigger(entry.data.trigger);
                loadTriggerEntries();
                break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Math.abs(targetScrollOffset - scrollOffset) > 0.5f) {
            scrollOffset += (targetScrollOffset - scrollOffset) * SCROLL_SPEED;
        } else {
            scrollOffset = targetScrollOffset;
        }

        drawRect(0, 0, this.width, this.height, 0xC0000000);

        drawRect(0, 0, this.width, HEADER_HEIGHT - 6, 0xFF0a0a0a);
        drawRect(0, HEADER_HEIGHT - 6, this.width, HEADER_HEIGHT, 0xFFFFaa00);

        GlStateManager.pushMatrix();
        GlStateManager.scale(7.5f, 7.5f, 7.5f);
        this.drawCenteredString(this.fontRendererObj, "§6§lMessage Triggers",
                (int)(this.width / 15.0f), 3, 0xFFFFFFFF);
        GlStateManager.popMatrix();

        drawRect(0, HEADER_HEIGHT, this.width, this.height, 0xF0151515);


        GlStateManager.pushMatrix();
        GlStateManager.scale(2.5f, 2.5f, 2.5f);
        this.drawString(this.fontRendererObj, "§eTrigger:",
                (int)((this.width / 2 - 400) / 2.5f), (int)((HEADER_HEIGHT + 70) / 2.5f), 0xFFFFaa00);
        this.drawString(this.fontRendererObj, "§eAnswer:",
                (int)((this.width / 2 - 20) / 2.5f), (int)((HEADER_HEIGHT + 70) / 2.5f), 0xFFFFaa00);
        GlStateManager.popMatrix();

        triggerField.drawTextBox();
        responseField.drawTextBox();

        int addX = this.width / 2 + 420;
        int addY = HEADER_HEIGHT + 100;
        int addSize = 45;

        boolean addHovered = mouseX >= addX && mouseX <= addX + addSize &&
                mouseY >= addY && mouseY <= addY + addSize;
        int addColor = addHovered ? 0xFF00cc00 : 0xFF008800;

        drawRect(addX, addY, addX + addSize, addY + addSize, addColor);

        GlStateManager.pushMatrix();
        GlStateManager.scale(3.0f, 3.0f, 3.0f);
        this.drawCenteredString(this.fontRendererObj,
                "§f+", (int)((addX + addSize / 2) / 3.0f) + 1, (int)((addY + 12) / 3.0f), 0xFFFFFFFF);
        GlStateManager.popMatrix();

        drawRect(30, HEADER_HEIGHT + 180, this.width - 30, HEADER_HEIGHT + 184, 0xFF303030);

        GlStateManager.pushMatrix();
        GlStateManager.scale(2.5f, 2.5f, 2.5f);
        this.drawString(this.fontRendererObj, "§e§lSaved Triggers:",
                (int)(40 / 2.5f), (int)((HEADER_HEIGHT + 195) / 2.5f), 0xFFFFaa00);
        GlStateManager.popMatrix();

        if (triggerEntries.size() > VISIBLE_ENTRIES) {
            int maxScroll = (triggerEntries.size() - VISIBLE_ENTRIES) * ENTRY_SPACING;
            float scrollPercent = maxScroll > 0 ? scrollOffset / maxScroll : 0;

            int indicatorX = this.width - 45;
            int indicatorY = HEADER_HEIGHT + 220;
            int indicatorHeight = VISIBLE_ENTRIES * ENTRY_SPACING;
            int barHeight = Math.max(20, indicatorHeight / triggerEntries.size());

            drawRect(indicatorX, indicatorY, indicatorX + 10, indicatorY + indicatorHeight, 0xFF303030);

            int barY = indicatorY + (int)((indicatorHeight - barHeight) * scrollPercent);
            drawRect(indicatorX, barY, indicatorX + 10, barY + barHeight, 0xFFFFaa00);
        }

        int listStartY = HEADER_HEIGHT + 220;
        int listHeight = VISIBLE_ENTRIES * ENTRY_SPACING;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffset, 0);

        for (TriggerEntry entry : triggerEntries) {
            int entryScreenY = (int)(entry.y - scrollOffset);
            if (entryScreenY + ENTRY_HEIGHT >= listStartY && entryScreenY <= listStartY + listHeight) {
                entry.draw(mouseX, (int)(mouseY + scrollOffset));
            }
        }

        GlStateManager.popMatrix();

        if (scrollOffset > 0) {
            drawGradientRect(30, listStartY, this.width - 30, listStartY + 20, 0xFF151515, 0x00151515);
        }
        if (scrollOffset < (triggerEntries.size() - VISIBLE_ENTRIES) * ENTRY_SPACING) {
            drawGradientRect(30, listStartY + listHeight - 20, this.width - 30, listStartY + listHeight, 0x00151515, 0xFF151515);
        }

        int backX = 30;
        int backY = this.height - 90;
        int backWidth = 360;
        int backHeight = 60;

        boolean backHovered = mouseX >= backX && mouseX <= backX + backWidth &&
                mouseY >= backY && mouseY <= backY + backHeight;

        int backColor = backHovered ? 0xFF181818 : 0xFF101010;
        drawRect(backX, backY, backX + backWidth, backY + backHeight, backColor);

        drawRect(backX, backY, backX + 2, backY + backHeight, 0xFFFFaa00);

        int textColor = backHovered ? 0xFFCCCCCC : 0xFF888888;
        GlStateManager.pushMatrix();
        GlStateManager.scale(3.3f, 3.3f, 3.3f);
        this.drawString(this.fontRendererObj, "§cBack",
                (int)((backX + 22) / 3.3f), (int)((backY + 18) / 3.3f), textColor);
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private class TriggerEntry {
        ChatTriggerManager.TriggerData data;
        int y;

        public TriggerEntry(ChatTriggerManager.TriggerData data, int y) {
            this.data = data;
            this.y = y;
        }

        public void draw(int mouseX, int mouseY) {
            int x = 30;
            int width = GuiTriggerSettings.this.width - 60;

            boolean hovered = mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + ENTRY_HEIGHT;

            int bgColor = data.enabled ?
                    (hovered ? 0xFF1a1a1a : 0xFF121212) :
                    (hovered ? 0xFF0a0a0a : 0xFF050505);
            drawRect(x, y, x + width, y + ENTRY_HEIGHT, bgColor);

            drawRect(x, y, x + width, y + 3, 0xFF303030);

            GlStateManager.pushMatrix();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            String triggerColor = data.enabled ? "§e" : "§7";
            GuiTriggerSettings.this.drawString(GuiTriggerSettings.this.fontRendererObj,
                    triggerColor + data.trigger, (int)((x + 20) / 2.0f), (int)((y + 15) / 2.0f), 0xFFFFFFFF);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.scale(1.5f, 1.5f, 1.5f);
            String displayResponse = data.response.length() > 60 ? data.response.substring(0, 60) + "..." : data.response;
            int responseColor = data.enabled ? 0xFFAAAAAA : 0xFF555555;
            GuiTriggerSettings.this.drawString(GuiTriggerSettings.this.fontRendererObj,
                    "§7→ " + displayResponse, (int)((x + 20) / 1.5f), (int)((y + 45) / 1.5f), responseColor);
            GlStateManager.popMatrix();

            int toggleX = x + width - 160;
            int toggleY = y + 20;
            int toggleWidth = 40;
            int toggleHeight = 40;

            boolean toggleHovered = isToggleHovered(mouseX, mouseY);
            int toggleColor = data.enabled ?
                    (toggleHovered ? 0xFF00cc00 : 0xFF008800) :
                    (toggleHovered ? 0xFF666666 : 0xFF444444);

            drawRect(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, toggleColor);

            GlStateManager.pushMatrix();
            GlStateManager.scale(2.5f, 2.5f, 2.5f);
            String toggleSymbol = data.enabled ? "§f✓" : "§f✗";
            GuiTriggerSettings.this.drawCenteredString(GuiTriggerSettings.this.fontRendererObj,
                    toggleSymbol, (int)((toggleX + toggleWidth / 2) / 2.5f), (int)((toggleY + 10) / 2.5f), 0xFFFFFFFF);
            GlStateManager.popMatrix();

            int deleteX = x + width - 100;
            int deleteY = y + 20;
            int deleteSize = 40;

            boolean deleteHovered = isDeleteHovered(mouseX, mouseY);
            int deleteColor = deleteHovered ? 0xFFcc0000 : 0xFF880000;

            drawRect(deleteX, deleteY, deleteX + deleteSize, deleteY + deleteSize, deleteColor);

            GlStateManager.pushMatrix();
            GlStateManager.scale(2.5f, 2.5f, 2.5f);
            GuiTriggerSettings.this.drawCenteredString(GuiTriggerSettings.this.fontRendererObj,
                    "§fX", (int)((deleteX + deleteSize / 2) / 2.5f), (int)((deleteY + 10) / 2.5f), 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }

        public boolean isToggleHovered(int mouseX, int mouseY) {
            int x = 30;
            int width = GuiTriggerSettings.this.width - 60;
            int toggleX = x + width - 160;
            int toggleY = y + 20;
            int toggleSize = 40;

            return mouseX >= toggleX && mouseX <= toggleX + toggleSize &&
                    mouseY >= toggleY && mouseY <= toggleY + toggleSize;
        }

        public boolean isDeleteHovered(int mouseX, int mouseY) {
            int x = 30;
            int width = GuiTriggerSettings.this.width - 60;
            int deleteX = x + width - 100;
            int deleteY = y + 20;
            int deleteSize = 40;

            return mouseX >= deleteX && mouseX <= deleteX + deleteSize &&
                    mouseY >= deleteY && mouseY <= deleteY + deleteSize;
        }
    }
}