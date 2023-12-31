package io.redspace.ironsspellbooks.mixin;

import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.BiomeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;


//@Mixin(OverworldBiomeBuilder.class)
//public abstract class OverworldBiomeBuilderMixin {
//
////    @Shadow
////    @Final
////    private Climate.Parameter FULL_RANGE;
////    private final Climate.Parameter FULL_SPAN = Climate.Parameter.span(-1.0F, 1.0F);
////
////    @Shadow
////    protected abstract void addUndergroundBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pTemperature, Climate.Parameter pHumidity, Climate.Parameter pContinentalness, Climate.Parameter pErosion, Climate.Parameter pDepth, float pWeirdness, ResourceKey<Biome> pKey);
//
//    @Inject(method = "addUndergroundBiomes", at = @At("TAIL"))
//    private void writeCaveBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, CallbackInfo ci) {
////        addUndergroundBiome(consumer, Climate.Parameter.span(0.5F, 1F), FULL_SPAN, Climate.Parameter.span(-1.0F, -0.3F), FULL_SPAN, FULL_SPAN, 0.0F, key("ice_caves"));
////        this.addUndergroundBiome(consumer, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(0.8F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation("irons_spellbooks:ice_caves")));
//        IronsSpellbooks.LOGGER.debug("OverworldBiomeBuilderMixin.writeCaveBiomes");
////        consumer.accept(Pair.of(
////                Climate.parameters(
////                        Climate.Parameter.span(-1.0F, 1.0F),
////                        Climate.Parameter.span(0.7F, 1.0F),
////                        Climate.Parameter.span(-1.0F, 1.0F),
////                        Climate.Parameter.span(0.2F, 0.9F),
////                        Climate.Parameter.span(-1.0F, 1.0F),
////                        Climate.Parameter.span(-1.0F, 1.0F),
////                        0.0F
////                ),
////                BiomeHandler.ICE_CAVES
////        ));
//
//    }
////
////    private static ResourceKey<Biome> key(String id) {
////        return ResourceKey.create(Registry.BIOME_REGISTRY, IronsSpellbooks.id(id));
////    }
//}

@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {

    private final Climate.Parameter FULL_SPAN = Climate.Parameter.span(-1.0F, 1.0F);

    private void addCaveBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, ResourceKey<Biome> biome, Climate.Parameter temperature, Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter weirdness, float offset) {
    }

    @Inject(method = "addUndergroundBiomes", at = @At("TAIL"))
    private void writeCaveBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, CallbackInfo ci) {
        //https://minecraft.fandom.com/wiki/Biome#Temperature_2
        consumer.accept(Pair.of(
                Climate.parameters(
                        Climate.Parameter.span(-1.1F, -0.6F),   // Temperature
                        Climate.Parameter.span(-1F, -0.2F),     // Humidity
                        Climate.Parameter.span(0.7F, 0.8F),     // Continentialness
                        Climate.Parameter.span(0.0F, 0.2F),     // Erosion
                        Climate.Parameter.span(0.9F, 1.1F),     // Depth
                        Climate.Parameter.span(-0.6F, -0.2F),   // Weirdness
                        0.0F),
                BiomeRegistry.ICE_CAVES_KEY));
//        this.addCaveBiome(consumer, ,
//                Climate.Parameter.span(-1.1F, -.6F),    // Temperature
//                Climate.Parameter.span(-1F, -0.2F),     // Humidity
//                Climate.Parameter.span(0.7F, 0.8F),     // Continentialness
//                Climate.Parameter.span(0.0F, 0.2F),     // Erosion
//                Climate.Parameter.span(-0.6F, -0.2F),   // Weirdness
//                0.0F);
    }
}