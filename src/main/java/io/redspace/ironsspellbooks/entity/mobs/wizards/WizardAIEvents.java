package io.redspace.ironsspellbooks.entity.mobs.wizards;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class WizardAIEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(ModTags.GUARDED_BY_WIZARDS)) {
            angerNearbyWizards(event.getPlayer(), 3, false, true);
        }
    }

    @SubscribeEvent
    public static void onBlockUsed(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockState(event.getHitVec().getBlockPos()).is(ModTags.GUARDED_BY_WIZARDS)) {
            angerNearbyWizards(event.getEntity(), 1, false, true);
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
