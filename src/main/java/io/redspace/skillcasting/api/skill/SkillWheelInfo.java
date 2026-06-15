package io.redspace.skillcasting.api.skill;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Grouping of text components to render at the head of the skill wheel when previewing this spell selection
 *
 * @param leftText  The left half of the tooltip, typically displaying generic stats (level, cooldown, etc)
 * @param rightText The right half of the tooltip, typically displaying specific stats (skill damage, skill attributes, etc)
 */
public record SkillWheelInfo(List<Component> leftText, List<Component> rightText) {
    public SkillWheelInfo() {
        this(new ArrayList<>(), new ArrayList<>());
    }
}
