package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class WitherKeyTracer {

    private static final WitherKeyTracer INSTANCE = new WitherKeyTracer();
    private List<Entity> witherKeys = new ArrayList<>();
    private int scanCooldown = 0;
    private boolean hasShownStartMessage = false;

    public static WitherKeyTracer getInstance() {
        return INSTANCE;
    }

    private void chat(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GOLD + "[KeyTracer] " + EnumChatFormatting.WHITE + message
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

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;



        if (!ModConfig.witherKeyTracerEnabled) return;
        if (!ModConfig.allFeaturesEnabled) return;

        boolean inDungeon = isInDungeon();

        if (!inDungeon) {
            if (!witherKeys.isEmpty()) {
                witherKeys.clear();
            }
            return;
        }

        if (scanCooldown > 0) {
            scanCooldown--;
            return;
        }

        scanCooldown = 2;
        scanForKeys();
    }

    private void scanForKeys() {
        Minecraft mc = Minecraft.getMinecraft();
        witherKeys.clear();

        for (Object obj : mc.theWorld.loadedEntityList) {
            Entity entity = (Entity) obj;
            double distance = mc.thePlayer.getDistanceToEntity(entity);

            if (distance > 50) continue;

            if (entity instanceof EntityArmorStand) {
                EntityArmorStand as = (EntityArmorStand) entity;

                if (as.hasCustomName()) {
                    String name = as.getCustomNameTag();
                    String cleanName = stripFormatting(name).toLowerCase();

                    if (cleanName.contains("wither key") || cleanName.contains("blood key")) {
                        witherKeys.add(entity);
                        continue;
                    }
                }

                for (int slot = 0; slot < 5; slot++) {
                    ItemStack stack = as.getEquipmentInSlot(slot);
                    if (stack != null) {

                        if (stack.getItem() == Item.getItemFromBlock(Blocks.skull)) {

                            if (stack.hasDisplayName()) {
                                String itemName = stripFormatting(stack.getDisplayName()).toLowerCase();
                                if (itemName.contains("key")) {
                                    witherKeys.add(entity);
                                    break;
                                }
                            } else {

                                if (stack.getMetadata() == 1) {
                                    witherKeys.add(entity);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            else if (entity instanceof EntityItem) {
                EntityItem item = (EntityItem) entity;
                ItemStack stack = item.getEntityItem();

                if (stack != null && stack.hasDisplayName()) {
                    String itemName = stripFormatting(stack.getDisplayName()).toLowerCase();
                    if (itemName.contains("wither key") || itemName.contains("blood key")) {
                        witherKeys.add(entity);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!ModConfig.witherKeyTracerEnabled) return;
        if (!ModConfig.allFeaturesEnabled) return;
        if (witherKeys.isEmpty()) return;

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

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        double eyeHeight = mc.thePlayer.getEyeHeight();

        for (Entity key : witherKeys) {
            if (key.isDead) continue;

            double keyX = key.lastTickPosX + (key.posX - key.lastTickPosX) * event.partialTicks;
            double keyY = key.lastTickPosY + (key.posY - key.lastTickPosY) * event.partialTicks;
            double keyZ = key.lastTickPosZ + (key.posZ - key.lastTickPosZ) * event.partialTicks;

            worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            worldRenderer.pos(playerX, playerY + eyeHeight, playerZ)
                    .color(255, 255, 0, 255).endVertex();

            worldRenderer.pos(keyX, keyY + 1.0, keyZ)
                    .color(255, 255, 0, 255).endVertex();

            tessellator.draw();

            drawBox(keyX - 0.4, keyY, keyZ - 0.4,
                    keyX + 0.4, keyY + 2.0, keyZ + 0.4,
                    255, 255, 0, 180);
        }

        GL11.glLineWidth(1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
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

    public void reset() {
        witherKeys.clear();
        scanCooldown = 0;
        hasShownStartMessage = false;
    }
}