package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BloodRoomHandler {

    private static final BloodRoomHandler INSTANCE = new BloodRoomHandler();

    private boolean inBloodRoom = false;
    private int remainingMobs = 0;
    private float currentHealthPercent = 0.0f;
    private String customBossBarText = "";
    private long lastUpdateTime = 0;


    private int initialHealth = -1;
    private boolean watcherSpawned = false;

    public static BloodRoomHandler getInstance() {
        return INSTANCE;
    }

    private boolean isInDungeon() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return false;

        try {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            if (scoreboard == null) return false;

            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            if (objective == null) return false;

            String displayName = objective.getDisplayName();
            if (displayName != null) {
                String cleanName = stripFormatting(displayName).toLowerCase();
                return cleanName.contains("catacombs") || cleanName.contains("dungeon");
            }
        } catch (Exception e) {
            // Ignore
        }

        return false;
    }

    private String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("§.", "");
    }

    public void processBossBar(String bossBarName, float healthPercent) {
        if (!ModConfig.bloodRoomProgressEnabled) {
            if (inBloodRoom) {
                reset();
            }
            return;
        }

        if (bossBarName == null || bossBarName.isEmpty()) {
            if (inBloodRoom) {
                reset();
            }
            return;
        }

        String lowerName = bossBarName.toLowerCase().trim();

        if (lowerName.contains("watcher")) {

            if (!watcherSpawned) {
                watcherSpawned = true;
            }

            boolean wasInBloodRoom = inBloodRoom;
            inBloodRoom = true;

            int newRemainingMobs = Math.max(0, (int) Math.ceil(healthPercent / 5.263150f));

            if (!wasInBloodRoom || newRemainingMobs != remainingMobs ||
                    Math.abs(healthPercent - currentHealthPercent) > 0.5f) {

                remainingMobs = newRemainingMobs;
                currentHealthPercent = healthPercent;

                String mobColor = remainingMobs > 10 ? "§c" : (remainingMobs > 5 ? "§6" : "§a");
                customBossBarText = "§6§lBlood Room §8- " + mobColor + remainingMobs + " §7Mobs";

            }

        } else {
            if (inBloodRoom) {
                reset();
            }
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (!ModConfig.bloodRoomProgressEnabled) return;
        if (event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH) return;


        if (BossStatus.bossName == null || BossStatus.bossName.isEmpty()) {
            return;
        }


        String cleanName = stripFormatting(BossStatus.bossName).toLowerCase();
        if (!cleanName.contains("watcher")) {
            return;
        }


        event.setCanceled(true);


        if (!inBloodRoom) {
            inBloodRoom = true;
            watcherSpawned = true;
            float healthPercent = BossStatus.healthScale * 100.0f;
            remainingMobs = Math.max(0, (int) Math.ceil(healthPercent / 5.263f)); // 19 Mobs
            currentHealthPercent = healthPercent;
            String mobColor = remainingMobs > 10 ? "§c" : (remainingMobs > 5 ? "§6" : "§a");
            customBossBarText = "§6§lBlood Room §8- " + mobColor + remainingMobs + " §7Mobs";
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int width = scaledResolution.getScaledWidth();

        int barWidth = 182;
        int barHeight = 5;
        int x = width / 2 - barWidth / 2;
        int y = 12;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        mc.getTextureManager().bindTexture(Gui.icons);


        mc.ingameGUI.drawTexturedModalRect(x, y, 0, 74, barWidth, barHeight);


        float progressPercent = 1.0f - (currentHealthPercent / 100.0f);
        int filledWidth = (int) (barWidth * progressPercent);

        if (filledWidth > 0) {
            GlStateManager.color(1.0f, 0.65f, 0.0f, 1.0f);
            mc.ingameGUI.drawTexturedModalRect(x, y, 0, 79, filledWidth, barHeight);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }


        FontRenderer fontRenderer = mc.fontRendererObj;
        int textX = width / 2 - fontRenderer.getStringWidth(customBossBarText) / 2;
        int textY = y - 10;

        fontRenderer.drawStringWithShadow(customBossBarText, textX, textY, 0xFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public void reset() {
        if (inBloodRoom) {
            DebugUtils.debug("Blood Room tracking reset");
        }
        inBloodRoom = false;
        watcherSpawned = false;
        remainingMobs = 0;
        currentHealthPercent = 0.0f;
        customBossBarText = "";
        lastUpdateTime = 0;
        initialHealth = -1;
    }

    public boolean isInBloodRoom() {
        return inBloodRoom;
    }

    public int getRemainingMobs() {
        return remainingMobs;
    }
}