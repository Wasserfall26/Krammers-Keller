package com.Wasserfall_26.KK;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.client.ClientCommandHandler;

@Mod(modid = KK.MODID, version = KK.VERSION, name = KK.NAME, clientSideOnly = true)
public class KK {
    public static final String MODID = "kk";
    public static final String VERSION = "1.7";
    public static final String NAME = "Krammers Keller";

    public static ModConfig config;
    public static KeyBinding menuKey;
    public static ChatSwitcher chatSwitcher;
    public static ChatEventHandler chatEventHandler;
    public static DungeonFeatures dungeonFeatures;
    public static CapeManager capeManager;
    public static BloodRoomHandler bloodRoomHandler;
    public static BossBarInterceptor bossBarInterceptor;
    public static ButtonHelper buttonHelper;
    public static WitherKeyTracer witherKeyTracer;
    public static StarredMobESP starredMobESP;
    public static PlayerJoinListener playerJoinListener;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new ModConfig(event.getSuggestedConfigurationFile());
        config.loadConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        chatSwitcher = new ChatSwitcher();
        MinecraftForge.EVENT_BUS.register(chatSwitcher);

        chatEventHandler = new ChatEventHandler();
        MinecraftForge.EVENT_BUS.register(chatEventHandler);

        ChatTriggerManager triggerManager = ChatTriggerManager.getInstance();
        MinecraftForge.EVENT_BUS.register(triggerManager);
        triggerManager.loadTriggers();

        dungeonFeatures = DungeonFeatures.getInstance();
        MinecraftForge.EVENT_BUS.register(dungeonFeatures);

        capeManager = CapeManager.getInstance();
        capeManager.registerCapeLayer();

        playerJoinListener = new PlayerJoinListener();
        MinecraftForge.EVENT_BUS.register(playerJoinListener);

        bloodRoomHandler = BloodRoomHandler.getInstance();
        MinecraftForge.EVENT_BUS.register(bloodRoomHandler);

        bossBarInterceptor = BossBarInterceptor.getInstance();
        MinecraftForge.EVENT_BUS.register(bossBarInterceptor);

        buttonHelper = ButtonHelper.getInstance();
        MinecraftForge.EVENT_BUS.register(buttonHelper);

        witherKeyTracer = WitherKeyTracer.getInstance();
        MinecraftForge.EVENT_BUS.register(witherKeyTracer);

        starredMobESP = StarredMobESP.getInstance();
        MinecraftForge.EVENT_BUS.register(starredMobESP);

        menuKey = new KeyBinding("Open KK Settings", Keyboard.KEY_RSHIFT, "Krammers Keller");
        ClientRegistry.registerKeyBinding(menuKey);

        ClientCommandHandler.instance.registerCommand(new KKCommand());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (menuKey.isPressed()) {
            net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new GuiModSettings());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            KKCommand.onClientTick();
        }
    }
}