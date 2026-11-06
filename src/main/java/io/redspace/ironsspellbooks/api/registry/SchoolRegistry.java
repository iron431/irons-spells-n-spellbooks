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

    public static final DeferredHolder<SchoolType, SchoolType> FIRE = registerSchool("fire", () -> SchoolType.builder()
            .focus(ModTags.FIRE_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.fire").withStyle(ChatFormatting.GOLD))
            .powerComponent(SpellcastingComponentTypes.FIRE_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.FIRE_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.FIRE_MAGIC_RESIST)
            .castSound(SoundRegistry.FIRE_CAST)
            .damageType(ISSDamageTypes.FIRE_MAGIC)
            .build());

    public static final DeferredHolder<SchoolType, SchoolType> ICE = registerSchool("ice", () -> SchoolType.builder()
            .focus(ModTags.ICE_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.ice").withStyle(Style.EMPTY.withColor(0xd0f9ff)))
            .powerComponent(SpellcastingComponentTypes.ICE_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.ICE_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.ICE_MAGIC_RESIST)
            .castSound(SoundRegistry.ICE_CAST)
            .damageType(ISSDamageTypes.ICE_MAGIC)
            .build());

    public static final DeferredHolder<SchoolType, SchoolType> LIGHTNING = registerSchool("lightning", () -> SchoolType.builder()
            .focus(ModTags.LIGHTNING_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.lightning").withStyle(ChatFormatting.AQUA))
            .powerComponent(SpellcastingComponentTypes.LIGHTNING_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.LIGHTNING_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.LIGHTNING_MAGIC_RESIST)
            .castSound(SoundRegistry.LIGHTNING_CAST)
            .damageType(ISSDamageTypes.LIGHTNING_MAGIC)
            .build());

    public static final DeferredHolder<SchoolType, SchoolType> HOLY = registerSchool("holy", () -> SchoolType.builder()
            .focus(ModTags.HOLY_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.holy").withStyle(Style.EMPTY.withColor(0xfff8d4)))
            .powerComponent(SpellcastingComponentTypes.HOLY_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.HOLY_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.HOLY_MAGIC_RESIST)
            .castSound(SoundRegistry.HOLY_CAST)
            .damageType(ISSDamageTypes.HOLY_MAGIC)
            .build());

    public static final DeferredHolder<SchoolType, SchoolType> ENDER = registerSchool("ender", () -> SchoolType.builder()
            .focus(ModTags.ENDER_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.ender").withStyle(ChatFormatting.LIGHT_PURPLE))
            .powerComponent(SpellcastingComponentTypes.ENDER_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.ENDER_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.ENDER_MAGIC_RESIST)
            .castSound(SoundRegistry.ENDER_CAST)
            .damageType(ISSDamageTypes.ENDER_MAGIC)
            .build());

    public static final DeferredHolder<SchoolType, SchoolType> BLOOD = registerSchool("blood", () -> SchoolType.builder()
            .focus(ModTags.BLOOD_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.blood").withStyle(ChatFormatting.DARK_RED))
            .powerComponent(SpellcastingComponentTypes.BLOOD_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.BLOOD_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.BLOOD_MAGIC_RESIST)
            .castSound(SoundRegistry.BLOOD_CAST)
            .damageType(ISSDamageTypes.BLOOD_MAGIC)
            .build());

    public static final DeferredHolder<SchoolType, SchoolType> EVOCATION = registerSchool("evocation", () -> SchoolType.builder()
            .focus(ModTags.EVOCATION_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.evocation").withStyle(ChatFormatting.WHITE))
            .powerComponent(SpellcastingComponentTypes.EVOCATION_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.EVOCATION_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.EVOCATION_MAGIC_RESIST)
            .castSound(SoundRegistry.EVOCATION_CAST)
            .damageType(ISSDamageTypes.EVOCATION_MAGIC)
            .build());

    public static final DeferredHolder<SchoolType, SchoolType> NATURE = registerSchool("nature", () -> SchoolType.builder()
            .focus(ModTags.NATURE_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.nature").withStyle(ChatFormatting.GREEN))
            .powerComponent(SpellcastingComponentTypes.NATURE_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.NATURE_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.NATURE_MAGIC_RESIST)
            .castSound(SoundRegistry.NATURE_CAST)
            .damageType(ISSDamageTypes.NATURE_MAGIC)
            .build());

    public static final DeferredHolder<SchoolType, SchoolType> ELDRITCH = registerSchool("eldritch", () -> SchoolType.builder()
            .focus(ModTags.ELDRITCH_FOCUS)
            .displayName(Component.translatable("school.irons_spellbooks.eldritch").withStyle(Style.EMPTY.withColor(0x0f839c)))
            .powerComponent(SpellcastingComponentTypes.ELDRITCH_POWER_MULTIPLIER)
            .powerAttribute(AttributeRegistry.ELDRITCH_SPELL_POWER)
            .resistanceAttribute(AttributeRegistry.ELDRITCH_MAGIC_RESIST)
            .castSound(SoundRegistry.EVOCATION_CAST)
            .damageType(ISSDamageTypes.ELDRITCH_MAGIC)
            .requiresLearning()
            .noLooting()
            .build());

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
