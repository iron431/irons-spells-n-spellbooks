package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.TickRepresentativeAttribute;
import io.redspace.ironsspellbooks.mixin.AttributeInstanceAccessor;
import io.redspace.skillcasting.api.event.BuildCastContextEvent;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.component.ComponentType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Map;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;


@EventBusSubscriber(modid = IronsSpellbooks.MODID)
public class AttributeRegistry {
    private static final double MILLION = 1_000_000;
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    public static final DeferredHolder<Attribute, Attribute> MAX_MANA = ATTRIBUTES.register("max_mana",
            () -> (new RangedAttribute("attribute.irons_spellbooks.max_mana", 100.0D, 0.0D, MILLION).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> MANA_REGEN = ATTRIBUTES.register("mana_regen",
            () -> (new PercentageAttribute("attribute.irons_spellbooks.mana_regen", 1.0D, 0.0D, 100.0D).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_POWER = ATTRIBUTES.register("spell_power",
            () -> (new PercentageAttribute("attribute.irons_spellbooks.spell_power", 1.0D, -100, 100.0D).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_RESIST = ATTRIBUTES.register("spell_resist",
            () -> (new PercentageAttribute("attribute.irons_spellbooks.spell_resist", 1.0D, -100, 100.0D).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SUMMON_DAMAGE = ATTRIBUTES.register("summon_damage",
            () -> (new PercentageAttribute("attribute.irons_spellbooks.summon_damage", 1.0D, -100, 100.0D).setSyncable(true)));

    public static final DeferredHolder<Attribute, Attribute> SPELL_RADIUS = ATTRIBUTES.register("spell_radius",
            () -> (new RangedAttribute("attribute.irons_spellbooks.spell_radius", 0, -MILLION, MILLION).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_RANGE = ATTRIBUTES.register("spell_range",
            () -> (new RangedAttribute("attribute.irons_spellbooks.spell_range", 0, -MILLION, MILLION).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_PIERCING = ATTRIBUTES.register("spell_piercing",
            () -> (new RangedAttribute("attribute.irons_spellbooks.spell_piercing", 0, -MILLION, MILLION).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_RICOCHET = ATTRIBUTES.register("spell_ricochet",
            () -> (new RangedAttribute("attribute.irons_spellbooks.spell_ricochet", 0, -MILLION, MILLION).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_PROJECTILE_SPEED = ATTRIBUTES.register("spell_projectile_speed",
            () -> (new PercentageAttribute("attribute.irons_spellbooks.spell_projectile_speed", 1.0D, -100, 100.0D).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_DAMAGE_OVER_TIME = ATTRIBUTES.register("spell_damage_over_time",
            () -> (new RangedAttribute("attribute.irons_spellbooks.spell_damage_over_time", 0, -MILLION, MILLION).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_HEALING = ATTRIBUTES.register("spell_healing",
            () -> (new RangedAttribute("attribute.irons_spellbooks.spell_healing", 0, -MILLION, MILLION).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> SPELL_EFFECT_DURATION = ATTRIBUTES.register("spell_effect_duration",
            () -> (new TickRepresentativeAttribute("attribute.irons_spellbooks.spell_effect_duration", 0, -MILLION, MILLION).setSyncable(true)));

    public static final DeferredHolder<Attribute, Attribute> FIRE_MAGIC_RESIST = newResistanceAttribute("fire");
    public static final DeferredHolder<Attribute, Attribute> ICE_MAGIC_RESIST = newResistanceAttribute("ice");
    public static final DeferredHolder<Attribute, Attribute> LIGHTNING_MAGIC_RESIST = newResistanceAttribute("lightning");
    public static final DeferredHolder<Attribute, Attribute> HOLY_MAGIC_RESIST = newResistanceAttribute("holy");
    public static final DeferredHolder<Attribute, Attribute> ENDER_MAGIC_RESIST = newResistanceAttribute("ender");
    public static final DeferredHolder<Attribute, Attribute> BLOOD_MAGIC_RESIST = newResistanceAttribute("blood");
    public static final DeferredHolder<Attribute, Attribute> EVOCATION_MAGIC_RESIST = newResistanceAttribute("evocation");
    public static final DeferredHolder<Attribute, Attribute> NATURE_MAGIC_RESIST = newResistanceAttribute("nature");
    public static final DeferredHolder<Attribute, Attribute> ELDRITCH_MAGIC_RESIST = newResistanceAttribute("eldritch");

    public static final DeferredHolder<Attribute, Attribute> FIRE_SPELL_POWER = newPowerAttribute("fire");
    public static final DeferredHolder<Attribute, Attribute> ICE_SPELL_POWER = newPowerAttribute("ice");
    public static final DeferredHolder<Attribute, Attribute> LIGHTNING_SPELL_POWER = newPowerAttribute("lightning");
    public static final DeferredHolder<Attribute, Attribute> HOLY_SPELL_POWER = newPowerAttribute("holy");
    public static final DeferredHolder<Attribute, Attribute> ENDER_SPELL_POWER = newPowerAttribute("ender");
    public static final DeferredHolder<Attribute, Attribute> BLOOD_SPELL_POWER = newPowerAttribute("blood");
    public static final DeferredHolder<Attribute, Attribute> EVOCATION_SPELL_POWER = newPowerAttribute("evocation");
    public static final DeferredHolder<Attribute, Attribute> NATURE_SPELL_POWER = newPowerAttribute("nature");
    public static final DeferredHolder<Attribute, Attribute> ELDRITCH_SPELL_POWER = newPowerAttribute("eldritch");

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> {
            if (!event.has(entity, attribute)) {
                event.add(entity, attribute);
            }
        }));
    }

    private static DeferredHolder<Attribute, Attribute> newResistanceAttribute(String id) {
        return (DeferredHolder<Attribute, Attribute>) ATTRIBUTES.register(id + "_magic_resist", () -> (new PercentageAttribute("attribute.irons_spellbooks." + id + "_magic_resist", 1.0D, -100, 100).setSyncable(true)));
    }

    private static DeferredHolder<Attribute, Attribute> newPowerAttribute(String id) {
        return ATTRIBUTES.register(id + "_spell_power", () -> (new PercentageAttribute("attribute.irons_spellbooks." + id + "_spell_power", 1.0D, -100, 100).setSyncable(true)));
    }

    @SubscribeEvent
    public static void applyAttributesToContext(BuildCastContextEvent.Post event) {
        CastContext castContext = event.context();
        if (!(castContext.skill().value() instanceof AbstractSpell) || !(castContext.entityOwner() instanceof LivingEntity livingEntity)) {
            return;
        }
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.CAST_RADIUS, SPELL_RADIUS, value -> (float) value);
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.CAST_RANGE, SPELL_RANGE, value -> (float) value);
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.TELEPORT_RANGE, SPELL_RANGE, value -> (float) value);
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.PROJECTILE_RICOCHET, SPELL_RICOCHET, value -> (int) value);
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.PROJECTILE_PIERCE, SPELL_PIERCING, value -> (int) value);
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.PROJECTILE_SPEED, SPELL_PROJECTILE_SPEED, value -> (float) value);
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.DOT_DAMAGE, SPELL_DAMAGE_OVER_TIME, value -> (float) value);
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.HEALING, SPELL_HEALING, value -> (float) value);
        applyToComponent(castContext, livingEntity, SkillcastingComponentTypes.EFFECT_DURATION_TICKS, SPELL_EFFECT_DURATION, value -> (int) value);
    }

    private static <T extends Number> void applyToComponent(CastContext castContext, LivingEntity livingEntity, Supplier<ComponentType<T>> componentType, Holder<Attribute> attribute, DoubleFunction<T> cast) {
        AttributeInstance instance = livingEntity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        Map<AttributeModifier.Operation, Map<ResourceLocation, AttributeModifier>> modifiers = ((AttributeInstanceAccessor) instance).irons_spellbooks$modifiersByOperation();
        Collection<AttributeModifier> addValue = modifiers.getOrDefault(AttributeModifier.Operation.ADD_VALUE, Map.of()).values();
        if (!castContext.has(componentType) && addValue.isEmpty()) {
            return;
        }
        double base = castContext.getOrDefault(componentType, cast.apply(0)).doubleValue();
        double add = addValue.stream()
                .mapToDouble(AttributeModifier::amount)
                .sum();
        double multiplyBase = 1 + modifiers.getOrDefault(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Map.of()).values().stream()
                .mapToDouble(AttributeModifier::amount)
                .sum();
        double multiplyTotal = modifiers.getOrDefault(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Map.of()).values().stream()
                .mapToDouble(modifier -> 1 + modifier.amount())
                .reduce(1, (a, b) -> a * b);
        castContext.set(componentType, cast.apply((base + add) * multiplyBase * multiplyTotal));
    }
}
