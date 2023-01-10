package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.example.testmod.gui.SpellWheelDisplay;
import com.example.testmod.gui.network.PacketChangeSelectedSpell;
import com.example.testmod.item.SpellBook;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientKeyHandler {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();


    private static final KeyState SPELL_WHEEL_STATE = register(KeyMappings.SPELL_WHEEL_KEYMAP);
    private static final KeyState SPELLBAR_MODIFIER_STATE = register(KeyMappings.SPELLBAR_SCROLL_MODIFIER_KEYMAP);

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {


        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null)
            return;

        if (SPELL_WHEEL_STATE.wasPressed()) {
            if (minecraft.screen == null && Utils.isPlayerHoldingSpellBook(player))
                SpellWheelDisplay.open();
        }
        if (SPELL_WHEEL_STATE.wasReleased()) {
            if (minecraft.screen == null && SpellWheelDisplay.active)
                SpellWheelDisplay.close();

        }
        if (SPELLBAR_MODIFIER_STATE.wasPressed())
            System.out.println("Shift Down");

        if (SPELLBAR_MODIFIER_STATE.wasReleased())
            System.out.println("Shift Up");

        Update();
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
//                    TestMod.LOGGER.debug("original index: {}", spellBookData.getActiveSpellIndex());

                    List<AbstractSpell> spells = new ArrayList<>();
                    for (AbstractSpell spell : spellBookData.getInscribedSpells()) {
                        if (spell != null)
                            spells.add(spell);
                    }
                    int spellCount = spellBookData.getSpellCount();
                    int scrollIndex = (spells.indexOf(spellBookData.getActiveSpell()) - direction);
//                    TestMod.LOGGER.debug("collapsed new index: {}", scrollIndex);
//                    TestMod.LOGGER.debug("{} + {} = {}", scrollIndex, spellCount, scrollIndex + spellCount);
//                    TestMod.LOGGER.debug("{} % {} = {}", scrollIndex + spellCount, spellCount, (scrollIndex + spellCount) % spellCount);
                    scrollIndex = (scrollIndex + spellCount) % spellCount;

//                    TestMod.LOGGER.debug("wrapped collapsed index: {}", scrollIndex);


                    int selectedIndex = ArrayUtils.indexOf(spellBookData.getInscribedSpells(), spells.get(scrollIndex));

//                    int newIndex = spellBookData.getActiveSpellIndex() - direction;
//                    newIndex = (newIndex + spellBookData.getSpellCount()) % spellBookData.getSpellCount();
//                    while (spellBookData.getInscribedSpells()[newIndex] == null) {
//                        newIndex = (newIndex + spellBookData.getSpellCount() - direction) % spellBookData.getSpellCount();
//                    }
//                    do {
//                        newIndex = (newIndex + direction) % spellBook.getCount();
//                        TestMod.LOGGER.debug("new index: {}",newIndex);
//                        TestMod.LOGGER.debug("spell here: {}",spellBookData.getInscribedSpells()[newIndex]);
//                        if(newIndex == spellBookData.getActiveSpellIndex())
//                            break;
//                    }
//                    while (spellBookData.getInscribedSpells()[newIndex] == null);
                    Messages.sendToServer(new PacketChangeSelectedSpell(selectedIndex));
                    event.setCanceled(true);
                }

            }
        }
    }

    private static void Update() {
        for (KeyState k : KEY_STATES) {
            k.Update();
        }
    }

    private static KeyState register(KeyMapping key) {
        var k = new KeyState(key);
        KEY_STATES.add(k);
        return k;
    }

}
