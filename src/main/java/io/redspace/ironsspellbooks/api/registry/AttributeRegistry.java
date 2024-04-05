package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.CreativeModeTab;
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

    public static final RegistryObject<Attribute> MAX_MANA = ATTRIBUTES.register("max_mana", () -> (new MagicRangedAttribute("attribute.irons_spellbooks.max_mana", 100.0D, 0.0D, 1000000.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> MANA_REGEN = ATTRIBUTES.register("mana_regen", () -> (new MagicRangedAttribute("attribute.irons_spellbooks.mana_regen", 1.0D, 0.0D, 100.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> COOLDOWN_REDUCTION = ATTRIBUTES.register("cooldown_reduction", () -> (new MagicRangedAttribute("attribute.irons_spellbooks.cooldown_reduction", 1.0D, -100.0D, 100.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> SPELL_POWER = ATTRIBUTES.register("spell_power", () -> (new MagicRangedAttribute("attribute.irons_spellbooks.spell_power", 1.0D, -100, 100.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> SPELL_RESIST = ATTRIBUTES.register("spell_resist", () -> (new MagicRangedAttribute("attribute.irons_spellbooks.spell_resist", 1.0D, -100, 100.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> CAST_TIME_REDUCTION = ATTRIBUTES.register("cast_time_reduction", () -> (new MagicRangedAttribute("attribute.irons_spellbooks.cast_time_reduction", 1.0D, -100, 100.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> SUMMON_DAMAGE = ATTRIBUTES.register("summon_damage", () -> (new MagicRangedAttribute("attribute.irons_spellbooks.summon_damage", 1.0D, -100, 100.0D).setSyncable(true)));

    public static final RegistryObject<Attribute> FIRE_MAGIC_RESIST = newResistanceAttribute("fire");
    public static final RegistryObject<Attribute> ICE_MAGIC_RESIST = newResistanceAttribute("ice");
    public static final RegistryObject<Attribute> LIGHTNING_MAGIC_RESIST = newResistanceAttribute("lightning");
    public static final RegistryObject<Attribute> HOLY_MAGIC_RESIST = newResistanceAttribute("holy");
    public static final RegistryObject<Attribute> ENDER_MAGIC_RESIST = newResistanceAttribute("ender");
    public static final RegistryObject<Attribute> BLOOD_MAGIC_RESIST = newResistanceAttribute("blood");
    public static final RegistryObject<Attribute> EVOCATION_MAGIC_RESIST = newResistanceAttribute("evocation");
    public static final RegistryObject<Attribute> NATURE_MAGIC_RESIST = newResistanceAttribute("nature");
    public static final RegistryObject<Attribute> ELDRITCH_MAGIC_RESIST = newResistanceAttribute("eldritch");

    public static final RegistryObject<Attribute> FIRE_SPELL_POWER = newPowerAttribute("fire");
    public static final RegistryObject<Attribute> ICE_SPELL_POWER = newPowerAttribute("ice");
    public static final RegistryObject<Attribute> LIGHTNING_SPELL_POWER = newPowerAttribute("lightning");
    public static final RegistryObject<Attribute> HOLY_SPELL_POWER = newPowerAttribute("holy");
    public static final RegistryObject<Attribute> ENDER_SPELL_POWER = newPowerAttribute("ender");
    public static final RegistryObject<Attribute> BLOOD_SPELL_POWER = newPowerAttribute("blood");
    public static final RegistryObject<Attribute> EVOCATION_SPELL_POWER = newPowerAttribute("evocation");
    public static final RegistryObject<Attribute> NATURE_SPELL_POWER = newPowerAttribute("nature");
    public static final RegistryObject<Attribute> ELDRITCH_SPELL_POWER = newPowerAttribute("eldritch");

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> e.add(entity, attribute.get())));
    }

    private static RegistryObject<Attribute> newResistanceAttribute(String id) {
        return ATTRIBUTES.register(id + "_magic_resist", () -> (new MagicRangedAttribute("attribute.irons_spellbooks." + id + "_magic_resist", 1.0D, -100, 100).setSyncable(true)));
    }

    private static RegistryObject<Attribute> newPowerAttribute(String id) {
        return ATTRIBUTES.register(id + "_spell_power", () -> (new MagicRangedAttribute("attribute.irons_spellbooks." + id + "_spell_power", 1.0D, -100, 100).setSyncable(true)));
    }
}
