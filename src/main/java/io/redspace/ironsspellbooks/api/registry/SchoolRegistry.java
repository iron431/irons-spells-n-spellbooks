package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class SchoolRegistry {
    public static final ResourceKey<Registry<SchoolType>> SCHOOL_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "schools"));
    private static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(SCHOOL_REGISTRY_KEY, IronsSpellbooks.MODID);
    public static final Registry<SchoolType> REGISTRY = new RegistryBuilder<>(SCHOOL_REGISTRY_KEY).create();

    public static void register(IEventBus eventBus) {
        SCHOOLS.register(eventBus);
    }

    public static void registerRegistry(NewRegistryEvent event) {
        IronsSpellbooks.LOGGER.debug("SchoolRegistry.registerRegistry");
        event.register(REGISTRY);
    }

    private static DeferredHolder<SchoolType, SchoolType> registerSchool(String name, Supplier<SchoolType> schoolType) {
        return SCHOOLS.register(name, schoolType);
    }

    public static SchoolType getSchool(ResourceLocation resourceLocation) {
        return REGISTRY.get(resourceLocation);
    }

    public static final ResourceLocation FIRE_RESOURCE = IronsSpellbooks.id("fire");
    public static final ResourceLocation ICE_RESOURCE = IronsSpellbooks.id("ice");
    public static final ResourceLocation LIGHTNING_RESOURCE = IronsSpellbooks.id("lightning");
    public static final ResourceLocation HOLY_RESOURCE = IronsSpellbooks.id("holy");
    public static final ResourceLocation ENDER_RESOURCE = IronsSpellbooks.id("ender");
    public static final ResourceLocation BLOOD_RESOURCE = IronsSpellbooks.id("blood");
    public static final ResourceLocation EVOCATION_RESOURCE = IronsSpellbooks.id("evocation");
    public static final ResourceLocation NATURE_RESOURCE = IronsSpellbooks.id("nature");
    public static final ResourceLocation ELDRITCH_RESOURCE = IronsSpellbooks.id("eldritch");

    public static final DeferredHolder<SchoolType, SchoolType> FIRE = registerSchool("fire", () -> new SchoolType(
            ModTags.FIRE_FOCUS,
            Component.translatable("school.irons_spellbooks.fire").withStyle(ChatFormatting.GOLD),
            SpellcastingComponentTypes.FIRE_POWER_MULTIPLIER,
            AttributeRegistry.FIRE_SPELL_POWER,
            AttributeRegistry.FIRE_MAGIC_RESIST,
            SoundRegistry.FIRE_CAST,
            ISSDamageTypes.FIRE_MAGIC
    ));

    public static final DeferredHolder<SchoolType, SchoolType> ICE = registerSchool("ice", () -> new SchoolType(
            ModTags.ICE_FOCUS,
            Component.translatable("school.irons_spellbooks.ice").withStyle(Style.EMPTY.withColor(0xd0f9ff)),
            SpellcastingComponentTypes.ICE_POWER_MULTIPLIER,
            AttributeRegistry.ICE_SPELL_POWER,
            AttributeRegistry.ICE_MAGIC_RESIST,
            SoundRegistry.ICE_CAST,
            ISSDamageTypes.ICE_MAGIC
    ));

    public static final DeferredHolder<SchoolType, SchoolType> LIGHTNING = registerSchool("lightning", () -> new SchoolType(
            ModTags.LIGHTNING_FOCUS,
            Component.translatable("school.irons_spellbooks.lightning").withStyle(ChatFormatting.AQUA),
            SpellcastingComponentTypes.LIGHTNING_POWER_MULTIPLIER,
            AttributeRegistry.LIGHTNING_SPELL_POWER,
            AttributeRegistry.LIGHTNING_MAGIC_RESIST,
            SoundRegistry.LIGHTNING_CAST,
            ISSDamageTypes.LIGHTNING_MAGIC
    ));

    public static final DeferredHolder<SchoolType, SchoolType> HOLY = registerSchool("holy", () -> new SchoolType(
            ModTags.HOLY_FOCUS,
            Component.translatable("school.irons_spellbooks.holy").withStyle(Style.EMPTY.withColor(0xfff8d4)),
            SpellcastingComponentTypes.HOLY_POWER_MULTIPLIER,
            AttributeRegistry.HOLY_SPELL_POWER,
            AttributeRegistry.HOLY_MAGIC_RESIST,
            SoundRegistry.HOLY_CAST,
            ISSDamageTypes.HOLY_MAGIC
    ));

    public static final DeferredHolder<SchoolType, SchoolType> ENDER = registerSchool("ender", () -> new SchoolType(
            ModTags.ENDER_FOCUS,
            Component.translatable("school.irons_spellbooks.ender").withStyle(ChatFormatting.LIGHT_PURPLE),
            SpellcastingComponentTypes.ENDER_POWER_MULTIPLIER,
            AttributeRegistry.ENDER_SPELL_POWER,
            AttributeRegistry.ENDER_MAGIC_RESIST,
            SoundRegistry.ENDER_CAST,
            ISSDamageTypes.ENDER_MAGIC
    ));

    public static final DeferredHolder<SchoolType, SchoolType> BLOOD = registerSchool("blood", () -> new SchoolType(
            ModTags.BLOOD_FOCUS,
            Component.translatable("school.irons_spellbooks.blood").withStyle(ChatFormatting.DARK_RED),
            SpellcastingComponentTypes.BLOOD_POWER_MULTIPLIER,
            AttributeRegistry.BLOOD_SPELL_POWER,
            AttributeRegistry.BLOOD_MAGIC_RESIST,
            SoundRegistry.BLOOD_CAST,
            ISSDamageTypes.BLOOD_MAGIC));

    public static final DeferredHolder<SchoolType, SchoolType> EVOCATION = registerSchool("evocation", () -> new SchoolType(
            ModTags.EVOCATION_FOCUS,
            Component.translatable("school.irons_spellbooks.evocation").withStyle(ChatFormatting.WHITE),
            SpellcastingComponentTypes.EVOCATION_POWER_MULTIPLIER,
            AttributeRegistry.EVOCATION_SPELL_POWER,
            AttributeRegistry.EVOCATION_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ISSDamageTypes.EVOCATION_MAGIC
    ));

    public static final DeferredHolder<SchoolType, SchoolType> NATURE = registerSchool("nature", () -> new SchoolType(
            ModTags.NATURE_FOCUS,
            Component.translatable("school.irons_spellbooks.nature").withStyle(ChatFormatting.GREEN),
            SpellcastingComponentTypes.NATURE_POWER_MULTIPLIER,
            AttributeRegistry.NATURE_SPELL_POWER,
            AttributeRegistry.NATURE_MAGIC_RESIST,
            SoundRegistry.NATURE_CAST,
            ISSDamageTypes.NATURE_MAGIC
    ));

    public static final DeferredHolder<SchoolType, SchoolType> ELDRITCH = registerSchool("eldritch", () -> new SchoolType(
            ModTags.ELDRITCH_FOCUS,
            Component.translatable("school.irons_spellbooks.eldritch").withStyle(Style.EMPTY.withColor(0x0f839c)),
            SpellcastingComponentTypes.ELDRITCH_POWER_MULTIPLIER,
            AttributeRegistry.ELDRITCH_SPELL_POWER,
            AttributeRegistry.ELDRITCH_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ISSDamageTypes.ELDRITCH_MAGIC,
            true,
            false
    ));

    @Nullable
    public static SchoolType getSchoolFromFocus(ItemStack focusStack) {
        for (SchoolType school : REGISTRY) {
            if (school.isFocus(focusStack)) {
                return school;
            }
        }
        return null;
    }

    public static List<SchoolType> getSchoolsFromFocus(ItemStack focusStack) {
        return REGISTRY.stream().filter(school -> school.isFocus(focusStack)).toList();
    }
}
