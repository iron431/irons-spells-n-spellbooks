package io.redspace.ironsspellbooks.api.schools;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AutoSchoolConfig;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;

@AutoSchoolConfig
public class SchoolFire extends SchoolType {
    public SchoolFire() {
        super(
                IronsSpellbooks.id("fire"),
                ModTags.FIRE_FOCUS,
                Component.translatable("school.irons_spellbooks.fire").withStyle(ChatFormatting.GOLD),
                LazyOptional.of(AttributeRegistry.FIRE_SPELL_POWER::get),
                LazyOptional.empty(),
                LazyOptional.of(SoundRegistry.FIRE_CAST::get)
        );
    }
}
