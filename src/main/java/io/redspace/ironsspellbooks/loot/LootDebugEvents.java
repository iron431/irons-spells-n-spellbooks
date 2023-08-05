package io.redspace.ironsspellbooks.loot;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class LootDebugEvents {

    private static final boolean debugLootTables = false;
    @SubscribeEvent
    public static void alertLootTable(PlayerInteractEvent.RightClickBlock event) {
        if(debugLootTables){
            var blockEntity = event.getLevel().getBlockEntity(event.getHitVec().getBlockPos());
            if (blockEntity instanceof RandomizableContainerBlockEntity chest) {
                var lootTable = chest.lootTable;
                if (lootTable != null) {
                    if (event.getEntity() instanceof ServerPlayer serverPlayer)
                        serverPlayer.sendSystemMessage(Component.literal(chest.lootTable.toString()).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, lootTable.toString()))));
                    IronsSpellbooks.LOGGER.info("{}", chest.lootTable);
                }
            }
        }
    }
}
