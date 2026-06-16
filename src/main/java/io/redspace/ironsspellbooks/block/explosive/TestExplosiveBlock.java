package io.redspace.ironsspellbooks.block.explosive;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.SuspendedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public class TestExplosiveBlock extends Block {
    public TestExplosiveBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onProjectileHit(@NotNull Level level, @NotNull BlockState state, @NotNull BlockHitResult hit, @NotNull Projectile projectile) {
        super.onProjectileHit(level, state, hit, projectile);
        explode(level, hit.getBlockPos(), 5);
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (Utils.getEnchantmentLevel(level, tool, Enchantments.SILK_TOUCH) == 0) {
            explode(level, pos, 5);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void explodeOnTeleport(EntityTeleportEvent event) {
        BlockPos pos = BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ()).below();
        Level level = event.getEntity().level;
        if (level.getBlockState(pos).getBlock() instanceof TestExplosiveBlock) {
            explode(level, pos, 4);
        }
    }

    public static void explode(Level level, BlockPos center, int radius) {
        explode(level, center, radius, true);
    }

    public static void explode(Level level, BlockPos center, int radius, boolean affectEntities) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 centerVec3 = center.getCenter();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (pos.distSqr(center) > radius * radius) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
                continue;
            }
            SuspendedBlockEntity entity = SuspendedBlockEntity.consumeBlock(serverLevel, pos.immutable());
            if (state.getBlock() instanceof TestExplosiveBlock) {
                entity.doExplosion = true;
            }
            Vec3 vector = entity.position().subtract(centerVec3).scale(.33f);
            double distance = vector.length();
            vector = vector.scale(1 / distance).add(Utils.getRandomVec3(0.25)).normalize();
            double speed = Mth.clamp(distance, 0.05, 2);
            entity.setDeltaMovement(vector.scale(speed));
        }
        if (affectEntities) {
            float entityRadius = radius + 2.5f;
            AABB aabb = AABB.ofSize(centerVec3, entityRadius, entityRadius, entityRadius).inflate(1);
            for (Entity entity : serverLevel.getEntities((Entity) null, aabb, entity -> entity.isPickable() || entity instanceof Projectile)) {
                Vec3 vector = entity.position().subtract(centerVec3.subtract(0,2,0));
                vector = vector.normalize();
                double speed = /*Mth.clamp(distance * 0.15, 0.05, 2) + */0.75;
                entity.setDeltaMovement(vector.multiply(speed, speed * 0.25, speed).add(0, 1, 0));
                entity.hurtMarked = true;
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 4, 0));
                }
            }
        }
    }
}
