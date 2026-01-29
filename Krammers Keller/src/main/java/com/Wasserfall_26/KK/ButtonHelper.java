package com.Wasserfall_26.KK;

import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class ButtonHelper {

    private static final ButtonHelper INSTANCE = new ButtonHelper();
    private int debugCounter = 0;
    private MovingObjectPosition customHit = null;

    public static ButtonHelper getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!ModConfig.buttonHelperEnabled) return;
        if (!ModConfig.allFeaturesEnabled) return;

        debugCounter++;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            customHit = null;
            return;
        }


        customHit = findButtonOrLeverWithExtendedHitbox(mc, 5.0);

        if (customHit != null) {
            mc.objectMouseOver = customHit;

            if (debugCounter % 20 == 0 && ModConfig.debugMode) {
                DebugUtils.debug("Button/Lever found at: " + customHit.getBlockPos());
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (!ModConfig.buttonHelperEnabled) return;
        if (!ModConfig.allFeaturesEnabled) return;
        if (event.button != 1) return;
        if (!event.buttonstate) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (customHit == null) return;

        BlockPos pos = customHit.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();

        if (block instanceof BlockButton || block instanceof BlockLever) {
            event.setCanceled(true);

            EnumFacing facing = getAttachedFace(block, mc.theWorld.getBlockState(pos));
            if (facing != null) {
                if (mc.playerController.onPlayerRightClick(
                        mc.thePlayer,
                        mc.theWorld,
                        mc.thePlayer.getHeldItem(),
                        pos,
                        facing.getOpposite(),
                        customHit.hitVec
                )) {
                    mc.thePlayer.swingItem();

                    if (ModConfig.debugMode) {
                        DebugUtils.debug("Clicked button/lever at: " + pos);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (!ModConfig.buttonHelperEnabled) return;
        if (!ModConfig.allFeaturesEnabled) return;
        if (customHit == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        BlockPos pos = customHit.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();

        if (block instanceof BlockButton || block instanceof BlockLever) {
            event.setCanceled(true);

            EnumFacing facing = getAttachedFace(block, mc.theWorld.getBlockState(pos));
            if (facing != null) {
                AxisAlignedBB box = create1x1FlatHitbox(pos, facing);
                if (box != null) {
                    drawSelectionBox(event.player, box, event.partialTicks);
                }
            }
        }
    }

    private void drawSelectionBox(EntityPlayer player, AxisAlignedBB box, float partialTicks) {
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        AxisAlignedBB aabb = box.expand(0.002D, 0.002D, 0.002D).offset(-x, -y, -z);

        RenderGlobal.drawSelectionBoundingBox(aabb);

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private MovingObjectPosition findButtonOrLeverWithExtendedHitbox(Minecraft mc, double reach) {
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 lookVec = mc.thePlayer.getLook(1.0F);
        Vec3 targetVec = eyePos.addVector(
                lookVec.xCoord * reach,
                lookVec.yCoord * reach,
                lookVec.zCoord * reach
        );

        int minX = (int) Math.floor(Math.min(eyePos.xCoord, targetVec.xCoord)) - 1;
        int maxX = (int) Math.ceil(Math.max(eyePos.xCoord, targetVec.xCoord)) + 1;
        int minY = (int) Math.floor(Math.min(eyePos.yCoord, targetVec.yCoord)) - 1;
        int maxY = (int) Math.ceil(Math.max(eyePos.yCoord, targetVec.yCoord)) + 1;
        int minZ = (int) Math.floor(Math.min(eyePos.zCoord, targetVec.zCoord)) - 1;
        int maxZ = (int) Math.ceil(Math.max(eyePos.zCoord, targetVec.zCoord)) + 1;

        MovingObjectPosition closestHit = null;
        double closestDist = Double.MAX_VALUE;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    Block block = mc.theWorld.getBlockState(checkPos).getBlock();

                    if (block instanceof BlockButton || block instanceof BlockLever) {
                        EnumFacing facing = getAttachedFace(block, mc.theWorld.getBlockState(checkPos));
                        if (facing == null) continue;

                        AxisAlignedBB extendedBB = create1x1FlatHitbox(checkPos, facing);

                        if (extendedBB != null) {
                            MovingObjectPosition hit = extendedBB.calculateIntercept(eyePos, targetVec);

                            if (hit != null) {
                                double dist = eyePos.distanceTo(hit.hitVec);

                                if (dist < closestDist) {
                                    closestDist = dist;
                                    closestHit = new MovingObjectPosition(
                                            hit.hitVec,
                                            facing.getOpposite(),
                                            checkPos
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        return closestHit;
    }

    private AxisAlignedBB create1x1FlatHitbox(BlockPos buttonPos, EnumFacing attachedFace) {
        double minX, minY, minZ, maxX, maxY, maxZ;
        double thickness = 0.0625;

        switch (attachedFace) {
            case DOWN:
                minX = buttonPos.getX();
                maxX = buttonPos.getX() + 1;
                minY = buttonPos.getY();
                maxY = buttonPos.getY() + thickness;
                minZ = buttonPos.getZ();
                maxZ = buttonPos.getZ() + 1;
                break;

            case UP:
                minX = buttonPos.getX();
                maxX = buttonPos.getX() + 1;
                minY = buttonPos.getY() + 1 - thickness;
                maxY = buttonPos.getY() + 1;
                minZ = buttonPos.getZ();
                maxZ = buttonPos.getZ() + 1;
                break;

            case NORTH:
                minX = buttonPos.getX();
                maxX = buttonPos.getX() + 1;
                minY = buttonPos.getY();
                maxY = buttonPos.getY() + 1;
                minZ = buttonPos.getZ();
                maxZ = buttonPos.getZ() + thickness;
                break;

            case SOUTH:
                minX = buttonPos.getX();
                maxX = buttonPos.getX() + 1;
                minY = buttonPos.getY();
                maxY = buttonPos.getY() + 1;
                minZ = buttonPos.getZ() + 1 - thickness;
                maxZ = buttonPos.getZ() + 1;
                break;

            case WEST:
                minX = buttonPos.getX();
                maxX = buttonPos.getX() + thickness;
                minY = buttonPos.getY();
                maxY = buttonPos.getY() + 1;
                minZ = buttonPos.getZ();
                maxZ = buttonPos.getZ() + 1;
                break;

            case EAST:
                minX = buttonPos.getX() + 1 - thickness;
                maxX = buttonPos.getX() + 1;
                minY = buttonPos.getY();
                maxY = buttonPos.getY() + 1;
                minZ = buttonPos.getZ();
                maxZ = buttonPos.getZ() + 1;
                break;

            default:
                return null;
        }

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private EnumFacing getAttachedFace(Block block, net.minecraft.block.state.IBlockState state) {
        try {
            if (block instanceof BlockButton) {
                Object facingValue = state.getValue(BlockButton.FACING);
                if (facingValue instanceof EnumFacing) {
                    return ((EnumFacing) facingValue).getOpposite();
                }
            } else if (block instanceof BlockLever) {
                Object facingValue = state.getValue(BlockLever.FACING);
                String orientationName = facingValue.toString().toUpperCase();

                if (orientationName.equals("DOWN_X") || orientationName.equals("DOWN_Z")) {
                    return EnumFacing.DOWN;
                } else if (orientationName.equals("UP_X") || orientationName.equals("UP_Z")) {
                    return EnumFacing.UP;
                } else if (orientationName.equals("NORTH")) {
                    return EnumFacing.SOUTH;
                } else if (orientationName.equals("SOUTH")) {
                    return EnumFacing.NORTH;
                } else if (orientationName.equals("WEST")) {
                    return EnumFacing.EAST;
                } else if (orientationName.equals("EAST")) {
                    return EnumFacing.WEST;
                }
            }
        } catch (Exception e) {
            if (ModConfig.debugMode) {
                DebugUtils.error("Error getting attached face: " + e.getMessage());
            }
        }

        return null;
    }
}