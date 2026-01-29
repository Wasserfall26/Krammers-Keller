package com.Wasserfall_26.KK;

import net.minecraft.entity.boss.BossStatus;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class BossBarInterceptor {

    private static final BossBarInterceptor INSTANCE = new BossBarInterceptor();
    private String lastBossName = "";
    private float lastHealthScale = 0.0f;
    private int tickCounter = 0;
    private int noBarTicks = 0;

    public static BossBarInterceptor getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        tickCounter++;

        try {
            if (BossStatus.bossName != null && !BossStatus.bossName.isEmpty()) {
                noBarTicks = 0;
                String bossName = BossStatus.bossName;
                float healthScale = BossStatus.healthScale;
                float healthPercent = healthScale * 100.0f;



                if (ModConfig.bloodRoomProgressEnabled) {
                    if (!bossName.equals(lastBossName) || Math.abs(healthScale - lastHealthScale) > 0.005f) {
                        lastBossName = bossName;
                        lastHealthScale = healthScale;

                        String cleanBossName = stripFormatting(bossName);
                        BloodRoomHandler.getInstance().processBossBar(cleanBossName, healthPercent);
                    }
                }
            } else {
                noBarTicks++;

                if (!lastBossName.isEmpty() && noBarTicks > 5) {
                    if (noBarTicks == 6) {
                        DebugUtils.debug("BossBar disappeared");
                    }
                    lastBossName = "";
                    lastHealthScale = 0.0f;
                    if (ModConfig.bloodRoomProgressEnabled) {
                        BloodRoomHandler.getInstance().reset();
                    }
                }
            }
        } catch (Exception e) {
            DebugUtils.error("BossBar interceptor error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderBossHealth(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH) return;

        try {
            if (BossStatus.bossName != null && !BossStatus.bossName.isEmpty()) {
                String bossName = stripFormatting(BossStatus.bossName);

                if (ModConfig.bloodRoomProgressEnabled) {
                    float healthPercent = BossStatus.healthScale * 100.0f;
                    BloodRoomHandler.getInstance().processBossBar(bossName, healthPercent);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("§.", "");
    }

    public void reset() {
        lastBossName = "";
        lastHealthScale = 0.0f;
        tickCounter = 0;
        noBarTicks = 0;
        if (ModConfig.bloodRoomProgressEnabled) {
            BloodRoomHandler.getInstance().reset();
        }
    }
}