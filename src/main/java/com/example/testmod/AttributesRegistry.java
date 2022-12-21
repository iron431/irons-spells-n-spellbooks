package com.example.testmod;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class AttributesRegistry {
    //public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, TestMod.MODID);
    //public static final RegistryObject<Attribute> MAX_MANA = registerAttribute(ATTRIBUTES,TestMod.MODID,"max_mana",(id)->new RangedAttribute(id,100,0,2048).setSyncable(true));
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, TestMod.MODID);
    //public static final RegistryObject<Attribute> MAX_MANA = ATTRIBUTES.register("max_mana",Attribute::new);
    public static final RangedAttribute MAX_MANA = new RangedAttribute("max_mana",100d,0d,1024d);
    public static void register(IEventBus eventBus){
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public void registerAttributes(RegistryEvent.Register<Attribute> e){
        System.out.println("apoth procedure");
        e.getRegistry().registerAll(
                MAX_MANA.setSyncable(true)
        );
    }

    @SubscribeEvent
    public void modifyEntityAttributes(EntityAttributeModificationEvent e){
        e.getTypes().forEach(entity ->{
            e.add(entity, MAX_MANA.setSyncable(true));
        });
        System.out.println("attribute event latched unto");
        return;
        /*
        e.getTypes().forEach(entity->{
            e.add(entity, MAX_MANA.get());
            System.out.println("modifying: "+entity);
        });
        System.out.println(MAX_MANA.get().getRegistryName());
         */

    }
    public static RegistryObject<Attribute> registerAttribute(DeferredRegister<Attribute> registry, String modId, String name, Function<String, Attribute> attribute) {
        RegistryObject<Attribute> registryObject = registry.register(name, () -> attribute.apply("attribute.name." + modId + "." + name));
        return registryObject;
    }

}
