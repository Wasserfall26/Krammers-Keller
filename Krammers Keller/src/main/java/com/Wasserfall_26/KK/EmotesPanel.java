package com.Wasserfall_26.KK;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

public class EmotesPanel {

    private int scrollOffset = 0;
    private static final int EMOTES_PER_PAGE = 6;
    private List<EmoteEntry> allEmotes = new ArrayList<>();
    private String copiedFeedback = "";
    private long copiedTime = 0;

    public EmotesPanel() {
        initEmotes();
    }

    private void initEmotes() {

        allEmotes.add(new EmoteEntry("Tableflip", "(╯°□°)╯︵ ┻━┻"));
        allEmotes.add(new EmoteEntry("Cry", "(╥﹏╥)"));
        allEmotes.add(new EmoteEntry("Disapprove", "ಠ_ಠ"));
        allEmotes.add(new EmoteEntry("Surprised", "(⊙_⊙)"));
        allEmotes.add(new EmoteEntry("Fight Me", "ᕦ(ò_óˇ)ᕤ"));
        allEmotes.add(new EmoteEntry("Disgusted", "(¬_¬)"));
        allEmotes.add(new EmoteEntry("Gimme", "༼ つ ◕_◕ ༽つ"));
        allEmotes.add(new EmoteEntry("Happy", "(◕‿◕)"));
        allEmotes.add(new EmoteEntry("Happy 2", "ヽ(•‿•)ノ"));
        allEmotes.add(new EmoteEntry("Sad", "(╯︵╰,)"));
        allEmotes.add(new EmoteEntry("Müde", "(=_=)"));
        allEmotes.add(new EmoteEntry("In Love", "(♥ω♥)"));
        allEmotes.add(new EmoteEntry("Cool", "(⌐■_■)"));
        allEmotes.add(new EmoteEntry("Hug", "(づ｡◕‿‿◕｡)づ"));
        allEmotes.add(new EmoteEntry("Hug 2", "⊂(◉‿◉)つ"));
        allEmotes.add(new EmoteEntry("Shocked", "ヽ(°〇°)ﾉ"));
        allEmotes.add(new EmoteEntry("Dead", "(✖╭╮✖)"));
        allEmotes.add(new EmoteEntry("Bear", "ʕ•ᴥ•ʔ"));
        allEmotes.add(new EmoteEntry("Cat", "(=^･ω･^=)"));
        allEmotes.add(new EmoteEntry("Content", "＜(^_^)＞"));
        allEmotes.add(new EmoteEntry("Disco", "ヾ(⌐■_■)ノ♪"));
        allEmotes.add(new EmoteEntry("Dance", "♪┏(・o･)┛♪"));
        allEmotes.add(new EmoteEntry("Music", "♫♪♫"));
        allEmotes.add(new EmoteEntry("Lightning", "⚡"));
        allEmotes.add(new EmoteEntry("Crown", "♛"));
        allEmotes.add(new EmoteEntry("Sword", "⚔"));
        allEmotes.add(new EmoteEntry("Diamond", "◇"));
        allEmotes.add(new EmoteEntry("Heart", "❤"));
        allEmotes.add(new EmoteEntry("Arrow", "→"));
        allEmotes.add(new EmoteEntry("Check", "✓"));
        allEmotes.add(new EmoteEntry("X", "✗"));
        allEmotes.add(new EmoteEntry("Rifle", "︻デ═一"));
        allEmotes.add(new EmoteEntry("Rifle 2", "︻╦╤─"));
        allEmotes.add(new EmoteEntry("Sniper", "▄︻̷̿┻̿═━一"));



    }

