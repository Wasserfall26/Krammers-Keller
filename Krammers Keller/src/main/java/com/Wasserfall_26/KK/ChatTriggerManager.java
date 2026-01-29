package com.Wasserfall_26.KK;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class ChatTriggerManager {

    private static final ChatTriggerManager INSTANCE = new ChatTriggerManager();
    private final Map<String, TriggerData> triggers = new LinkedHashMap<>();
    private final Queue<String> messageQueue = new LinkedList<>();
    private int tickDelay = 0;
    private boolean processingTrigger = false;

    public static ChatTriggerManager getInstance() {
        return INSTANCE;
    }

    public static class TriggerData {
        public String trigger;
        public String response;
        public boolean enabled;

        public TriggerData(String trigger, String response, boolean enabled) {
            this.trigger = trigger;
            this.response = response;
            this.enabled = enabled;
        }
    }

    public void addTrigger(String trigger, String response) {
        triggers.put(trigger.toLowerCase(), new TriggerData(trigger.toLowerCase(), response, true));
        saveTriggers();
    }

    public void removeTrigger(String trigger) {
        triggers.remove(trigger.toLowerCase());
        saveTriggers();
    }


    public void toggleTrigger(String trigger) {
        TriggerData data = triggers.get(trigger.toLowerCase());
        if (data != null) {
            data.enabled = !data.enabled;
            saveTriggers();
        }
    }


    public Map<String, TriggerData> getTriggers() {
        return new LinkedHashMap<>(triggers);
    }


    public void clearTriggers() {
        triggers.clear();
        saveTriggers();
    }

    public void loadTriggers() {
        triggers.clear();
        Map<String, TriggerData> loaded = KK.config.loadTriggers();
        if (loaded != null) {
            triggers.putAll(loaded);
        }
    }


    private void saveTriggers() {
        KK.config.saveTriggers(triggers);
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!ModConfig.chatTriggersEnabled) return;
        if (processingTrigger) return;

        String message = event.message.getUnformattedText().toLowerCase();


        for (Map.Entry<String, TriggerData> entry : triggers.entrySet()) {
            TriggerData data = entry.getValue();
            if (data.enabled && message.contains(entry.getKey())) {
                messageQueue.add(entry.getValue().response);
                break;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!ModConfig.chatTriggersEnabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }


        if (!messageQueue.isEmpty()) {
            String msg = messageQueue.poll();
            processingTrigger = true;
            mc.thePlayer.sendChatMessage(msg);
            tickDelay = 20;


            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    processingTrigger = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}