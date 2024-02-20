package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.VisualFallingBlockEntity;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EarthquakeAoe extends AoeEntity implements AntiMagicSusceptible {
    public static Map<UUID, EarthquakeAoe> clientEarthquakeOrigins = new HashMap<>();

    public EarthquakeAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.reapplicationDelay = 25;
        this.setCircular();
    }

    public EarthquakeAoe(Level level) {
        this(EntityRegistry.EARTHQUAKE_AOE.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        var damageSource = SpellRegistry.EARTHQUAKE_SPELL.get().getDamageSource(this, getOwner());
        DamageSources.ignoreNextKnockback(target);
        if (target.hurt(damageSource, getDamage())) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, slownessAmplifier));
            target.setDeltaMovement(target.getDeltaMovement().add(0, .5, 0));
            target.hurtMarked = true;
        }
    }

    private CameraShakeData cameraShakeData;
    private int slownessAmplifier;

    public int getSlownessAmplifier() {
        return slownessAmplifier;
    }

    public void setSlownessAmplifier(int slownessAmplifier) {
        this.slownessAmplifier = slownessAmplifier;
    }

    @Override
    public float getParticleCount() {
        return 0f;
    }

    @Override
    public void ambientParticles() {

    }

    int waveAnim = -1;

    @Override
    public void tick() {
        super.tick();
        if (tickCount == 1) {
            createScreenShake();
        }
        if (tickCount % 20 == 1) {
            this.playSound(SoundRegistry.EARTHQUAKE_LOOP.get(), 2f, .9f + random.nextFloat() * .15f);
        }
        if (tickCount % reapplicationDelay == 1) {
            //aligns with damage tick
            waveAnim = 0;
            this.playSound(SoundRegistry.EARTHQUAKE_IMPACT.get(), 1.5f, .9f + random.nextFloat() * .2f);
        }
        if (!level.isClientSide) {
            var radius = this.getRadius();
            var level = this.level;
            int intensity = Math.min((int) (radius * radius * .09f), 15);
            for (int i = 0; i < intensity; i++) {
                Vec3 vec3 = this.position().add(uniformlyDistributedPointInRadius(radius));
                BlockPos blockPos = BlockPos.containing(Utils.moveToRelativeGroundLevel(level, vec3, 4)).below();
                Utils.createTremorBlock(level, blockPos, .1f + random.nextFloat() * .2f);
            }
            if (waveAnim >= 0) {
                var circumference = waveAnim * 2 * 3.14f;
                int blocks = Mth.clamp((int) circumference, 0, 25);
                float anglePerBlock = 360f / blocks;
                for (int i = 0; i < blocks; i++) {
                    Vec3 vec3 = new Vec3(
                            waveAnim * Mth.cos(anglePerBlock * i),
                            0,
                            waveAnim * Mth.sin(anglePerBlock * i)
                    );
                    BlockPos blockPos = BlockPos.containing(Utils.moveToRelativeGroundLevel(level, position().add(vec3), 4)).below();
                    Utils.createTremorBlock(level, blockPos, .1f + random.nextFloat() * .2f);
                }
                if (waveAnim++ >= radius) {
                    waveAnim = -1;
                    if (tickCount + reapplicationDelay >= duration) {
                        //end ourselves smoothly with the last bang instead of timing out awkwardly
                        this.discard();
                    }
                }
            }
        }
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(0, 5, 0);
    }


    protected void createScreenShake() {
        if (!this.level.isClientSide && !this.isRemoved()) {
            this.cameraShakeData = new CameraShakeData(this.duration - this.tickCount, this.position(), 15);
            CameraShakeManager.addCameraShake(cameraShakeData);
        }
    }

    protected Vec3 uniformlyDistributedPointInRadius(float r) {
        var distance = r * (1 - this.random.nextFloat() * this.random.nextFloat());
        var theta = this.random.nextFloat() * 6.282f; // two pi :nerd:
        return new Vec3(
                distance * Mth.cos(theta),
                .2f,
                distance * Mth.sin(theta)
        );
    }

    @Override
    public void remove(RemovalReason pReason) {
        super.remove(pReason);
        if (!level.isClientSide) {
            CameraShakeManager.removeCameraShake(this.cameraShakeData);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 3F);
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Slowness", slownessAmplifier);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.slownessAmplifier = pCompound.getInt("Slowness");
        //IronsSpellbooks.LOGGER.debug("EarthquakeAoe readAdditionalSaveData");
        createScreenShake();
    }
}
