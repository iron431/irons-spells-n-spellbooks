package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.TickRepresentativeAttribute;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.component.ComponentType;
import io.redspace.skillcasting.api.event.BuildCastContextEvent;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
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
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> e.add(entity, attribute)));
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
        if (!(castContext.skill().value() instanceof AbstractSpell spell) || !(castContext.asEntityCaster() instanceof LivingEntity livingEntity)) {
            return;
        }
        modifyComponentAsBase(castContext, SkillcastingComponentTypes.CAST_RADIUS, livingEntity.getAttribute(AttributeRegistry.SPELL_RADIUS));
        modifyComponentAsBase(castContext, SkillcastingComponentTypes.CAST_RANGE, livingEntity.getAttribute(AttributeRegistry.SPELL_RANGE));
        modifyComponentAsBase(castContext, SkillcastingComponentTypes.TELEPORT_RANGE, livingEntity.getAttribute(AttributeRegistry.SPELL_RANGE));
        simpleAddition(castContext, livingEntity, SkillcastingComponentTypes.PROJECTILE_RICOCHET, AttributeRegistry.SPELL_RICOCHET, 0);
        simpleAddition(castContext, livingEntity, SkillcastingComponentTypes.PROJECTILE_PIERCE, AttributeRegistry.SPELL_PIERCING, 0);
        simpleScale(castContext, livingEntity, SkillcastingComponentTypes.PROJECTILE_SPEED, AttributeRegistry.SPELL_PROJECTILE_SPEED, 1.0f);
        modifyComponentAsBase(castContext, SkillcastingComponentTypes.DOT_DAMAGE, livingEntity.getAttribute(AttributeRegistry.SPELL_DAMAGE_OVER_TIME));
        modifyComponentAsBase(castContext, SkillcastingComponentTypes.HEALING, livingEntity.getAttribute(AttributeRegistry.SPELL_HEALING));
        modifyComponentAsBaseInt(castContext, SkillcastingComponentTypes.EFFECT_DURATION_TICKS, livingEntity.getAttribute(AttributeRegistry.SPELL_EFFECT_DURATION));
    }

    public static void simpleAddition(CastContext castContext, LivingEntity livingEntity, Supplier<ComponentType<Integer>> componentType, Holder<Attribute> attribute, int defaultValue) {
        int value = (int) livingEntity.getAttributeValue(attribute);
        if (value == 0) {
            return;
        }
        castContext.set(componentType, castContext.getOrDefault(componentType, defaultValue) + value);
    }

    public static void simpleScale(CastContext castContext, LivingEntity livingEntity, Supplier<ComponentType<Float>> componentType, Holder<Attribute> attribute, float defaultValue) {
        float value = (float) livingEntity.getAttributeValue(attribute);
        if (value == 0) {
            return;
        }
        castContext.set(componentType, castContext.getOrDefault(componentType, defaultValue) * value);
    }

    public static void modifyComponentAsBase(CastContext castContext, Supplier<ComponentType<Float>> componentType, @Nullable AttributeInstance attribute) {
        if (attribute == null) {
            return;
        }
        castContext.mutate(componentType,
                value -> {
                    AttributeInstance simulated = new AttributeInstance(attribute.getAttribute(), (atr) -> {
                    });
                    simulated.replaceFrom(attribute);
                    simulated.setBaseValue(simulated.getBaseValue() + value.doubleValue());
                    return (float) simulated.getValue();
                }
        );
    }

    public static void modifyComponentAsBaseInt(CastContext castContext, Supplier<ComponentType<Integer>> componentType, @Nullable AttributeInstance attribute) {
        if (attribute == null) {
            return;
        }
        castContext.mutate(componentType,
                value -> {
                    AttributeInstance simulated = new AttributeInstance(attribute.getAttribute(), (atr) -> {
                    });
                    simulated.replaceFrom(attribute);
                    simulated.setBaseValue(simulated.getBaseValue() + value.doubleValue());
                    return (int) simulated.getValue();
                }
        );
    }
}