    public void handleScroll(int wheel) {
        if (wheel > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else {
            int maxScroll = Math.max(0, allEmotes.size() - EMOTES_PER_PAGE);
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
    }

    public void handleClick(int mouseX, int mouseY, int contentStartX, int startY, int contentWidth) {
        int entryHeight = 70;
        int entrySpacing = 75;

        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + EMOTES_PER_PAGE, allEmotes.size());

        for (int i = startIndex; i < endIndex; i++) {
            int index = i - startIndex;
            int y = startY + (index * entrySpacing);

            if (mouseX >= contentStartX && mouseX <= contentStartX + contentWidth &&
                    mouseY >= y && mouseY <= y + entryHeight) {
                copyToClipboard(allEmotes.get(i).emote);
                break;
            }
        }
    }

    private void copyToClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(text), null);
            copiedFeedback = "§a✓ Copied!";
            copiedTime = System.currentTimeMillis();
        } catch (Exception e) {
            copiedFeedback = "§c✗ Failed!";
            copiedTime = System.currentTimeMillis();
        }
    }

    public void draw(GuiScreen screen, int mouseX, int mouseY, int contentStartX, int headerHeight, int contentWidth, int screenWidth, int screenHeight) {
        int startY = headerHeight + 82;
        int entryHeight = 70;
        int entrySpacing = 75;

        screen.drawCenteredString(screen.mc.fontRendererObj, "§7Click on the Emote to Copy it",
                screenWidth / 2 + 210, headerHeight + 25, 0xFFAAAAAA);

        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + EMOTES_PER_PAGE, allEmotes.size());

        for (int i = startIndex; i < endIndex; i++) {
            EmoteEntry emote = allEmotes.get(i);
            int index = i - startIndex;
            int y = startY + (index * entrySpacing);

            boolean hovered = mouseX >= contentStartX && mouseX <= contentStartX + contentWidth &&
                    mouseY >= y && mouseY <= y + entryHeight;

            int bgColor = hovered ? 0xFF1a1a1a : 0xFF121212;
            screen.drawRect(contentStartX, y, contentStartX + contentWidth, y + entryHeight, bgColor);

            if (hovered) {
                screen.drawRect(contentStartX, y, contentStartX + contentWidth, y + 2, 0xFFFFaa00);
                screen.drawRect(contentStartX, y + entryHeight - 2, contentStartX + contentWidth, y + entryHeight, 0xFFFFaa00);
            }

            GlStateManager.pushMatrix();
            GlStateManager.scale(1.5f, 1.5f, 1.5f);
            screen.drawString(screen.mc.fontRendererObj, "§f" + emote.name,
                    (int)((contentStartX + 15) / 1.5f), (int)((y + 20) / 1.5f), 0xFFFFFFFF);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.scale(2.5f, 2.5f, 2.5f);
            String displayEmote = emote.emote.length() > 20 ? emote.emote.substring(0, 20) + "..." : emote.emote;
            screen.drawString(screen.mc.fontRendererObj, "§e" + displayEmote,
                    (int)((contentStartX + contentWidth - 450) / 2.5f), (int)((y + 18) / 2.5f), 0xFFFFFFFF);
            GlStateManager.popMatrix();

            if (hovered) {
                screen.drawString(screen.mc.fontRendererObj, "§7» Click «",
                        contentStartX + contentWidth - 100, y + 28, 0xFFAAAAAA);
            }
        }

        if (allEmotes.size() > EMOTES_PER_PAGE) {
            String scrollInfo = String.format("§7%d-%d / %d", startIndex + 1, endIndex, allEmotes.size());
            screen.drawCenteredString(screen.mc.fontRendererObj, scrollInfo,
                    screenWidth / 2 + 210, screenHeight - 70, 0xFFAAAAAA);
        }

        if (System.currentTimeMillis() - copiedTime < 2000) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            screen.drawCenteredString(screen.mc.fontRendererObj, copiedFeedback,
                    (screenWidth / 2 + 210) / 2, (screenHeight - 100) / 2, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }
    }

    public void reset() {
        scrollOffset = 0;
    }

    private static class EmoteEntry {
        String name;
        String emote;

        public EmoteEntry(String name, String emote) {
            this.name = name;
            this.emote = emote;
        }
    }
}