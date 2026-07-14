package io.redspace.skillcasting.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcasting.Skillcasting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = IronsSpellbooks.MODID)
public final class SkillcastingAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, Skillcasting.NAMESPACE);
    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> e.add(entity, attribute)));
    }

    public static final DeferredHolder<Attribute, Attribute> CAST_TIME_REDUCTION = ATTRIBUTES.register("cast_time_reduction",
            () -> (new PercentageAttribute("attribute.irons_spellbooks.cast_time_reduction", 1.0D, -100, 100.0D).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> COOLDOWN_REDUCTION = ATTRIBUTES.register("cooldown_reduction",
            () -> (new PercentageAttribute("attribute.irons_spellbooks.cooldown_reduction", 1.0D, -100.0D, 100.0D).setSyncable(true)));
    public static final DeferredHolder<Attribute, Attribute> CASTING_MOVESPEED = ATTRIBUTES.register("casting_movespeed",
            () -> (new PercentageAttribute("attribute.skillcasting.casting_movespeed", 1, 0, 100.0D).setSyncable(true)));
}
