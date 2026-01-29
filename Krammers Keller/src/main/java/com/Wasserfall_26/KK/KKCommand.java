package com.Wasserfall_26.KK;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.client.Minecraft;

public class KKCommand extends CommandBase {

    private static int ticksUntilOpen = 0;

    @Override
    public String getCommandName() {
        return "kk";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kk - Opens Settings";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        System.out.println("[KK] KK Command wurde ausgeführt!");
        ticksUntilOpen = 2;
    }

    public static void onClientTick() {
        if (ticksUntilOpen > 0) {
            ticksUntilOpen--;
            if (ticksUntilOpen == 0) {
                System.out.println("[KK] Öffne jetzt GUI nach Delay!");
                Minecraft.getMinecraft().displayGuiScreen(new GuiModSettings());
            }
        }
    }
}