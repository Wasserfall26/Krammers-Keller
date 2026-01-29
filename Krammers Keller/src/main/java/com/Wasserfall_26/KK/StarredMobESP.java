package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarredMobESP {

    private static final StarredMobESP INSTANCE = new StarredMobESP();
    private Map<EntityLivingBase, EntityArmorStand> starredMobs = new HashMap<>();
    private int scanCooldown = 0;
    private int debugCooldown = 0;
    private boolean debugMode = false;

    public static StarredMobESP getInstance() {
        return INSTANCE;
    }

    private void chat(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.YELLOW + "[StarredESP] " + EnumChatFormatting.WHITE + message
            ));
        }
    }

    private String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("§.", "");
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
                return cleanName.contains("skyblock") || cleanName.contains("catacombs") || cleanName.contains("dungeon");
            }
        } catch (Exception e) {
            // Ignore
        }

        return false;
    }

    private boolean isStarredName(String name) {
        if (name == null || name.isEmpty()) return false;


        boolean hasStarInRaw = name.contains("✯") ||
                name.contains("⭐") ||
                name.contains("✪") ||
                name.contains("★") ||
                name.contains("☆");

        String cleanName = stripFormatting(name);
        boolean hasStarInClean = cleanName.contains("✯") ||
                cleanName.contains("⭐") ||
                cleanName.contains("✪") ||
                cleanName.contains("★") ||
                cleanName.contains("☆");

        if (debugMode && debugCooldown <= 0) {
            chat("Checking name: '" + name + "' | Clean: '" + cleanName + "' | HasStar: " + (hasStarInRaw || hasStarInClean));
        }

        return hasStarInRaw || hasStarInClean;
    }

    /**
     * Prüft ob ein Mob ein Shadow Assassin sein könnte
     * Shadow Assassins sind:
     * - EntityZombie
     * - Haben keine Custom Name ODER Name ohne Stern (während unsichtbar)
     * - Sind unsichtbar (isInvisible)
     * - Spawnen in Dungeons
     */
    private boolean isShadowAssassin(EntityLivingBase mob) {

        if (!(mob instanceof EntityZombie)) return false;


        if (!mob.isInvisible()) return false;

        return true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (!ModConfig.starredMobEspEnabled) return;
        if (!ModConfig.allFeaturesEnabled) return;

        if (debugCooldown > 0) debugCooldown--;

        boolean inDungeon = isInDungeon();

        if (!inDungeon) {
            if (!starredMobs.isEmpty()) {
                starredMobs.clear();
            }
            return;
        }

        if (scanCooldown > 0) {
            scanCooldown--;
            return;
        }


        scanCooldown = 2;
        scanForStarredMobs();
    }

    private void scanForStarredMobs() {
        Minecraft mc = Minecraft.getMinecraft();
        int oldSize = starredMobs.size();
        starredMobs.clear();


        List<EntityArmorStand> allArmorStands = new ArrayList<>();
        List<EntityLivingBase> allMobs = new ArrayList<>();

        for (Object obj : mc.theWorld.loadedEntityList) {
            Entity entity = (Entity) obj;


            double distance = mc.thePlayer.getDistanceToEntity(entity);
            if (distance > 50) continue;

            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                allArmorStands.add(armorStand);
            } else if (entity instanceof EntityLivingBase) {
                EntityLivingBase mob = (EntityLivingBase) entity;
                if (mob != mc.thePlayer) {
                    allMobs.add(mob);
                }
            }
        }


        if (debugMode && debugCooldown <= 0) {
            chat("Scanning " + allArmorStands.size() + " armor stands and " + allMobs.size() + " mobs");
        }

        List<EntityArmorStand> starredArmorStands = new ArrayList<>();
        for (EntityArmorStand armorStand : allArmorStands) {
            if (armorStand.hasCustomName()) {
                String name = armorStand.getCustomNameTag();
                if (isStarredName(name)) {
                    starredArmorStands.add(armorStand);

                    if (debugMode && debugCooldown <= 0) {
                        chat("Found starred ArmorStand: '" + name + "'");
                    }
                }
            }
        }

        for (EntityArmorStand as : starredArmorStands) {
            EntityLivingBase nearestMob = findClosestMob(as, allMobs);
            if (nearestMob != null) {
                starredMobs.put(nearestMob, as);

                if (debugMode && debugCooldown <= 0) {
                    chat("Linked mob to starred ArmorStand");
                }
            }
        }

        for (EntityLivingBase mob : allMobs) {
            if (mob.hasCustomName()) {
                String name = mob.getCustomNameTag();
                if (isStarredName(name)) {
                    if (!starredMobs.containsKey(mob)) {
                        starredMobs.put(mob, null);

                        if (debugMode && debugCooldown <= 0) {
                            chat("Found mob with starred name: '" + name + "'");
                        }
                    }
                }
            }
        }


        for (EntityLivingBase mob : allMobs) {
            if (isShadowAssassin(mob)) {

                EntityArmorStand nearbyArmorStand = findClosestArmorStand(mob, allArmorStands);

                if (!starredMobs.containsKey(mob)) {
                    starredMobs.put(mob, nearbyArmorStand);

                    if (debugMode && debugCooldown <= 0) {
                        String asInfo = nearbyArmorStand != null ?
                                " (with AS: '" + nearbyArmorStand.getCustomNameTag() + "')" : " (no AS)";
                        chat("Found invisible Shadow Assassin" + asInfo);
                    }
                }
            }
        }


        if (debugMode && starredMobs.size() != oldSize) {
            chat("Now tracking " + starredMobs.size() + " starred mobs (was: " + oldSize + ")");
            debugCooldown = 100;
        }
    }

    private EntityLivingBase findClosestMob(EntityArmorStand armorStand, List<EntityLivingBase> mobs) {
        EntityLivingBase closest = null;
        double closestDist = Double.MAX_VALUE;
        double maxHorizontalDist = 3.0;
        double maxVerticalDist = 5.0;

        for (EntityLivingBase mob : mobs) {
            double dx = mob.posX - armorStand.posX;
            double dz = mob.posZ - armorStand.posZ;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);

            double dy = Math.abs(armorStand.posY - mob.posY);

            if (horizontalDist < maxHorizontalDist && dy < maxVerticalDist) {
                double totalDist = horizontalDist + dy;
                if (totalDist < closestDist) {
                    closestDist = totalDist;
                    closest = mob;
                }
            }
        }

        return closest;
    }

    private EntityArmorStand findClosestArmorStand(EntityLivingBase mob, List<EntityArmorStand> armorStands) {
        EntityArmorStand closest = null;
        double closestDist = Double.MAX_VALUE;
        double maxHorizontalDist = 2.0;
        double maxVerticalDist = 3.0;

        for (EntityArmorStand as : armorStands) {
            double dx = mob.posX - as.posX;
            double dz = mob.posZ - as.posZ;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);

            double dy = Math.abs(as.posY - mob.posY);

            if (horizontalDist < maxHorizontalDist && dy < maxVerticalDist) {
                double totalDist = horizontalDist + dy;
                if (totalDist < closestDist) {
                    closestDist = totalDist;
                    closest = as;
                }
            }
        }

        return closest;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!ModConfig.starredMobEspEnabled) return;
        if (!ModConfig.allFeaturesEnabled) return;
        if (starredMobs.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        double playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
        double playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
        double playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-playerX, -playerY, -playerZ);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glLineWidth(3.0F);

        int[] rgb = hueToRgb(ModConfig.espColorHue);

        for (Map.Entry<EntityLivingBase, EntityArmorStand> entry : starredMobs.entrySet()) {
            EntityLivingBase mob = entry.getKey();

            if (mob.isDead) continue;

            double x = mob.lastTickPosX + (mob.posX - mob.lastTickPosX) * event.partialTicks;
            double y = mob.lastTickPosY + (mob.posY - mob.lastTickPosY) * event.partialTicks;
            double z = mob.lastTickPosZ + (mob.posZ - mob.lastTickPosZ) * event.partialTicks;

            double width = mob.width / 2.0;
            double height = mob.height;

            drawBox(x - width, y, z - width,
                    x + width, y + height, z + width,
                    rgb[0], rgb[1], rgb[2], 200);

            drawLine(x, y, z, x, y + height + 1.0, z, rgb[0], rgb[1], rgb[2], 255);
        }

        GL11.glLineWidth(1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
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

    private void drawBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                         int r, int g, int b, int a) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        // Bottom face
        worldRenderer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();

        // Top face
        worldRenderer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();

        // Vertical edges
        worldRenderer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        tessellator.draw();
    }

    private void drawLine(double x1, double y1, double z1, double x2, double y2, double z2,
                          int r, int g, int b, int a) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        worldRenderer.pos(x2, y2, z2).color(r, g, b, a).endVertex();
        tessellator.draw();
    }

    public void toggleDebug() {
        debugMode = !debugMode;
        chat("Debug mode: " + (debugMode ? "ON" : "OFF"));
    }

    public void reset() {
        starredMobs.clear();
        scanCooldown = 0;
        debugCooldown = 0;
    }
}