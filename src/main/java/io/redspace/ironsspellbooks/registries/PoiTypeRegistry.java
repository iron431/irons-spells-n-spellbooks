package io.redspace.ironsspellbooks.registries;


import com.google.common.collect.ImmutableSet;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;


public class PoiTypeRegistry {
    private static final DeferredRegister<PoiType> POIS = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        POIS.register(eventBus);
    }

    public static final RegistryObject<PoiType> CINDEROUS_KEYSTONE_POI = POIS.register("cinderous_soul_rune", ()->new PoiType(getBlockStates(BlockRegistry.CINDEROUS_KEYSTONE.get()),1,1));

    private static Set<BlockState> getBlockStates(Block pBlock) {
        return ImmutableSet.copyOf(pBlock.getStateDefinition().getPossibleStates());
    }
}
