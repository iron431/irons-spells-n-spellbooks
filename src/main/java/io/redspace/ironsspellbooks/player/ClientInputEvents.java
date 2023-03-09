package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import io.redspace.ironsspellbooks.gui.overlays.network.ServerboundSetSpellBookActiveIndex;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.util.Utils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientInputEvents {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();

    private static final KeyState SPELL_WHEEL_STATE = register(KeyMappings.SPELL_WHEEL_KEYMAP);
    private static final KeyState SPELLBAR_MODIFIER_STATE = register(KeyMappings.SPELLBAR_SCROLL_MODIFIER_KEYMAP);

    private static KeyMapping useKeyMapping;
    public static boolean isUseKeyDown;
    public static boolean hasReleasedSinceCasting;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {

        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null)
            return;

        if (SPELL_WHEEL_STATE.wasPressed()) {
            if (minecraft.screen == null && Utils.isPlayerHoldingSpellBook(player))
                SpellWheelOverlay.instance.open();
        }
        if (SPELL_WHEEL_STATE.wasReleased()) {
            if (minecraft.screen == null && SpellWheelOverlay.instance.active)
                SpellWheelOverlay.instance.close();

        }

        //irons_spellbooks.LOGGER.debug("IsUseKeyDown: {} |\tSuppressRightClicks: {}", isUseKeyDown, ClientSpellCastHelper.shouldSuppressRightClicks());
        //irons_spellbooks.LOGGER.debug("HasReleasedSinceCasting: {}", hasReleasedSinceCasting);

        update();
    }

    @SubscribeEvent
    public static void clientMouseScrolled(InputEvent.MouseScrollingEvent event) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null)
            return;
        if (Utils.isPlayerHoldingSpellBook(player) && minecraft.screen == null) {
            if (SPELLBAR_MODIFIER_STATE.isHeld()) {
                ItemStack spellBook = player.getMainHandItem().getItem() instanceof SpellBook ? player.getMainHandItem() : player.getOffhandItem();
                var spellBookData = ((SpellBook) spellBook.getItem()).getSpellBookData(spellBook);
                if (spellBookData.getSpellCount() > 0) {
                    int direction = (int) event.getScrollDelta();
//                    irons_spellbooks.LOGGER.debug("original index: {}", spellBookData.getActiveSpellIndex());

                    List<AbstractSpell> spells = new ArrayList<>();
                    for (AbstractSpell spell : spellBookData.getInscribedSpells()) {
                        if (spell != null)
                            spells.add(spell);
                    }
                    int spellCount = spellBookData.getSpellCount();
                    int scrollIndex = (spells.indexOf(spellBookData.getActiveSpell()) - direction);
//                    irons_spellbooks.LOGGER.debug("collapsed new index: {}", scrollIndex);
//                    irons_spellbooks.LOGGER.debug("{} + {} = {}", scrollIndex, spellCount, scrollIndex + spellCount);
//                    irons_spellbooks.LOGGER.debug("{} % {} = {}", scrollIndex + spellCount, spellCount, (scrollIndex + spellCount) % spellCount);
                    scrollIndex = (scrollIndex + spellCount) % spellCount;

//                    irons_spellbooks.LOGGER.debug("wrapped collapsed index: {}", scrollIndex);


                    int selectedIndex = ArrayUtils.indexOf(spellBookData.getInscribedSpells(), spells.get(scrollIndex));

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
                    Messages.sendToServer(new ServerboundSetSpellBookActiveIndex(selectedIndex));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onUseInput(InputEvent.InteractionKeyMappingTriggered event) {
        //irons_spellbooks.LOGGER.debug("onUseInput: keymapping: {} ({})", useKeyMapping.getKey(), useKeyMapping.getKey().getValue());
        if (event.isUseItem()) {
            useKeyMapping = event.getKeyMapping();
            if (ClientSpellCastHelper.shouldSuppressRightClicks()) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        handleRightClickSuppression(event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        handleRightClickSuppression(event.getButton(), event.getAction());
    }

    private static void handleRightClickSuppression(int button, int action) {
        if (useKeyMapping != null && button == useKeyMapping.getKey().getValue())
            if (action == InputConstants.RELEASE) {
                ClientSpellCastHelper.setSuppressRightClicks(false);
                isUseKeyDown = false;
                hasReleasedSinceCasting = true;
            } else if (action == InputConstants.PRESS) {
                isUseKeyDown = true;
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

}
