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
public class SchoolBlood extends SchoolType {
    public SchoolBlood() {
        super(
                IronsSpellbooks.id("blood"),
                ModTags.BLOOD_FOCUS,
                Component.translatable("school.irons_spellbooks.blood").withStyle(ChatFormatting.DARK_RED),
                LazyOptional.of(AttributeRegistry.BLOOD_SPELL_POWER::get),
                LazyOptional.empty(),
                LazyOptional.of(SoundRegistry.BLOOD_CAST::get)
        );
    }
}
