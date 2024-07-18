package io.redspace.ironsspellbooks.entity.spells.gust;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GustCollider extends AbstractConeProjectile {

    public GustCollider(Level level, LivingEntity owner) {
        this(EntityRegistry.GUST_COLLIDER.get(), level);
        this.setOwner(owner);
        IronsSpellbooks.LOGGER.debug("GustCollider<init>: {} {}", owner.getYRot(), owner.getXRot());
        this.setRot(owner.getYRot(), owner.getXRot());
    }

    public GustCollider(EntityType<GustCollider> gustColliderEntityType, Level level) {
        super(gustColliderEntityType, level);
    }

    @Override
    public void spawnParticles() {
        if (!level.isClientSide || tickCount > 2) {
            return;
        }
        Vec3 rotation = this.getLookAngle().normalize();
        var pos = this.position().add(rotation.scale(1.6));

        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        double speed = random.nextDouble() * .4 + .45;
        for (int i = 0; i < 5; i++) {
            double offset = .25;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;
            double angularness = .8;
            Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            level.addParticle(ParticleTypes.POOF, x + ox, y + oy, z + oz, result.x, result.y, result.z);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = getOwner();
        var resultEntity = entityHitResult.getEntity();
        if (entity != null && resultEntity instanceof LivingEntity target && target.distanceToSqr(entity) < range * range) {
            if (!DamageSources.isFriendlyFireBetween(entity, target)) {
                var knockback = new Vec3(entity.getX() - target.getX(), entity.getY() - target.getY(), entity.getZ() - target.getZ()).normalize().scale(-strength);
                target.setDeltaMovement(target.getDeltaMovement().add(knockback));
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffectRegistry.AIRBORNE, 60, amplifier));
            }
        }
    }

    @Override
    public void tick() {
        double x = getX();
        double y = getY();
        double z = getZ();
        if (tickCount > 8)
            discard();
        else
            super.tick();
        setPosRaw(x, y, z);
    }

    @Nullable
    @Override
    public Entity getOwner() {
        if (tickCount >= 1)
            return null;
        return super.getOwner();
    }

    public float strength;
    public float range;
    public int amplifier;

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity pEntity) {
        return super.getAddEntityPacket(pEntity);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }
}
