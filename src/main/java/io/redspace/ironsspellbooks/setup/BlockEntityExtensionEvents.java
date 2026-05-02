package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BlockEntityExtensionEvents {

    @SubscribeEvent
    public static void addVaultCompatibleBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.VAULT, BlockRegistry.BONE_VAULT_BLOCK.get());
    }
}
