package io.redspace.ironsspellbooks.entity.mobs.wizards;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        var blockstate = event.getLevel().getBlockState(event.getHitVec().getBlockPos());
        if (blockstate.is(ModTags.GUARDED_BY_WIZARDS)) {
            //If the block being guarded can hold a loot table, we only anger if it does. (Ie you looted a chest of theirs, versus opening a generic chest while in their vicinity)
            if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof RandomizableContainerBlockEntity randomizableContainerBlockEntity) || randomizableContainerBlockEntity.lootTable != null) {
                angerNearbyWizards(event.getEntity(), 1, false, true);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileShot(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (event.getEntity() instanceof Projectile projectile) {
                Vec3 start = projectile.position();
                int searchRange = 16;
                Vec3 end = Utils.raycastForBlock(event.getLevel(), start, projectile.getDeltaMovement().normalize().scale(searchRange).add(start), ClipContext.Fluid.NONE).getLocation();
                serverLevel.getEntitiesOfClass(AbstractSpellCastingMob.class, new AABB(start.x, start.y, start.z, end.x, end.y, end.z)).forEach(castingMob -> {
                    if (Utils.checkEntityIntersecting(castingMob, start, end, 0f).getType()== HitResult.Type.ENTITY) {

                    }
                });
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
