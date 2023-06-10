package io.redspace.ironsspellbooks.registries;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.event.EventNetworkChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DamageTypeRegistry {
    public static final DeferredRegister<DamageType> DAMAGE_TYPE_REGISTRY = DeferredRegister.create(Registries.DAMAGE_TYPE, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        DAMAGE_TYPE_REGISTRY.register(eventBus);
    }

    public static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(IronsSpellbooks.MODID, name));
    }

    public static final ResourceKey<DamageType> CAULDRON = register("blood_cauldron");
    public static final ResourceKey<DamageType> HEARTSTOP = register("heartstop");

    public static void bootstrap(BootstapContext<DamageType> context) {
        context.register(CAULDRON, new DamageType(CAULDRON.location().getPath(), DamageScaling.NEVER, 0f));
        context.register(HEARTSTOP, new DamageType(HEARTSTOP.location().getPath(), DamageScaling.NEVER, 0f));
    }
}


