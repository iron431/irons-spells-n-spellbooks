package io.redspace.ironsspellbooks.api.schools;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AutoSchoolConfig;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.LazyOptional;

@AutoSchoolConfig
public class SchoolNature extends SchoolType {
    public SchoolNature() {
        super(
                IronsSpellbooks.id("nature"),
                ModTags.NATURE_FOCUS,
                Component.translatable("school.irons_spellbooks.nature").withStyle(ChatFormatting.GREEN),
                LazyOptional.of(AttributeRegistry.NATURE_SPELL_POWER::get),
                LazyOptional.empty(),
                LazyOptional.of(SoundRegistry.NATURE_CAST::get)
        );
    }
}
