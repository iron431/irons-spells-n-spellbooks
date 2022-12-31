package com.example.testmod.entity;

import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

//https://github.com/TobyNguyen710/kyomod/blob/56d3a9dc6b45f7bc5ecdb0d6de9d201cea2603f5/Mod/build/tmp/expandedArchives/forge-1.19.2-43.1.7_mapped_official_1.19.2-sources.jar_b6309abf8a7e6a853ce50598293fb2e7/net/minecraft/world/entity/projectile/ShulkerBullet.java
//https://github.com/maximumpower55/Aura/blob/1.18/src/main/java/me/maximumpower55/aura/entity/SpellProjectileEntity.java
//https://github.com/CammiePone/Arcanus/blob/1.18-dev/src/main/java/dev/cammiescorner/arcanus/common/entities/MagicMissileEntity.java#L51
//https://github.com/maximumpower55/Aura

public class MagicMissileProjectile extends Projectile implements ItemSupplier {
    private static final double SPEED = 1.5d;
    private static final int EXPIRE_TIME = 1 * 20;
    private int age;

    public MagicMissileProjectile(EntityType<? extends MagicMissileProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public MagicMissileProjectile(EntityType<? extends MagicMissileProjectile> entityType, Level levelIn, double x, double y, double z) {
        super(entityType, levelIn);
        setPos(x, y, z);
    }

    public MagicMissileProjectile(EntityType<? extends MagicMissileProjectile> entityType, Level levelIn, LivingEntity shooter) {
        super(entityType, levelIn);
        setOwner(shooter);
    }

    public MagicMissileProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.MAGIC_MISSILE_PROJECTILE.get(), levelIn);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        kill();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (entityHitResult.getEntity() instanceof LivingEntity target)
            target.causeFallDamage(1, 1, DamageSource.MAGIC);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        age++;
        if (!level.isClientSide) {
            for (int count = 0; count < 16; count++) {
                double x = getX() + (level.random.nextInt(3) - 1) / 4D;
                double y = getY() + 0.2F + (level.random.nextInt(3) - 1) / 4D;
                double z = getZ() + (level.random.nextInt(3) - 1) / 4D;
                double deltaX = (level.random.nextInt(3) - 1) * level.random.nextDouble();
                double deltaY = (level.random.nextInt(3) - 1) * level.random.nextDouble();
                double deltaZ = (level.random.nextInt(3) - 1) * level.random.nextDouble();

                level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, ParticleTypes.END_ROD, true, x, y, z, 1, deltaX, deltaY, deltaZ, .1d));
            }
        }

        if (age > 40)
            kill();
    }
}
