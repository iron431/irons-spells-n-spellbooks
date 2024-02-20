package io.redspace.ironsspellbooks.player;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.checkerframework.checker.units.qual.K;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = IronsSpellbooks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class KeyMappings {
    public static final String KEY_BIND_GENERAL_CATEGORY = "key.irons_spellbooks.group_1";
    public static final String KEY_BIND_QUICK_CAST_CATEGORY = "key.irons_spellbooks.group_2";

    public static final KeyMapping SPELL_WHEEL_KEYMAP = new KeyMapping(getResourceName("spell_wheel"), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_R, KEY_BIND_GENERAL_CATEGORY);
    public static final KeyMapping SPELLBOOK_CAST_ACTIVE_KEYMAP = new KeyMapping(getResourceName("spellbook_cast"), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, KEY_BIND_GENERAL_CATEGORY);
    public static final KeyMapping SPELLBAR_SCROLL_MODIFIER_KEYMAP = new KeyMapping(getResourceName("spell_bar_modifier"), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_LALT, KEY_BIND_GENERAL_CATEGORY);
    public static final List<KeyMapping> QUICK_CAST_MAPPINGS = createQuickCastKeybinds();

    private static String getResourceName(String name) {
        return String.format("key.irons_spellbooks.%s", name);
    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent event) {
        //Ironsspellbooks.logger.debug("KeyMappings.onRegisterKeybinds");
        event.register(SPELL_WHEEL_KEYMAP);
        event.register(SPELLBOOK_CAST_ACTIVE_KEYMAP);
        event.register(SPELLBAR_SCROLL_MODIFIER_KEYMAP);
//        event.register(ELDRITCH_SCREEN_KEYMAP);
        QUICK_CAST_MAPPINGS.forEach(event::register);
    }

    private static List<KeyMapping> createQuickCastKeybinds() {
        var qcm = new ArrayList<KeyMapping>();
        for (int i = 1; i <= 15; i++) {
            qcm.add(new KeyMapping(getResourceName(String.format("spell_quick_cast_%d", i)), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_BIND_QUICK_CAST_CATEGORY));
        }
        return qcm;
    }
}
