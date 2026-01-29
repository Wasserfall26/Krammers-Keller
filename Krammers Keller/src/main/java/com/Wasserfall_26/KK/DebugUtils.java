package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class DebugUtils {


    public static void debug(String message) {
        if (!ModConfig.debugMode) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§8[§6KK Debug§8] §7" + message));
        }


        System.out.println("[KK Debug] " + message);
    }


    public static void info(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§8[§6KK§8] §7" + message));
        }
        System.out.println("[KK] " + message);
    }


    public static void error(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§8[§6KK§8] §c" + message));
        }
        System.err.println("[KK Error] " + message);
    }


    public static void success(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§8[§6KK§8] §a" + message));
        }
        System.out.println("[KK] " + message);
    }
}