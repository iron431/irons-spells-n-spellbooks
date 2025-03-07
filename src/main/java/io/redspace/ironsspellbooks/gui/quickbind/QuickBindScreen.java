package io.redspace.ironsspellbooks.gui.quickbind;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ModListScreen;

public class QuickBindScreen extends Screen {
    private static final int ENTRY_HEIGHT = 25;
    private final Screen parent;
    private int scrollOffset = 0;
    private int maxScroll;
    private int numItems = 15;
    private String title = Component.translatable("screen.irons_spellbooks.quickbind").getString();

    //TODO: needs language support
    //TODO: check for existing key conflicts on bind
    //TODO: rebind dynamically
    //TODO: Deal with no active spellbook
    //TODO: Deal with no active spells

    public QuickBindScreen(Screen parent) {
        super(Component.literal("Quickbind Test"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        maxScroll = Math.max(0, numItems * ENTRY_HEIGHT - height + 40);

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> minecraft.setScreen(parent))
                .pos(width / 2 - 50, height - 30)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
        int centerX = scaledWidth / 2;
        int titleWidth = font.width(title);

        //Render title centered with proper scaling
        int yPos = 20 - scrollOffset;
        guiGraphics.drawString(font, title, centerX - titleWidth / 2, yPos, 0xFFFFFF);
        yPos += ENTRY_HEIGHT;

        var spells = ClientMagicData.getSpellSelectionManager().getAllSpells();

        for (int i = 0; i < spells.size(); i++) {
            if (yPos >= 0 && yPos < height - 40) {
                guiGraphics.drawString(font, spells.get(i).spellData.getDisplayName().getString(), 20, yPos, 0xFFFFFF);
            }
            yPos += ENTRY_HEIGHT;
        }

        //Render scrollbar (simple visual representation)
        if (maxScroll > 0) {
            int scrollBarHeight = (int) ((float) height / (numItems * ENTRY_HEIGHT) * height);
            int scrollBarY = (int) ((float) scrollOffset / maxScroll * (height - scrollBarHeight));
            guiGraphics.fill(width - 10, scrollBarY, width - 5, scrollBarY + scrollBarHeight, 0xFFAAAAAA);
        }

        //Render widgets (like the Done button)
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Handle scrolling
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (deltaY * 10)));
        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /*

package your.modid.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import com.mojang.blaze3d.platform.InputConstants;

public class ClientEvents {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Check for a trigger (e.g., pressing 'R' to remap)
        if (event.getKey() == 82 && event.getAction() == 1) { // 'R' key pressed
            KeyMapping keyToRemap = ClientSetup.MY_KEYBIND; // Or Minecraft.getInstance().options.keyJump
            InputConstants.Key newKey = InputConstants.getKey(75, -1); // GLFW key code for 'K'
            keyToRemap.setKey(newKey);

            // Optional: Update the keybind display name (not persisted)
            Minecraft.getInstance().options.setKey(keyToRemap, newKey);
        }
    }
}

    * */
}