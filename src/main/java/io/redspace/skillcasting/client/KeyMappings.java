package io.redspace.skillcasting.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import java.util.ArrayList;
import java.util.List;

public final class KeyMappings {
    public static final String KEY_BIND_GENERAL_CATEGORY = "key.skillcasting.group_1";
    public static final String KEY_BIND_QUICK_CAST_CATEGORY = "key.skillcasting.group_2";

    public static final ExtendedKeyMapping SKILL_WHEEL_KEYMAP = new ExtendedKeyMapping(
            "key.skillcasting.skill_wheel", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_R, KEY_BIND_GENERAL_CATEGORY);
    public static final ExtendedKeyMapping SKILL_WHEEL_TOGGLE_KEYMAP = new ExtendedKeyMapping(
            "key.skillcasting.skill_wheel_toggle", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_BIND_GENERAL_CATEGORY);
    public static final ExtendedKeyMapping CAST_SELECTED_SKILL_KEYMAP = new ExtendedKeyMapping(
            "key.skillcasting.cast_selected", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, KEY_BIND_GENERAL_CATEGORY);
    public static final ExtendedKeyMapping SKILLBAR_SCROLL_MODIFIER_KEYMAP = new ExtendedKeyMapping(
            "key.skillcasting.skill_bar_modifier", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_LALT, KEY_BIND_GENERAL_CATEGORY);
    public static final List<ExtendedKeyMapping> QUICK_CAST_MAPPINGS = createQuickCastKeybinds();

    private KeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SKILL_WHEEL_KEYMAP);
        event.register(SKILL_WHEEL_TOGGLE_KEYMAP);
        event.register(CAST_SELECTED_SKILL_KEYMAP);
        event.register(SKILLBAR_SCROLL_MODIFIER_KEYMAP);
        QUICK_CAST_MAPPINGS.forEach(event::register);
    }

    private static List<ExtendedKeyMapping> createQuickCastKeybinds() {
        var mappings = new ArrayList<ExtendedKeyMapping>();
        for (int i = 1; i <= 15; i++) {
            mappings.add(new ExtendedKeyMapping(
                    "key.skillcasting.quick_cast_" + i,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    KEY_BIND_QUICK_CAST_CATEGORY));
        }
        return mappings;
    }
}
