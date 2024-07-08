package io.redspace.ironsspellbooks.entity.mobs.wizards;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

@EventBusSubscriber
public class WizardAIEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(ModTags.GUARDED_BY_WIZARDS)) {
            angerNearbyWizards(event.getPlayer(), 3, false, true);
        }
    }

    @SubscribeEvent
    public static void onBlockUsed(PlayerInteractEvent.RightClickBlock event) {
        var blockstate = event.getLevel().getBlockState(event.getHitVec().getBlockPos());
        if (blockstate.is(ModTags.GUARDED_BY_WIZARDS)) {
            //If the block being guarded can hold a loot table, we only anger if it does. (Ie you looted a chest of theirs, versus opening a generic chest while in their vicinity)
            if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof RandomizableContainerBlockEntity randomizableContainerBlockEntity) || randomizableContainerBlockEntity.getLootTable() != null) {
                angerNearbyWizards(event.getEntity(), 1, false, true);
            }
        }
    }

    public static void angerNearbyWizards(Player player, int angerLevel, boolean requireLineOfSight, boolean blockRelated) {
        if (player.getAbilities().instabuild) {
            return;
        }
        List<NeutralWizard> list = player.level.getEntitiesOfClass(NeutralWizard.class, player.getBoundingBox().inflate(16.0D));
        list.stream().filter((neutralWizard) -> (neutralWizard.guardsBlocks() || !blockRelated) && (!requireLineOfSight || BehaviorUtils.canSee(neutralWizard, player))).forEach((neutralWizard) -> {
            neutralWizard.increaseAngerLevel(angerLevel);
            neutralWizard.setPersistentAngerTarget(player.getUUID());
        });
    }

}
