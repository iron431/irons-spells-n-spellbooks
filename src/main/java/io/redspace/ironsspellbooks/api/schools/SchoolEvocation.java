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
public class SchoolEvocation extends SchoolType {
    public SchoolEvocation() {
        super(
                IronsSpellbooks.id("evocation"),
                ModTags.EVOCATION_FOCUS,
                Component.translatable("school.irons_spellbooks.evocation").withStyle(ChatFormatting.WHITE),
                LazyOptional.of(AttributeRegistry.EVOCATION_SPELL_POWER::get),
                LazyOptional.empty(),
                LazyOptional.of(SoundRegistry.EVOCATION_CAST::get)
        );
    }
}
