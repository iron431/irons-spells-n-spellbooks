package com.example.testmod.registries;

import com.example.testmod.TestMod;
import com.example.testmod.entity.SlashProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static final RegistryObject<EntityType<?>> SLASH_PROJECTILE = ENTITIES.register("test_projectile", () -> EntityType.Builder.of(SlashProjectile::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).sized(6.0f, 1.0f).updateInterval(20).build("test_projectile"));
}
