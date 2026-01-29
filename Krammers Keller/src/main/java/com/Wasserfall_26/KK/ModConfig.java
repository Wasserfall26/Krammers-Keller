package com.Wasserfall_26.KK;

import net.minecraftforge.common.config.Configuration;
import java.io.File;
import java.util.*;

public class ModConfig {
    private Configuration config;

    public static boolean allFeaturesEnabled = true;
    private static Map<String, Boolean> previousStates = new HashMap<>();

    public static boolean chatSwitcherEnabled = false;
    public static String currentChatMode = "all";

    public static boolean chatTriggersEnabled = false;

    public static boolean autoWishEnabled = false;
    public static boolean autoWishOnLowHpEnabled = false;
    public static float lowHpThreshold = 20.0f;
    public static boolean bloodRoomProgressEnabled = false;
    public static boolean witherKeyTracerEnabled = false;

    public static boolean capeEnabled = true;

    public static boolean buttonHelperEnabled = false;

    public static boolean starredMobEspEnabled = false;
    public static float espColorHue = 0.0f;

    public static boolean debugMode = false;

    public ModConfig(File configFile) {
        config = new Configuration(configFile);
    }

    public void loadConfig() {
        config.load();

        allFeaturesEnabled = config.getBoolean("All Features Enabled", Configuration.CATEGORY_GENERAL, true, "Master toggle for all features");

        chatSwitcherEnabled = config.getBoolean("Chat Switcher Enabled", "Extras", false, "Enable Chat Switcher");
        currentChatMode = config.getString("Current Chat Mode", "Extras", "all", "Current chat mode (all/party)");

        chatTriggersEnabled = config.getBoolean("Chat Triggers Enabled", "Extras", false, "Enable Chat Triggers");

        autoWishEnabled = config.getBoolean("Auto Wish Enabled", "Dungeons", false, "Automatically press Q on Maxor enrage or party wish request");
        autoWishOnLowHpEnabled = config.getBoolean("Auto Wish on Low HP Enabled", "Dungeons", false, "Automatically send /pc wish when below HP threshold");
        lowHpThreshold = config.getFloat("Low HP Threshold", "Dungeons", 20.0f, 1.0f, 99.0f, "HP threshold in percent to trigger auto wish");

        bloodRoomProgressEnabled = config.getBoolean("Blood Room Progress Enabled", "Dungeons", false, "Shows remaining mobs in Blood Room as custom bossbar");

        witherKeyTracerEnabled = config.getBoolean("Wither Key Tracer Enabled", "Dungeons", false, "Shows lines to wither keys in dungeons");

        capeEnabled = config.getBoolean("Cape Enabled", Configuration.CATEGORY_GENERAL, true, "Show custom cape");

        buttonHelperEnabled = config.getBoolean("Button Helper Enabled", "Extras", false, "Makes button and lever hitboxes the size of the block they're attached to");

        starredMobEspEnabled = config.getBoolean("Starred Mob ESP Enabled", "Extras", false, "Highlights starred mobs in dungeons");
        espColorHue = config.getFloat("ESP Color Hue", "Extras", 0.0f, 0.0f, 300.0f, "Color hue for ESP boxes (0=Red, 60=Yellow, 120=Green, 180=Cyan, 240=Blue, 300=Purple)");

        debugMode = config.getBoolean("Debug Mode", Configuration.CATEGORY_GENERAL, false, "Shows debug messages in chat");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public void saveConfig() {
        config.get(Configuration.CATEGORY_GENERAL, "All Features Enabled", true).set(allFeaturesEnabled);

        config.get("Extras", "Chat Switcher Enabled", false).set(chatSwitcherEnabled);
        config.get("Extras", "Current Chat Mode", "all").set(currentChatMode);
        config.get("Extras", "Chat Triggers Enabled", false).set(chatTriggersEnabled);

        config.get("Dungeons", "Auto Wish Enabled", false).set(autoWishEnabled);
        config.get("Dungeons", "Auto Wish on Low HP Enabled", false).set(autoWishOnLowHpEnabled);
        config.get("Dungeons", "Low HP Threshold", 20.0f).set(lowHpThreshold);

        config.get("Dungeons", "Blood Room Progress Enabled", false).set(bloodRoomProgressEnabled);

        config.get("Dungeons", "Wither Key Tracer Enabled", false).set(witherKeyTracerEnabled);

        config.get(Configuration.CATEGORY_GENERAL, "Cape Enabled", true).set(capeEnabled);

        config.get("Extras", "Button Helper Enabled", false).set(buttonHelperEnabled);

        config.get("Extras", "Starred Mob ESP Enabled", false).set(starredMobEspEnabled);
        config.get("Extras", "ESP Color Hue", 0.0f).set(espColorHue);

        config.get(Configuration.CATEGORY_GENERAL, "Debug Mode", false).set(debugMode);

        config.save();
    }


    public static void toggleAllFeatures(boolean enabled) {
        if (!enabled) {
            previousStates.clear();
            previousStates.put("chatSwitcher", chatSwitcherEnabled);
            previousStates.put("chatTriggers", chatTriggersEnabled);
            previousStates.put("autoWish", autoWishEnabled);
            previousStates.put("autoWishLowHp", autoWishOnLowHpEnabled);
            previousStates.put("bloodRoom", bloodRoomProgressEnabled);
            previousStates.put("witherKey", witherKeyTracerEnabled);
            previousStates.put("cape", capeEnabled);
            previousStates.put("buttonHelper", buttonHelperEnabled);
            previousStates.put("starredMob", starredMobEspEnabled);

            chatSwitcherEnabled = false;
            chatTriggersEnabled = false;
            autoWishEnabled = false;
            autoWishOnLowHpEnabled = false;
            bloodRoomProgressEnabled = false;
            witherKeyTracerEnabled = false;
            capeEnabled = false;
            buttonHelperEnabled = false;
            starredMobEspEnabled = false;
        } else {
            chatSwitcherEnabled = previousStates.getOrDefault("chatSwitcher", false);
            chatTriggersEnabled = previousStates.getOrDefault("chatTriggers", false);
            autoWishEnabled = previousStates.getOrDefault("autoWish", false);
            autoWishOnLowHpEnabled = previousStates.getOrDefault("autoWishLowHp", false);
            bloodRoomProgressEnabled = previousStates.getOrDefault("bloodRoom", false);
            witherKeyTracerEnabled = previousStates.getOrDefault("witherKey", false);
            capeEnabled = previousStates.getOrDefault("cape", true);
            buttonHelperEnabled = previousStates.getOrDefault("buttonHelper", false);
            starredMobEspEnabled = previousStates.getOrDefault("starredMob", false);
        }

        allFeaturesEnabled = enabled;
    }


    public void saveTriggers(Map<String, ChatTriggerManager.TriggerData> triggers) {
        try {
            config.load();

            if (config.hasCategory("ChatTriggers")) {
                config.removeCategory(config.getCategory("ChatTriggers"));
            }
            if (config.hasCategory("chattriggers")) {
                config.removeCategory(config.getCategory("chattriggers"));
            }

            int i = 0;
            for (Map.Entry<String, ChatTriggerManager.TriggerData> entry : triggers.entrySet()) {
                ChatTriggerManager.TriggerData data = entry.getValue();
                config.get("chattriggers", "trigger_" + i, "").set(data.trigger);
                config.get("chattriggers", "response_" + i, "").set(data.response);
                config.get("chattriggers", "enabled_" + i, true).set(data.enabled);
                i++;
            }

            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.out.println("[KK] Error saving triggers: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public Map<String, ChatTriggerManager.TriggerData> loadTriggers() {
        Map<String, ChatTriggerManager.TriggerData> triggers = new LinkedHashMap<>();

        try {
            config.load();

            String category = config.hasCategory("chattriggers") ? "chattriggers" : "ChatTriggers";

            if (!config.hasCategory(category)) {
                System.out.println("[KK] No chat triggers category found");
                return triggers;
            }

            int i = 0;
            while (true) {
                String trigger = config.getString("trigger_" + i, category, "", "");
                String response = config.getString("response_" + i, category, "", "");
                boolean enabled = config.getBoolean("enabled_" + i, category, true, "");

                if (trigger.isEmpty() && response.isEmpty()) {
                    break;
                }

                if (!trigger.isEmpty()) {
                    triggers.put(trigger, new ChatTriggerManager.TriggerData(trigger, response, enabled));
                    System.out.println("[KK] Loaded trigger: " + trigger + " -> " + response + " (enabled: " + enabled + ")");
                }
                i++;
            }

            System.out.println("[KK] Total triggers loaded: " + triggers.size());
        } catch (Exception e) {
            System.out.println("[KK] Error loading triggers: " + e.getMessage());
            e.printStackTrace();
        }

        return triggers;
    }
}