package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
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
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SchoolRegistry {
    public static final ResourceKey<Registry<SchoolType>> SCHOOL_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(IronsSpellbooks.MODID, "schools"));
    private static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(SCHOOL_REGISTRY_KEY, IronsSpellbooks.MODID);
    public static final Registry<SchoolType> REGISTRY = new RegistryBuilder<>(SCHOOL_REGISTRY_KEY).create();

    /**
     * Register registry objects
     */
    public static void register(IEventBus eventBus) {
        SCHOOLS.register(eventBus);
    }

    public static void registerRegistry(NewRegistryEvent event) {
        IronsSpellbooks.LOGGER.debug("SchoolRegistry.registerRegistry");
        event.register(REGISTRY);
    }

    private static Supplier<SchoolType> registerSchool(SchoolType schoolType) {
        return SCHOOLS.register(schoolType.getId().getPath(), () -> schoolType);
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

    public static final Supplier<SchoolType> FIRE = registerSchool(new SchoolType(
            FIRE_RESOURCE,
            ModTags.FIRE_FOCUS,
            Component.translatable("school.irons_spellbooks.fire").withStyle(ChatFormatting.GOLD),
            AttributeRegistry.FIRE_SPELL_POWER,
            AttributeRegistry.FIRE_MAGIC_RESIST,
            SoundRegistry.FIRE_CAST,
            ISSDamageTypes.FIRE_MAGIC
    ));

    public static final Supplier<SchoolType> ICE = registerSchool(new SchoolType(
            ICE_RESOURCE,
            ModTags.ICE_FOCUS,
            Component.translatable("school.irons_spellbooks.ice").withStyle(Style.EMPTY.withColor(0xd0f9ff)),
            AttributeRegistry.ICE_SPELL_POWER,
            AttributeRegistry.ICE_MAGIC_RESIST,
            SoundRegistry.ICE_CAST,
            ISSDamageTypes.ICE_MAGIC
    ));

    public static final Supplier<SchoolType> LIGHTNING = registerSchool(new SchoolType(
            LIGHTNING_RESOURCE,
            ModTags.LIGHTNING_FOCUS,
            Component.translatable("school.irons_spellbooks.lightning").withStyle(ChatFormatting.AQUA),
            AttributeRegistry.LIGHTNING_SPELL_POWER,
            AttributeRegistry.LIGHTNING_MAGIC_RESIST,
            SoundRegistry.LIGHTNING_CAST,
            ISSDamageTypes.LIGHTNING_MAGIC
    ));

    public static final Supplier<SchoolType> HOLY = registerSchool(new SchoolType(
            HOLY_RESOURCE,
            ModTags.HOLY_FOCUS,
            Component.translatable("school.irons_spellbooks.holy").withStyle(Style.EMPTY.withColor(0xfff8d4)),
            AttributeRegistry.HOLY_SPELL_POWER,
            AttributeRegistry.HOLY_MAGIC_RESIST,
            SoundRegistry.HOLY_CAST,
            ISSDamageTypes.HOLY_MAGIC
    ));

    public static final Supplier<SchoolType> ENDER = registerSchool(new SchoolType(
            ENDER_RESOURCE,
            ModTags.ENDER_FOCUS,
            Component.translatable("school.irons_spellbooks.ender").withStyle(ChatFormatting.LIGHT_PURPLE),
            AttributeRegistry.ENDER_SPELL_POWER,
            AttributeRegistry.ENDER_MAGIC_RESIST,
            SoundRegistry.ENDER_CAST,
            ISSDamageTypes.ENDER_MAGIC
    ));

    public static final Supplier<SchoolType> BLOOD = registerSchool(new SchoolType(
            BLOOD_RESOURCE,
            ModTags.BLOOD_FOCUS,
            Component.translatable("school.irons_spellbooks.blood").withStyle(ChatFormatting.DARK_RED),
            AttributeRegistry.BLOOD_SPELL_POWER,
            AttributeRegistry.BLOOD_MAGIC_RESIST,
            SoundRegistry.BLOOD_CAST,
            ISSDamageTypes.BLOOD_MAGIC));

    public static final Supplier<SchoolType> EVOCATION = registerSchool(new SchoolType(
            EVOCATION_RESOURCE,
            ModTags.EVOCATION_FOCUS,
            Component.translatable("school.irons_spellbooks.evocation").withStyle(ChatFormatting.WHITE),
            AttributeRegistry.EVOCATION_SPELL_POWER,
            AttributeRegistry.EVOCATION_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ISSDamageTypes.EVOCATION_MAGIC
    ));

    public static final Supplier<SchoolType> NATURE = registerSchool(new SchoolType(
            NATURE_RESOURCE,
            ModTags.NATURE_FOCUS,
            Component.translatable("school.irons_spellbooks.nature").withStyle(ChatFormatting.GREEN),
            AttributeRegistry.NATURE_SPELL_POWER,
            AttributeRegistry.NATURE_MAGIC_RESIST,
            SoundRegistry.NATURE_CAST,
            ISSDamageTypes.NATURE_MAGIC
    ));

    public static final Supplier<SchoolType> ELDRITCH = registerSchool(new SchoolType(
            ELDRITCH_RESOURCE,
            ModTags.ELDRITCH_FOCUS,
            Component.translatable("school.irons_spellbooks.eldritch").withStyle(Style.EMPTY.withColor(0x0f839c)),
            AttributeRegistry.ELDRITCH_SPELL_POWER,
            AttributeRegistry.ELDRITCH_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ISSDamageTypes.ELDRITCH_MAGIC,
            true,
            false
    ));

    @Nullable
    public static SchoolType getSchoolFromFocus(ItemStack focusStack) {
        //TODO: optimize with map or something
        for (SchoolType school : REGISTRY) {
            if (school.isFocus(focusStack)) {
                return school;
            }
        }
        return null;
    }
}
