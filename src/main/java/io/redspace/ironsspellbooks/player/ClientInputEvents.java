package io.redspace.ironsspellbooks.player;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelectionManager;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import io.redspace.ironsspellbooks.network.ServerboundCast;
import io.redspace.ironsspellbooks.network.ServerboundQuickCast;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static io.redspace.ironsspellbooks.player.KeyMappings.SPELLBOOK_CAST_ACTIVE_KEYMAP;

@Mod.EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientInputEvents {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();

    private static final KeyState SPELL_WHEEL_STATE = register(KeyMappings.SPELL_WHEEL_KEYMAP);
    private static final KeyState SPELLBAR_MODIFIER_STATE = register(KeyMappings.SPELLBAR_SCROLL_MODIFIER_KEYMAP);
    private static final KeyState SPELLBOOK_CAST_STATE = register(SPELLBOOK_CAST_ACTIVE_KEYMAP);
    //    private static final KeyState ELDRITCH_SCREEN_STATE = register(KeyMappings.ELDRITCH_SCREEN_KEYMAP);
    private static final List<KeyState> QUICK_CAST_STATES = registerQuickCast(KeyMappings.QUICK_CAST_MAPPINGS);

    private static int useKeyId = Integer.MIN_VALUE;
    public static boolean isUseKeyDown;
    public static boolean hasReleasedSinceCasting;

    public static int test = 0;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        if (SPELLBOOK_CAST_STATE.wasPressed() && minecraft.screen == null) {
            Messages.sendToServer(new ServerboundCast());
        }

        if (SPELL_WHEEL_STATE.wasPressed()) {
            if (minecraft.screen == null /*&& Utils.isPlayerHoldingSpellBook(player)*/)
                SpellWheelOverlay.instance.open();
        }
        if (SPELL_WHEEL_STATE.wasReleased()) {
            if (minecraft.screen == null && SpellWheelOverlay.instance.active)
                SpellWheelOverlay.instance.close();
        }
//        if (ELDRITCH_SCREEN_STATE.wasPressed()) {
//            if (minecraft.screen == null) {
//                minecraft.setScreen(new EldritchResearchScreen(Component.empty(), player.getOffhandItem().is(ItemRegistry.ELDRITCH_PAGE.get()) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
//            }else if(minecraft.screen instanceof EldritchResearchScreen screen){
//                screen.onClose();
//            }
//        }

        update();
    }

    @SubscribeEvent
    public static void clientMouseScrolled(InputEvent.MouseScrollingEvent event) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null)
            return;

        var spellbookStack = Utils.getPlayerSpellbookStack(player);
        if (spellbookStack != null && minecraft.screen == null) {
            if (SPELLBAR_MODIFIER_STATE.isHeld()) {
                SpellSelectionManager spellSelectionManager = new SpellSelectionManager(MinecraftInstanceHelper.getPlayer());
                if (spellSelectionManager.getSpellCount() > 0) {
                    int direction = Mth.clamp((int) event.getScrollDelta(), -1, 1);
//                    irons_spellbooks.LOGGER.debug("original index: {}", spellBookData.getActiveSpellIndex());

                    List<SpellSelectionManager.SpellSlot> spellbookSpells = spellSelectionManager.getSpellsForSlot(Curios.SPELLBOOK_SLOT);
                    int spellCount = spellbookSpells.size();
                    int scrollIndex = (Mth.clamp(spellSelectionManager.getSelectionIndex(), 0, spellCount) - direction);
//                    irons_spellbooks.LOGGER.debug("collapsed new index: {}", scrollIndex);
//                    irons_spellbooks.LOGGER.debug("{} + {} = {}", scrollIndex, spellCount, scrollIndex + spellCount);
//                    irons_spellbooks.LOGGER.debug("{} % {} = {}", scrollIndex + spellCount, spellCount, (scrollIndex + spellCount) % spellCount);
                    int selectedIndex = (Mth.clamp(scrollIndex, -1, spellCount + 1) + spellCount) % spellCount;

//                    irons_spellbooks.LOGGER.debug("wrapped collapsed index: {}", scrollIndex);


//                    int selectedIndex = ArrayUtils.indexOf(spellBookData.getInscribedSpells(), spellbookSpells.get(scrollIndex));

//                    int newIndex = spellBookData.getActiveSpellIndex() - direction;
//                    newIndex = (newIndex + spellBookData.getSpellCount()) % spellBookData.getSpellCount();
//                    while (spellBookData.getInscribedSpells()[newIndex] == null) {
//                        newIndex = (newIndex + spellBookData.getSpellCount() - direction) % spellBookData.getSpellCount();
//                    }
//                    do {
//                        newIndex = (newIndex + direction) % spellBook.getCount();
//                        irons_spellbooks.LOGGER.debug("new index: {}",newIndex);
//                        irons_spellbooks.LOGGER.debug("spell here: {}",spellBookData.getInscribedSpells()[newIndex]);
//                        if(newIndex == spellBookData.getActiveSpellIndex())
//                            break;
//                    }
//                    while (spellBookData.getInscribedSpells()[newIndex] == null);
                    spellSelectionManager.makeSelection(selectedIndex);
                    //Messages.sendToServer(new ServerboundSetSpellBookActiveIndex(selectedIndex));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onUseInput(InputEvent.InteractionKeyMappingTriggered event) {
        //IronsSpellbooks.LOGGER.debug("InteractionKeyMappingTriggered: {}", event.getKeyMapping().getName());
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
        //IronsSpellbooks.LOGGER.debug("onKeyInput key:{}", event.getKey());
        handleRightClickSuppression(event.getKey(), event.getAction());

        for (int i = 0; i < QUICK_CAST_STATES.size(); i++) {
            //IronsSpellbooks.LOGGER.debug("onKeyInput i:{}",i);
            if (QUICK_CAST_STATES.get(i).wasPressed()) {
                //IronsSpellbooks.LOGGER.debug("onKeyInput cast}");
                Messages.sendToServer(new ServerboundQuickCast(i));
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        handleRightClickSuppression(event.getButton(), event.getAction());
    }

    private static void handleRightClickSuppression(int button, int action) {
        //IronsSpellbooks.LOGGER.debug("ClientInputEvents.handleRightClickSuppression {} {}", button, action);
        if (useKeyId == Integer.MIN_VALUE) {
            useKeyId = Minecraft.getInstance().options.keyUse.getKey().getValue();
        }

        if (button == useKeyId) {
            if (action == InputConstants.RELEASE) {
                //IronsSpellbooks.LOGGER.debug("ClientInputEvents.handleRightClickSuppression.1");
                ClientSpellCastHelper.setSuppressRightClicks(false);
                isUseKeyDown = false;
                hasReleasedSinceCasting = true;
            } else if (action == InputConstants.PRESS) {
                //IronsSpellbooks.LOGGER.debug("ClientInputEvents.handleRightClickSuppression.2");
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
