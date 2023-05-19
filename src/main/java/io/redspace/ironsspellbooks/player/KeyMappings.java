package io.redspace.ironsspellbooks.player;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public final class KeyMappings {
    public static final String KEY_BIND_GENERAL_CATEGORY = "key.irons_spellbooks.group_1";
    public static final String KEY_BIND_QUICK_CAST_CATEGORY = "key.irons_spellbooks.group_2";
    public static final KeyMapping SPELL_WHEEL_KEYMAP = new KeyMapping(getResourceName("spell_wheel"), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_R, KEY_BIND_GENERAL_CATEGORY);
    public static final KeyMapping SPELLBAR_SCROLL_MODIFIER_KEYMAP = new KeyMapping(getResourceName("spell_bar_modifier"), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_LSHIFT, KEY_BIND_GENERAL_CATEGORY);

    public static final List<KeyMapping> QUICK_CAST_MAPPINGS = createQuickCastKeybinds();

    private static String getResourceName(String name) {
        return String.format("key.irons_spellbooks.%s", name);
    }

    public static void onRegisterKeybinds() {
 //Ironsspellbooks.logger.debug("KeyMappings.onRegisterKeybinds");
        ClientRegistry .registerKeyBinding(SPELL_WHEEL_KEYMAP) ;
        ClientRegistry .registerKeyBinding(SPELLBAR_SCROLL_MODIFIER_KEYMAP) ;
        QUICK_CAST_MAPPINGS.forEach(ClientRegistry::registerKeyBinding);
    }

    private static List<KeyMapping> createQuickCastKeybinds() {
        var qcm = new ArrayList<KeyMapping>();
        for (int i = 1; i <= 15; i++) {
            qcm.add(new KeyMapping(getResourceName(String.format("spell_quick_cast_%d", i)), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_BIND_QUICK_CAST_CATEGORY));
        }
        return qcm;
    }
}
