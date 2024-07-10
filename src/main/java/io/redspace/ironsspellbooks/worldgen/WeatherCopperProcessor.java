package io.redspace.ironsspellbooks.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.StructureProcessorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class WeatherCopperProcessor extends StructureProcessor {

    public static final MapCodec<WeatherCopperProcessor> CODEC = Codec.FLOAT.fieldOf("bias").xmap(WeatherCopperProcessor::new, obj -> obj.bias);
    float bias;

    public WeatherCopperProcessor(float bias) {
        this.bias = bias;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(@Nonnull LevelReader level, @Nonnull BlockPos jigsawPiecePos, @Nonnull BlockPos jigsawPieceBottomCenterPos, @Nonnull StructureTemplate.StructureBlockInfo blockInfoLocal, @Nonnull StructureTemplate.StructureBlockInfo blockInfoGlobal, @Nonnull StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        if (blockInfoGlobal.state().getBlock() instanceof WeatheringCopper copperBlock) {
            float f = Mth.lerp(Utils.random.nextFloat(), bias, 1);
            int weatherStage = (int) (f * 4);
            BlockState state = blockInfoGlobal.state();
            //IronsSpellbooks.LOGGER.debug("WeatherCopperProcessor.original state: {}", state.toString());
            for (int i = 0; i < weatherStage; i++) {
                var nextState = copperBlock.getNext(state);
                if (nextState.isPresent()) {
                    state = nextState.get().getBlock().withPropertiesOf(blockInfoGlobal.state());
                }
                //IronsSpellbooks.LOGGER.debug("WeatherCopperProcessor.nextState: {}", state.toString());
            }
            //IronsSpellbooks.LOGGER.debug("WeatherCopperProcessor.final state: {}", state.toString());

            return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), state, blockInfoGlobal.nbt());
        }

        return blockInfoGlobal;
    }

    @Nonnull
    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorRegistry.WEATHER_COPPER.get();
    }
}