package io.redspace.ironsspellbooks.player;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import io.redspace.ironsspellbooks.network.casting.CastPacket;
import io.redspace.ironsspellbooks.network.casting.QuickCastPacket;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static io.redspace.ironsspellbooks.player.KeyMappings.*;

@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ClientInputEvents {
    private static int useKeyId = Integer.MIN_VALUE;
    public static boolean isUseKeyDown;
    public static boolean hasReleasedSinceCasting;
    public static boolean isShiftKeyDown;

    @SubscribeEvent
    public static void clientMouseScrolled(InputEvent.MouseScrollingEvent event) {
        Player player = MinecraftInstanceHelper.getPlayer();
        if (player == null)
            return;

        if (SPELLBAR_SCROLL_MODIFIER_KEYMAP.isDown()) {
            int direction = Mth.clamp((int) event.getScrollDeltaY(), -1, 1);
            if (handleSpellBarScrollModifier(direction)) {
                event.setCanceled(true);
            }
        }
    }

    /// Handles spell bar modifier scrolling to change the currently selected spell.
    /// Triggered by holding a modifier key and then scrolling with the mouse.
    /// Extracted for modularity without assuming mouse-specific input,
    /// allowing other mods to provide controller or alternative input sources.
    ///
    /// **Note:** This is an internal API, breaking changes may occur in future versions.
    ///
    /// @return Whether the scrolling action was consumed
    public static boolean handleSpellBarScrollModifier(int direction) {
        SpellSelectionManager spellSelectionManager = ClientMagicData.getSpellSelectionManager();
        if (spellSelectionManager.getSpellCount() <= 0) {
            return false;
        }
        List<SpellSelectionManager.SelectionOption> spellbookSpells = spellSelectionManager.getAllSpells();
        int spellCount = spellbookSpells.size();
        int scrollIndex = (Mth.clamp(spellSelectionManager.getSelectionIndex(), 0, spellCount) - direction);
        int selectedIndex = (Mth.clamp(scrollIndex, -1, spellCount + 1) + spellCount) % spellCount;
        spellSelectionManager.makeSelection(selectedIndex);
        return true;
    }

    @SubscribeEvent
    public static void onUseInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isUseItem()) {
            if (ClientSpellCastHelper.shouldSuppressRightClicks()) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        } else if (event.isAttack()) {
            if (ClientMagicData.isCasting()) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!FMLLoader.isProduction()) {
            if (event.getKey() == InputConstants.KEY_NUMPAD9 && event.getAction() == InputConstants.PRESS) {
                IronsSpellbooks.LOGGER.debug("breakpoint");
            }
        }
        handleInputEvent(event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        handleInputEvent(event.getButton(), event.getAction());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        handleKeybinds();
    }

    /// Tracks the previous [KeyMapping#isDown()] state for [KeyMappings#SPELL_WHEEL_KEYMAP].
    private static boolean wasSpellWheelDown = false;

    /// Called in every client tick to handle the vanilla [KeyMapping].
    /// Similar to [Minecraft#handleKeybinds()] but for the mod's keybinds.
    private static void handleKeybinds() {
        while (SPELLBOOK_CAST_ACTIVE_KEYMAP.consumeClick()) {
            PacketDistributor.sendToServer(new CastPacket());
        }

        while (SPELL_WHEEL_KEYMAP.consumeClick()) {
            SpellWheelOverlay.instance.open();
        }

        handleSpellWheelRelease();

        while (SPELL_WHEEL_TOGGLE_KEYMAP.consumeClick()) {
            if (SpellWheelOverlay.instance.active) {
                SpellWheelOverlay.instance.close();
            } else {
                SpellWheelOverlay.instance.open();
            }
        }

        for (int i = 0; i < QUICK_CAST_MAPPINGS.size(); i++) {
            if (QUICK_CAST_MAPPINGS.get(i).consumeClick()) {
                PacketDistributor.sendToServer(new QuickCastPacket(i));
                break;
            }
        }

        if (SPELLBAR_SCROLL_MODIFIER_KEYMAP.isDown()) {
            if (ClientConfigs.SPELL_BAR_DISPLAY.get().equals(ManaBarOverlay.Display.Contextual)) {
                SpellBarOverlay.fadeoutDelay = 40;
            }
        }
    }

    private static void handleSpellWheelRelease() {
        final boolean isDown = SPELL_WHEEL_KEYMAP.isDown();

        final boolean wasReleased = wasSpellWheelDown && !isDown;
        if (wasReleased && SpellWheelOverlay.instance.active) {
            SpellWheelOverlay.instance.close();
        }

        wasSpellWheelDown = isDown;
    }

    private static void handleInputEvent(int button, int action) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        handleRightClickSuppression(button, action);
        if (button == InputConstants.KEY_LSHIFT) {
            isShiftKeyDown = action >= InputConstants.PRESS;
        }
    }

    private static void handleRightClickSuppression(int button, int action) {
        if (useKeyId == Integer.MIN_VALUE) {
            useKeyId = Minecraft.getInstance().options.keyUse.getKey().getValue();
        }

        if (button == useKeyId) {
            if (action == InputConstants.RELEASE) {
                ClientSpellCastHelper.setSuppressRightClicks(false);
                isUseKeyDown = false;
                hasReleasedSinceCasting = true;
            } else if (action == InputConstants.PRESS) {
                isUseKeyDown = true;
            }
        }
    }
}
