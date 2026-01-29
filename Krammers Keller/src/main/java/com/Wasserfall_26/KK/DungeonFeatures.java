package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class DungeonFeatures {

    private static final DungeonFeatures INSTANCE = new DungeonFeatures();

    private boolean shouldPressQ = false;
    private int qPressDelay = 0;
    private long lastWishMessage = 0;
    private static final long WISH_MESSAGE_COOLDOWN = 5000;
    private int debugTickCounter = 0;
    private boolean wasInDungeon = false;

    public static DungeonFeatures getInstance() {
        return INSTANCE;
    }

    private boolean isInDungeon() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return false;
        }

        try {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            if (scoreboard == null) {
                return false;
            }

            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            if (objective == null) {
                return false;
            }

            Collection<Score> scores = scoreboard.getSortedScores(objective);
            List<String> lines = new ArrayList<>();

            for (Score score : scores) {
                if (score != null && score.getPlayerName() != null) {
                    String formattedLine = ScorePlayerTeam.formatPlayerName(
                            scoreboard.getPlayersTeam(score.getPlayerName()),
                            score.getPlayerName()
                    );

                    if (formattedLine != null && !formattedLine.isEmpty()) {
                        lines.add(formattedLine);
                    }
                }
            }

            if (ModConfig.debugMode && debugTickCounter % 100 == 0 && !lines.isEmpty()) {
                DebugUtils.debug("=== SCOREBOARD (" + lines.size() + " lines) ===");
                for (int i = 0; i < lines.size(); i++) {
                    String clean = stripFormatting(lines.get(i));
                    DebugUtils.debug("[" + i + "] '" + clean + "'");
                }
            }

            for (String line : lines) {
                String cleanLine = stripFormatting(line).toLowerCase().trim();

                if (cleanLine.isEmpty()) continue;

                if (cleanLine.contains("the catacombs") ||
                        cleanLine.contains("catacombs") ||
                        cleanLine.contains("floor") ||
                        cleanLine.contains("cleared:") ||
                        cleanLine.contains("secrets") ||
                        cleanLine.contains("deaths") ||
                        cleanLine.contains("crypts") ||
                        cleanLine.contains("time elapsed") ||
                        cleanLine.contains("master mode") ||
                        cleanLine.matches(".*\\(f[0-9]\\).*") ||
                        cleanLine.matches(".*\\(m[0-9]\\).*") ||
                        cleanLine.contains("keys:") ||
                        cleanLine.contains("puzzle")) {

                    return true;
                }
            }

        } catch (Exception e) {
            DebugUtils.error("Error checking dungeon: " + e.getMessage());
        }

        return false;
    }

    private String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("§.", "");
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (event.type != 0) {
            return;
        }

        String message = event.message.getUnformattedText();
        String messageLower = message.toLowerCase();

        boolean inDungeon = isInDungeon();

        if (ModConfig.debugMode) {
            DebugUtils.debug("=== CHAT MESSAGE ===");
            DebugUtils.debug("Raw: '" + message + "'");
            DebugUtils.debug("Lower: '" + messageLower + "'");
            DebugUtils.debug("In Dungeon: " + inDungeon);
            DebugUtils.debug("Auto Wish: " + ModConfig.autoWishEnabled);
            DebugUtils.debug("===================");
        }

        if (!inDungeon) {
            return;
        }

        if (ModConfig.autoWishEnabled) {


            boolean isMaxorEnraged =
                    (messageLower.contains("maxor") && messageLower.contains("enraged")) ||
                            (messageLower.contains("maxor") && messageLower.contains("is enraged")) ||
                            messageLower.contains("maxor is enraged") || messageLower.contains("the core entrance is opening");

            if (isMaxorEnraged) {
                if (ModConfig.debugMode) {
                    DebugUtils.debug("MAXOR ENRAGED DETECTED");
                }

                triggerAutoWish();
                return;
            }




                int lastColon = message.lastIndexOf(": ");


                if (lastColon != -1 && lastColon < message.length() - 2) {
                    String partyMessage = message.substring(lastColon + 2);
                    String MessageLower = partyMessage.toLowerCase();


                    boolean isWish = MessageLower.equals(": wish") ||
                            MessageLower.startsWith("wish") ||
                            MessageLower.contains(": wish");

                    if (ModConfig.debugMode) {
                        DebugUtils.debug("Is wish? " + isWish);
                    }

                    if (isWish) {
                        if (ModConfig.debugMode) {
                            DebugUtils.debug("WISH MATCH CONFIRMED");
                        }

                        triggerAutoWish();
                        return;
                    }
                } else {
                    if (ModConfig.debugMode) {
                        DebugUtils.debug("No valid colon found in message");
                    }
                }
            }
        }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        debugTickCounter++;

        boolean inDungeon = isInDungeon();

        if (inDungeon && !wasInDungeon) {
            if (ModConfig.debugMode) {
                DebugUtils.debug("━━━━━━━━━━━━━━━━━━━━━━━━━━");
                DebugUtils.debug("✓ ENTERED DUNGEON!");
                DebugUtils.debug("Auto Wish: " + (ModConfig.autoWishEnabled ? "ON" : "OFF"));
                DebugUtils.debug("Low HP Wish: " + (ModConfig.autoWishOnLowHpEnabled ? "ON" : "OFF"));
                if (ModConfig.autoWishOnLowHpEnabled) {
                    DebugUtils.debug("HP Threshold: " + ModConfig.lowHpThreshold + "%");
                }
                DebugUtils.debug("━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
            wasInDungeon = true;
        } else if (!inDungeon && wasInDungeon) {
            if (ModConfig.debugMode) {
                DebugUtils.debug("✗ LEFT DUNGEON");
            }
            wasInDungeon = false;
            resetCooldown();
        }

        if (!inDungeon) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (shouldPressQ && qPressDelay > 0) {
            qPressDelay--;
            if (qPressDelay == 0) {
                pressQ();
                shouldPressQ = false;
            }
        }

        if (ModConfig.autoWishOnLowHpEnabled) {
            checkLowHp(mc.thePlayer);
        }
    }

    private void triggerAutoWish() {
        shouldPressQ = true;
        qPressDelay = 5;
        if (ModConfig.debugMode) {
            DebugUtils.debug("Q press scheduled in " + qPressDelay + " ticks");
        }
    }

    private void pressQ() {
        try {
            int qKey = Minecraft.getMinecraft().gameSettings.keyBindDrop.getKeyCode();

            KeyBinding.setKeyBindState(qKey, true);
            KeyBinding.onTick(qKey);

            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    KeyBinding.setKeyBindState(qKey, false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();


            if (ModConfig.debugMode) {
                DebugUtils.debug("✓ Q PRESSED!");
            }


        } catch (Exception e) {
            DebugUtils.error("Failed to press Q: " + e.getMessage());
        }
    }

    private void checkLowHp(EntityPlayerSP player) {
        float currentHp = player.getHealth();
        float maxHp = player.getMaxHealth();
        float hpPercent = (currentHp / maxHp) * 100.0f;

        if (ModConfig.debugMode && debugTickCounter % 100 == 0) {
            DebugUtils.debug("HP: " + String.format("%.1f", hpPercent) + "% | Threshold: " + ModConfig.lowHpThreshold + "%");
        }

        if (hpPercent <= ModConfig.lowHpThreshold) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastWishMessage >= WISH_MESSAGE_COOLDOWN) {
                player.sendChatMessage("/pc wish");
                lastWishMessage = currentTime;


            } else {
                if (ModConfig.debugMode && debugTickCounter % 20 == 0) {
                    long timeLeft = (WISH_MESSAGE_COOLDOWN - (currentTime - lastWishMessage)) / 1000;
                    DebugUtils.debug("Low HP but cooldown active (" + timeLeft + "s)");
                }
            }
        }
    }

    public void resetCooldown() {
        lastWishMessage = 0;
        shouldPressQ = false;
        qPressDelay = 0;
        if (ModConfig.debugMode) {
            DebugUtils.debug("Cooldowns reset");
        }
    }
}