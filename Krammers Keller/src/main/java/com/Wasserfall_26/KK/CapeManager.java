package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class CapeManager {

    private static final CapeManager INSTANCE = new CapeManager();

    private static final ResourceLocation CAPE_TEXTURE =
            new ResourceLocation("kk", "textures/cape.png");

    private boolean initialRegistrationDone = false;

    public static CapeManager getInstance() {
        return INSTANCE;
    }


    public void registerCapeLayer() {
        Minecraft mc = Minecraft.getMinecraft();

        RenderPlayer defaultRenderer =
                mc.getRenderManager().getSkinMap().get("default");
        RenderPlayer slimRenderer =
                mc.getRenderManager().getSkinMap().get("slim");

        if (defaultRenderer != null) {
            defaultRenderer.addLayer(new CustomCapeLayer(defaultRenderer));
        }

        if (slimRenderer != null) {
            slimRenderer.addLayer(new CustomCapeLayer(slimRenderer));
        }

        startHeartbeatTimer();
    }


    public void tryInitialRegistration() {
        if (!initialRegistrationDone) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                initialRegistrationDone = true;
                CapeAPIClient.getInstance().registerSelf();
            }
        }
    }

    public void resetRegistration() {
        initialRegistrationDone = false;
    }

    private void startHeartbeatTimer() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5 * 60 * 1000);
                    CapeAPIClient.getInstance().registerSelf();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "CapeAPI-Heartbeat").start();
    }


    public boolean shouldRenderCape(AbstractClientPlayer player) {
        if (!ModConfig.capeEnabled) return false;
        if (!ModConfig.allFeaturesEnabled) return false;
        if (player == null) return false;

        Minecraft mc = Minecraft.getMinecraft();


        if (mc.thePlayer != null && player.getUniqueID().equals(mc.thePlayer.getUniqueID())) {
            return true;
        }

        String uuid = player.getUniqueID().toString();

        CapeAPIClient.getInstance().checkPlayerHasMod(uuid);

        return CapeAPIClient.getInstance().shouldHaveCape(uuid);
    }

    private class CustomCapeLayer implements LayerRenderer<AbstractClientPlayer> {

        private final RenderPlayer playerRenderer;

        public CustomCapeLayer(RenderPlayer playerRenderer) {
            this.playerRenderer = playerRenderer;
        }

        @Override
        public void doRenderLayer(AbstractClientPlayer player,
                                  float limbSwing,
                                  float limbSwingAmount,
                                  float partialTicks,
                                  float ageInTicks,
                                  float netHeadYaw,
                                  float headPitch,
                                  float scale) {

            if (!shouldRenderCape(player)) return;
            if (!player.hasPlayerInfo()) return;
            if (player.isInvisible()) return;
            if (!player.isWearing(EnumPlayerModelParts.CAPE)) return;

            GlStateManager.color(1F, 1F, 1F, 1F);
            playerRenderer.bindTexture(CAPE_TEXTURE);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);

            double d0 = player.prevChasingPosX +
                    (player.chasingPosX - player.prevChasingPosX) * partialTicks
                    - (player.prevPosX +
                    (player.posX - player.prevPosX) * partialTicks);

            double d1 = player.prevChasingPosY +
                    (player.chasingPosY - player.prevChasingPosY) * partialTicks
                    - (player.prevPosY +
                    (player.posY - player.prevPosY) * partialTicks);

            double d2 = player.prevChasingPosZ +
                    (player.chasingPosZ - player.prevChasingPosZ) * partialTicks
                    - (player.prevPosZ +
                    (player.posZ - player.prevPosZ) * partialTicks);

            float yaw = player.prevRenderYawOffset +
                    (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;

            double sin = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            double cos = -MathHelper.cos(yaw * (float) Math.PI / 180.0F);

            float lift = (float) d1 * 10.0F;
            lift = MathHelper.clamp_float(lift, -6.0F, 32.0F);

            float sway = (float) (d0 * sin + d2 * cos) * 100.0F;
            float side = (float) (d0 * cos - d2 * sin) * 100.0F;

            if (sway < 0.0F) sway = 0.0F;

            if (player.isSneaking()) {
                lift += 25.0F;
            }

            float cameraYaw = player.prevCameraYaw +
                    (player.cameraYaw - player.prevCameraYaw) * partialTicks;

            lift += MathHelper.sin(
                    (player.prevDistanceWalkedModified +
                            (player.distanceWalkedModified -
                                    player.prevDistanceWalkedModified) * partialTicks)
                            * 6.0F) * 32.0F * cameraYaw;

            GlStateManager.rotate(6.0F + sway / 2.0F + lift, 1, 0, 0);
            GlStateManager.rotate(side / 2.0F, 0, 0, 1);
            GlStateManager.rotate(-side / 2.0F, 0, 1, 0);
            GlStateManager.rotate(180.0F, 0, 1, 0);

            playerRenderer.getMainModel().renderCape(0.0625F);

            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}