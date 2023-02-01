package com.example.testmod.registries;

import com.example.testmod.TestMod;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeRegistry {

    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    //"attribute.testmod.max_mana" seems to be the id for the lang file
    public static final RegistryObject<Attribute> MAX_MANA = ATTRIBUTES.register("max_mana", () -> (new RangedAttribute("attribute.testmod.max_mana", 100.0D, 0.0D, 10000.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> COOLDOWN_REDUCTION = ATTRIBUTES.register("cooldown_reduction", () -> (new RangedAttribute("attribute.testmod.cooldown_reduction", 1.0D, -10.0D, 2.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> SPELL_POWER = ATTRIBUTES.register("spell_power", () -> (new RangedAttribute("attribute.testmod.spell_power", 1.0D, 1, 2.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> CAST_TIME_REDUCTION = ATTRIBUTES.register("cast_time_reduction", () -> (new RangedAttribute("attribute.testmod.cast_time_reduction", 1.0D, 1, 2.0D).setSyncable(true)));
    public static final RegistryObject<Attribute> BLOOD_MAGIC_RESIST = ATTRIBUTES.register("blood_magic_resist", () -> (new RangedAttribute("attribute.testmod.blood_magic_resist", 1.0D, 1, 2).setSyncable(true)));
    public static final RegistryObject<Attribute> ICE_MAGIC_RESIST = ATTRIBUTES.register("ice_magic_resist", () -> (new RangedAttribute("attribute.testmod.ice_magic_resist", 1.0D, 1, 2).setSyncable(true)));

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> {
            e.add(entity, MAX_MANA.get());
            e.add(entity, SPELL_POWER.get());
            e.add(entity, COOLDOWN_REDUCTION.get());
            e.add(entity, CAST_TIME_REDUCTION.get());
            e.add(entity, BLOOD_MAGIC_RESIST.get());
            e.add(entity, ICE_MAGIC_RESIST.get());
        });
    }
}
