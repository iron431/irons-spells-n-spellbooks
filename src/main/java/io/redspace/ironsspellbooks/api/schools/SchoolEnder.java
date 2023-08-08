package io.redspace.ironsspellbooks.api.schools;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AutoSchoolConfig;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.common.util.LazyOptional;

@AutoSchoolConfig
public class SchoolEnder extends SchoolType {
    public SchoolEnder() {
        super(
                IronsSpellbooks.id("ender"),
                ModTags.ENDER_FOCUS,
                Component.translatable("school.irons_spellbooks.ender").withStyle(ChatFormatting.LIGHT_PURPLE),
                LazyOptional.of(AttributeRegistry.ENDER_SPELL_POWER::get),
                LazyOptional.empty(),
                LazyOptional.of(SoundRegistry.ENDER_CAST::get)
        );
    }
}
