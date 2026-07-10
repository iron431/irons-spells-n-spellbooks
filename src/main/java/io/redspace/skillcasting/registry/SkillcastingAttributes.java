package io.redspace.skillcasting.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcasting.Skillcasting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
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
//    public static final DeferredHolder<Attribute, Attribute> CASTING_MOVESPEED = ATTRIBUTES.register("casting_movespeed",
//            () -> (new MagicPercentAttribute("attribute.irons_spellbooks.casting_movespeed", 1, 0, 100.0D).setSyncable(true)));
}
