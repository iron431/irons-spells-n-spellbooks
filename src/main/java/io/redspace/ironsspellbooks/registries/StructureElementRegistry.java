package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.worldgen.IndividualTerrainStructurePoolElement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class StructureElementRegistry {

    public static final DeferredRegister<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT_DEFERRED_REGISTER = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        STRUCTURE_POOL_ELEMENT_DEFERRED_REGISTER.register(eventBus);
    }

    public static final Supplier<StructurePoolElementType<IndividualTerrainStructurePoolElement>> INDIVIDUAL_TERRAIN_ELEMENT = STRUCTURE_POOL_ELEMENT_DEFERRED_REGISTER.register("individual_terrain_element", () -> () -> IndividualTerrainStructurePoolElement.CODEC);
}
