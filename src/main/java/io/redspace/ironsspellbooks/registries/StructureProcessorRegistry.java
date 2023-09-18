package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.worldgen.RemoveWaterProcessor;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class StructureProcessorRegistry {

    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registry.STRUCTURE_PROCESSOR_REGISTRY, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        PROCESSORS.register(eventBus);
    }

    public static final RegistryObject<StructureProcessorType<RemoveWaterProcessor>> REMOVE_WATER = PROCESSORS.register("remove_water", () -> () -> RemoveWaterProcessor.CODEC);
}
