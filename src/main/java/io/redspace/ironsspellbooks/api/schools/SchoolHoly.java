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
public class SchoolHoly extends SchoolType {
    public SchoolHoly() {
        super(
                IronsSpellbooks.id("holy"),
                ModTags.HOLY_FOCUS,
                Component.translatable("school.irons_spellbooks.holy").withStyle(Style.EMPTY.withColor(0xfff8d4)),
                LazyOptional.of(AttributeRegistry.HOLY_SPELL_POWER::get),
                LazyOptional.empty(),
                LazyOptional.of(SoundRegistry.HOLY_CAST::get)
        );
    }
}
