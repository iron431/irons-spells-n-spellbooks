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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.ArrayList;
import java.util.List;

import static io.redspace.ironsspellbooks.player.KeyMappings.*;

@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientInputEvents {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();

    private static final KeyState SPELLBAR_MODIFIER_STATE = register(KeyMappings.SPELLBAR_SCROLL_MODIFIER_KEYMAP);
    private static final List<KeyState> QUICK_CAST_STATES = registerQuickCast(KeyMappings.QUICK_CAST_MAPPINGS);

    private static int useKeyId = Integer.MIN_VALUE;
    public static boolean isUseKeyDown;
    public static boolean hasReleasedSinceCasting;
    public static boolean isShiftKeyDown;

    @SubscribeEvent
    public static void clientMouseScrolled(InputEvent.MouseScrollingEvent event) {
        Player player = MinecraftInstanceHelper.getPlayer();
        if (player == null)
            return;

        if (Minecraft.getInstance().screen == null) {
            if (SPELLBAR_MODIFIER_STATE.isHeld()) {
                SpellSelectionManager spellSelectionManager = ClientMagicData.getSpellSelectionManager();
                if (spellSelectionManager.getSpellCount() > 0) {
                    int direction = Mth.clamp((int) event.getScrollDelta(), -1, 1);
                    List<SpellSelectionManager.SelectionOption> spellbookSpells = spellSelectionManager.getAllSpells();
                    int spellCount = spellbookSpells.size();
                    int scrollIndex = (Mth.clamp(spellSelectionManager.getSelectionIndex(), 0, spellCount) - direction);
                    int selectedIndex = (Mth.clamp(scrollIndex, -1, spellCount + 1) + spellCount) % spellCount;
                    spellSelectionManager.makeSelection(selectedIndex);
                    event.setCanceled(true);
                }
            }
        }
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
        for (int i = 0; i < QUICK_CAST_STATES.size(); i++) {
            if (QUICK_CAST_STATES.get(i).wasPressed()) {
                PacketDistributor.sendToServer(new QuickCastPacket(i));
                break;
            }
        }
        if (SPELLBAR_MODIFIER_STATE.isHeld()) {
            if (ClientConfigs.SPELL_BAR_DISPLAY.get().equals(ManaBarOverlay.Display.Contextual)) {
                SpellBarOverlay.fadeoutDelay = 40;
            }
        }
        update();
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

    private static void update() {
        for (KeyState k : KEY_STATES) {
            k.update();
        }
    }

    private static KeyState register(KeyMapping key) {
        var k = new KeyState(key);
        KEY_STATES.add(k);
        return k;
    }

    private static List<KeyState> registerQuickCast(List<KeyMapping> mappings) {
        var keyStates = new ArrayList<KeyState>();

        mappings.forEach(keyMapping -> {
            var k = new KeyState(keyMapping);
            KEY_STATES.add(k);
            keyStates.add(k);
        });

        return keyStates;
    }
}
