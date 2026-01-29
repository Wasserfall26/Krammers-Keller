package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class ChatSwitcher {

    private boolean tabWasPressed = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ModConfig.chatSwitcherEnabled) return;
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.currentScreen instanceof GuiChat) {
            boolean tabPressed = Keyboard.isKeyDown(Keyboard.KEY_TAB);

            if (tabPressed && !tabWasPressed) {
                switchChatMode();
            }

            tabWasPressed = tabPressed;
        } else {
            tabWasPressed = false;
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        if (!ModConfig.chatSwitcherEnabled) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.currentScreen instanceof GuiChat) {
            ScaledResolution sr = new ScaledResolution(mc);

            String modeText;
            if (ModConfig.currentChatMode.equals("party")) {
                modeText = "§6[Chat: §d§lPARTY§6]";
            } else if (ModConfig.currentChatMode.equals("guild")) {
                modeText = "§6[Chat: §a§lGUILD§6]";
            } else {
                modeText = "§6[Chat: §7§lNONE§6]";
            }

            int textWidth = mc.fontRendererObj.getStringWidth(modeText);
            int chatLeft = 2;
            int chatWidth = (int) (sr.getScaledWidth() * 0.5F);
            int x = chatLeft + ((chatWidth / 2) - textWidth) / 2;
            int y = sr.getScaledHeight() - 24;

            mc.fontRendererObj.drawStringWithShadow(modeText, x, y, 0xFFFFFF);
        }
    }

    private void switchChatMode() {
        if (ModConfig.currentChatMode.equals("none")) {
            ModConfig.currentChatMode = "party";
        } else if (ModConfig.currentChatMode.equals("party")) {
            ModConfig.currentChatMode = "guild";
        } else {
            ModConfig.currentChatMode = "none";
        }
        KK.config.saveConfig();
    }

    public static String getCurrentChatCommand() {
        if (!ModConfig.chatSwitcherEnabled) return "";

        if (ModConfig.currentChatMode.equals("party")) {
            return "/pc ";
        } else if (ModConfig.currentChatMode.equals("guild")) {
            return "/gc ";
        } else {
            return "";
        }
    }
}