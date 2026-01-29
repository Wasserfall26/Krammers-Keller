package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;

public class PlayerJoinListener {

    private final Set<String> checkedPlayers = new HashSet<>();
    private int tickCounter = 0;
    private int initialCheckDelay = 0;
    private boolean worldLoaded = false;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {

        if (event.world.isRemote && event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entity;

            Minecraft mc = Minecraft.getMinecraft();

            if (mc.thePlayer != null && player.getUniqueID().equals(mc.thePlayer.getUniqueID())) {
                worldLoaded = true;
                initialCheckDelay = 40;
                return;
            }

            String uuid = player.getUniqueID().toString();


            if (!checkedPlayers.contains(uuid)) {
                checkedPlayers.add(uuid);
                CapeAPIClient.getInstance().checkPlayerHasMod(uuid);
            }
        }
    }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;

        if (worldLoaded && initialCheckDelay > 0) {
            initialCheckDelay--;
            if (initialCheckDelay == 0) {
                CapeManager.getInstance().tryInitialRegistration();
                worldLoaded = false;
            }
        }

        if (tickCounter >= 100) {
            tickCounter = 0;
            scanNearbyPlayers();
        }
    }

    private void scanNearbyPlayers() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player.getUniqueID().equals(mc.thePlayer.getUniqueID())) {
                continue;
            }

            String uuid = player.getUniqueID().toString();

            if (!checkedPlayers.contains(uuid)) {
                checkedPlayers.add(uuid);
                CapeAPIClient.getInstance().checkPlayerHasMod(uuid);
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world.isRemote) {
            clearCache();
            CapeAPIClient.getInstance().clearCache();
            CapeManager.getInstance().resetRegistration();
            worldLoaded = false;
            initialCheckDelay = 0;
        }
    }

    public void clearCache() {
        checkedPlayers.clear();
    }
}