package io.redspace.ironsspellbooks.item;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EnergizedCoreItem extends Item {
    public EnergizedCoreItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        var level = pContext.getLevel();
        var blockPos = pContext.getClickedPos();
        if (pContext.getPlayer() != null && level.getBlockState(blockPos).is(Blocks.LIGHTNING_ROD)) {
            if (level.isThundering()) {
                if (!level.isClientSide) {
                    if (level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState())) {
                        //consume core
                        var itemstack = pContext.getItemInHand();
                        itemstack.shrink(1);
                        //pContext.getPlayer().getItemInHand(pContext.getPlayer().getUsedItemHand()).shrink(1);
                        Vec3 center = new Vec3(blockPos.getX() + .5, blockPos.getY(), blockPos.getZ() + .5);
                        //boom
                        doLightningBolt(level, center);
                        Explosion.BlockInteraction blockinteraction = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;
                        MagicManager.spawnParticles(level, ParticleHelper.ELECTRIC_SPARKS, center.x, center.y + .5, center.z, 100, .2f, .2f, .2f, 1.5, false);
                        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(new Vector3f(.7f, 1f, 1f), 6), center.x, center.y + .15, center.z, 1, 0, 0, 0, 0, true);
                        level.explode(null, DamageSource.LIGHTNING_BOLT, null, center.x, center.y, center.z, 3, true, blockinteraction);
                        //create electrified item
                        ItemEntity itementity = new ItemEntity(level, center.x, center.y + 1, center.z, new ItemStack(ItemRegistry.LIGHTNING_ROD_STAFF.get()));
                        itementity.setGlowingTag(true);
                        level.addFreshEntity(itementity);
                    }
                }
            } else {
                if (level.isClientSide) {
                    pContext.getPlayer().displayClientMessage(Component.translatable("item.irons_spellbooks.energized_core.failure").withStyle(ChatFormatting.AQUA), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(pContext);
    }

    private void doLightningBolt(Level level, Vec3 pos) {
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
        lightningBolt.setVisualOnly(true);
        lightningBolt.setDamage(0);
        lightningBolt.setPos(pos);
        level.addFreshEntity(lightningBolt);
    }
}
