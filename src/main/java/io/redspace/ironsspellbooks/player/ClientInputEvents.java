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
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static io.redspace.ironsspellbooks.player.KeyMappings.*;

@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientInputEvents {
    public static boolean hasReleasedSinceCasting;
    private static boolean showExpandedTooltip;

    @SubscribeEvent
    public static void clientMouseScrolled(InputEvent.MouseScrollingEvent event) {
        Player player = MinecraftInstanceHelper.getPlayer();
        if (player == null)
            return;

        if (SPELLBAR_SCROLL_MODIFIER_KEYMAP.isDown()) {
            int direction = Mth.clamp((int) event.getScrollDelta(), -1, 1);
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        handleKeybinds();
    }

    /// Tracks the previous [KeyMapping#isDown()] state for [KeyMappings#SPELL_WHEEL_KEYMAP].
    private static boolean wasSpellWheelDown = false;

    /// Called in every client tick to handle the vanilla [KeyMapping].
    /// Similar to [Minecraft#handleKeybinds()] but for the mod's keybinds.
    private static void handleKeybinds() {
        while (SPELLBOOK_CAST_ACTIVE_KEYMAP.consume()) {
            PacketDistributor.sendToServer(new CastPacket());
        }
        while (SPELL_WHEEL_KEYMAP.consume()) {
            // force user to let go of key before allowing hold keybind to trigger again
            if (!wasSpellWheelDown) {
                SpellWheelOverlay.instance.open();
            }
        }

        handleSpellWheelRelease();

        while (SPELL_WHEEL_TOGGLE_KEYMAP.consume()) {
            if (SpellWheelOverlay.instance.active) {
                SpellWheelOverlay.instance.close();
            } else {
                SpellWheelOverlay.instance.open();
            }
        }

        for (int i = 0; i < QUICK_CAST_MAPPINGS.size(); i++) {
            if (QUICK_CAST_MAPPINGS.get(i).consume()) {
                PacketDistributor.sendToServer(new QuickCastPacket(i));
                break;
            }
        }

        if (SPELLBAR_SCROLL_MODIFIER_KEYMAP.isDown()) {
            if (ClientConfigs.SPELL_BAR_DISPLAY.get().equals(ManaBarOverlay.Display.Contextual)) {
                SpellBarOverlay.fadeoutDelay = 40;
            }
        }

        updateShowExpandedTooltip();

        handleUseRelease();
    }

    private static void handleSpellWheelRelease() {
        final boolean isDown = SPELL_WHEEL_KEYMAP.isDown();

        final boolean wasReleased = wasSpellWheelDown && !isDown;
        if (wasReleased && SpellWheelOverlay.instance.active) {
            SpellWheelOverlay.instance.close();
        }

        wasSpellWheelDown = isDown;
    }

    /// Called in every client tick event to update [#showExpandedTooltip].
    ///
    /// Extracted for modularity without assuming keyboard/mouse specific input,
    /// allowing other mods to provide controller or alternative input sources.
    private static void updateShowExpandedTooltip() {
        /// Uses [KeyMapping#getDefaultKey()] instead of [KeyMapping#getKey()] to always use "Left Shift"
        /// without respecting the current bound input to "Sneak"
        showExpandedTooltip = isKeyboardMouseInputDown(Minecraft.getInstance().options.keyShift.getDefaultKey());
    }

    /// Tracks the previous [KeyMapping#isDown()] state for [net.minecraft.client.Options#keyUse].
    private static boolean wasUseDown = false;

    private static void handleUseRelease() {
        final boolean isDown = Minecraft.getInstance().options.keyUse.isDown();

        final boolean wasReleased = wasUseDown && !isDown;
        if (wasReleased) {
            ClientSpellCastHelper.setSuppressRightClicks(false);
            hasReleasedSinceCasting = true;
        }

        wasUseDown = isDown;
    }

    public static boolean isShowExpandedTooltip() {
        return showExpandedTooltip;
    }

    public static void setShowExpandedTooltip(boolean showExpandedTooltip) {
        ClientInputEvents.showExpandedTooltip = showExpandedTooltip;
    }

    public static boolean isUseKeyDown() {
        return isKeyboardMouseInputDown(Minecraft.getInstance().options.keyUse.getKey());
    }

    /// Returns whether the provided key or mouse button is physically down, regardless of Minecraft internals,
    /// so this may report `true` when down even when a screen is open, unlike [KeyMapping#isDown()].
    ///
    /// **Important:** Consumers should always consider using [KeyMapping#isDown()] over this API, as it does not
    /// work with other input systems, and is not a vanilla supported API.
    /// This is only needed in the case of GUI,
    /// since [KeyMapping#isDown()] will always report `false` when any screen is open.
    ///
    /// @param key example [InputConstants#KEY_LEFT] or [InputConstants#]
    /// @see InputConstants
    private static boolean isKeyboardMouseInputDown(InputConstants.Key key) {
        final int keyValue = key.getValue();
        final long windowPointer = Minecraft.getInstance().getWindow().getWindow();

        if (key.getType() == InputConstants.Type.KEYSYM) {
            return GLFW.glfwGetKey(windowPointer, keyValue) > 0;
        } else if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(windowPointer, keyValue) > 0;
        }
        return false;
    }
}
