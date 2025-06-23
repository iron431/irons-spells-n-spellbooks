//package io.redspace.ironsspellbooks.registries;
//
//import io.redspace.ironsspellbooks.IronsSpellbooks;
//import io.redspace.ironsspellbooks.worldgen.DegradeSlabsStairsProcessor;
//import io.redspace.ironsspellbooks.worldgen.HandleLitBlocksProcessor;
//import io.redspace.ironsspellbooks.worldgen.StructureFoundationProcessor;
//import io.redspace.ironsspellbooks.worldgen.WeatherCopperProcessor;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.world.level.levelgen.structure.StructureType;
//import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//import net.neoforged.bus.api.IEventBus;
//import net.neoforged.neoforge.registries.DeferredRegister;
//
//import java.util.function.Supplier;
//
//public class StructureTypeRegistry {
//    public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(Registries.STRUCTURE_TYPE, IronsSpellbooks.MODID);
//
//    public static void register(IEventBus eventBus) {
//        STRUCTURES.register(eventBus);
//    }
//
//    public static final Supplier<StructureProcessorType<WeatherCopperProcessor>> ADVANCED_JIGSAW = STRUCTURES.register("advanced_jigsaw", () -> WeatherCopperProcessor.CODEC);
//
//}
