package com.Wasserfall_26.KK;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

public class ChatEventHandler {

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!ModConfig.chatSwitcherEnabled) return;

        if (!(event.gui instanceof GuiChat)) return;

        if (Keyboard.getEventKey() != Keyboard.KEY_RETURN || !Keyboard.getEventKeyState()) return;

        GuiChat chatGui = (GuiChat) event.gui;

        try {
            GuiTextField textField = getTextField(chatGui);

            if (textField != null) {
                String message = textField.getText();

                System.out.println("[KK] Original message: '" + message + "'");
                System.out.println("[KK] Current mode: " + ModConfig.currentChatMode);

                if (!message.startsWith("/") && !message.isEmpty()) {
                    String prefix = ChatSwitcher.getCurrentChatCommand();

                    System.out.println("[KK] Prefix: '" + prefix + "'");

                    if (!prefix.isEmpty()) {
                        textField.setText(prefix + message);
                        System.out.println("[KK] Modified to: '" + textField.getText() + "'");
                    }
                }
            } else {
                System.out.println("[KK] TextField is null!");
            }
        } catch (Exception e) {
            System.out.println("[KK] Error:");
            e.printStackTrace();
        }
    }

    private GuiTextField getTextField(GuiChat chatGui) {
        String[] possibleFieldNames = {"inputField", "field_146415_a", "message"};

        for (String fieldName : possibleFieldNames) {
            try {
                Field field = GuiChat.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object obj = field.get(chatGui);
                if (obj instanceof GuiTextField) {
                    System.out.println("[KK] Found TextField with field name: " + fieldName);
                    return (GuiTextField) obj;
                }
            } catch (Exception e) {

            }
        }

        return null;
    }
}