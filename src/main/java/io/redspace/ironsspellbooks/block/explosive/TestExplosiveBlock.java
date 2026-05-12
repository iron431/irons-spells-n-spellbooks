package io.redspace.ironsspellbooks.block.explosive;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.SuspendedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static void explode(Level level, BlockPos center, int radius) {
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
    }
}
