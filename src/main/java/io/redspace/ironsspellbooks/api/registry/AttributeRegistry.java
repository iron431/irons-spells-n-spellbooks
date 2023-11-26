package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


@Mod.EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeRegistry {

    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    //"attribute.irons_spellbooks.max_mana" seems to be the id for the lang file
    public static final RegistryObject<Attribute> MAX_MANA = ATTRIBUTES.register("max_mana", () -> (new RangedAttribute("attribute.irons_spellbooks.max_mana", 100.0D, 0.0D, 10000.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> MANA_REGEN = ATTRIBUTES.register("mana_regen", () -> (new RangedAttribute("attribute.irons_spellbooks.mana_regen", 1.0D, 0.0D, 10.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> COOLDOWN_REDUCTION = ATTRIBUTES.register("cooldown_reduction", () -> (new RangedAttribute("attribute.irons_spellbooks.cooldown_reduction", 1.0D, -10.0D, 10.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> SPELL_POWER = ATTRIBUTES.register("spell_power", () -> (new RangedAttribute("attribute.irons_spellbooks.spell_power", 1.0D, 0, 10.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> SPELL_RESIST = ATTRIBUTES.register("spell_resist", () -> (new RangedAttribute("attribute.irons_spellbooks.spell_resist", 1.0D, 0, 10.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> CAST_TIME_REDUCTION = ATTRIBUTES.register("cast_time_reduction", () -> (new RangedAttribute("attribute.irons_spellbooks.cast_time_reduction", 1.0D, 0, 10.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> SUMMON_DAMAGE = ATTRIBUTES.register("summon_damage", () -> (new RangedAttribute("attribute.irons_spellbooks.summon_damage", 1.0D, 0, 10.0D).setSyncable(true)));

    public static final RegistryObject<Attribute> FIRE_MAGIC_RESIST = newResistanceAttribute("fire");
    public static final RegistryObject<Attribute> ICE_MAGIC_RESIST = newResistanceAttribute("ice");
    public static final RegistryObject<Attribute> LIGHTNING_MAGIC_RESIST = newResistanceAttribute("lightning");
    public static final RegistryObject<Attribute> HOLY_MAGIC_RESIST = newResistanceAttribute("holy");
    public static final RegistryObject<Attribute> ENDER_MAGIC_RESIST = newResistanceAttribute("ender");
    public static final RegistryObject<Attribute> BLOOD_MAGIC_RESIST = newResistanceAttribute("blood");
    public static final RegistryObject<Attribute> EVOCATION_MAGIC_RESIST = newResistanceAttribute("evocation");
    public static final RegistryObject<Attribute> NATURE_MAGIC_RESIST = newResistanceAttribute("nature");

    public static final RegistryObject<Attribute> FIRE_SPELL_POWER = newPowerAttribute("fire");
    public static final RegistryObject<Attribute> ICE_SPELL_POWER = newPowerAttribute("ice");
    public static final RegistryObject<Attribute> LIGHTNING_SPELL_POWER = newPowerAttribute("lightning");
    public static final RegistryObject<Attribute> HOLY_SPELL_POWER = newPowerAttribute("holy");
    public static final RegistryObject<Attribute> ENDER_SPELL_POWER = newPowerAttribute("ender");
    public static final RegistryObject<Attribute> BLOOD_SPELL_POWER = newPowerAttribute("blood");
    public static final RegistryObject<Attribute> EVOCATION_SPELL_POWER = newPowerAttribute("evocation");
    public static final RegistryObject<Attribute> NATURE_SPELL_POWER = newPowerAttribute("nature");

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> {
            e.add(entity, MAX_MANA.get());
            e.add(entity, MANA_REGEN.get());
            e.add(entity, SPELL_POWER.get());
            e.add(entity, SPELL_RESIST.get());
            e.add(entity, COOLDOWN_REDUCTION.get());
            e.add(entity, CAST_TIME_REDUCTION.get());
            e.add(entity, SUMMON_DAMAGE.get());

            e.add(entity, FIRE_MAGIC_RESIST.get());
            e.add(entity, ICE_MAGIC_RESIST.get());
            e.add(entity, LIGHTNING_MAGIC_RESIST.get());
            e.add(entity, HOLY_MAGIC_RESIST.get());
            e.add(entity, ENDER_MAGIC_RESIST.get());
            e.add(entity, BLOOD_MAGIC_RESIST.get());
            e.add(entity, EVOCATION_MAGIC_RESIST.get());
            e.add(entity, NATURE_MAGIC_RESIST.get());

            e.add(entity, FIRE_SPELL_POWER.get());
            e.add(entity, ICE_SPELL_POWER.get());
            e.add(entity, LIGHTNING_SPELL_POWER.get());
            e.add(entity, HOLY_SPELL_POWER.get());
            e.add(entity, ENDER_SPELL_POWER.get());
            e.add(entity, BLOOD_SPELL_POWER.get());
            e.add(entity, EVOCATION_SPELL_POWER.get());
            e.add(entity, NATURE_SPELL_POWER.get());
        });
    }

    private static RegistryObject<Attribute> newResistanceAttribute(String id) {
        return ATTRIBUTES.register(id + "_magic_resist", () -> (new RangedAttribute("attribute.irons_spellbooks." + id + "_magic_resist", 1.0D, 0, 10).setSyncable(true)));
    }

    private static RegistryObject<Attribute> newPowerAttribute(String id) {
        return ATTRIBUTES.register(id + "_spell_power", () -> (new RangedAttribute("attribute.irons_spellbooks." + id + "_spell_power", 1.0D, 0, 10).setSyncable(true)));
    }
}
