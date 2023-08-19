package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EarthquakeAoe extends AoeEntity implements AntiMagicSusceptible {
    public static List<EarthquakeAoe> clientEarthquakeOrigins = new ArrayList<>();

    public EarthquakeAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.reapplicationDelay = 25;
        this.setCircular();
        if (this.level.isClientSide) {
            clientEarthquakeOrigins.add(this);
        }
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
            int intensity = (int) (radius * radius * .09f);
            for (int i = 0; i < intensity; i++) {
                Vec3 vec3 = this.position().add(uniformlyDistributedPointInRadius(radius));
                BlockPos blockPos = new BlockPos(Utils.moveToRelativeGroundLevel(level, vec3, 4)).below();
                createTremorBlock(blockPos, .1f + random.nextFloat() * .2f);
            }
            if (waveAnim >= 0) {
                var circumference = waveAnim * 2 * 3.14f;
                int blocks = (int) circumference;
                float anglePerBlock = 360f / blocks;
                for (int i = 0; i < blocks; i++) {
                    Vec3 vec3 = new Vec3(
                            waveAnim * Mth.cos(anglePerBlock * i),
                            0,
                            waveAnim * Mth.sin(anglePerBlock * i)
                    );
                    BlockPos blockPos = new BlockPos(Utils.moveToRelativeGroundLevel(level, position().add(vec3), 4)).below();
                    createTremorBlock(blockPos, .1f + random.nextFloat() * .2f);
                }
                if (waveAnim++ >= radius) {
                    waveAnim = -1;
                    if (tickCount + reapplicationDelay >= duration) {
                        this.discard();
                        //end ourselves smoothly with the last bang instead of timing out awkwardly
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

    protected void createTremorBlock(BlockPos blockPos, float impulseStrength) {
        var fallingblockentity = new VisualFallingBlockEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), level.getBlockState(blockPos));
        fallingblockentity.setDeltaMovement(0, impulseStrength, 0);
        level.addFreshEntity(fallingblockentity);
        if (!level.getBlockState(blockPos.above()).isAir()) {
            createTremorBlock(blockPos.above(), impulseStrength);
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
    public void onClientRemoval() {
        if (level.isClientSide) {
            clientEarthquakeOrigins.remove(this);
        }
        super.onClientRemoval();
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 3F);
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.ENTITY_EFFECT;
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
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void handleCameraShake(ViewportEvent.ComputeCameraAngles event) {
        if (clientEarthquakeOrigins.isEmpty()) {
            return;
        }
        var player = event.getCamera().getEntity();
        List<EarthquakeAoe> closestPositions = clientEarthquakeOrigins.stream().sorted((o1, o2) -> (int) (o1.position().distanceToSqr(player.position()) - o2.position().distanceToSqr(player.position()))).toList();
        var closestPos = closestPositions.get(0).position();
        //.0039f is 1/15^2
        float intensity = (float) Mth.clampedLerp(1, 0, closestPos.distanceToSqr(player.position()) * 0.0039f);
        float f = (float) (player.tickCount + event.getPartialTick());
        float yaw = Mth.cos(f * 1.5f) * intensity * .35f;
        float pitch = Mth.cos(f * 2f) * intensity * .35f;
        float roll = Mth.sin(f * 2.2f) * intensity * .35f;
        event.setYaw(event.getYaw() + yaw);
        event.setRoll(event.getRoll() + roll);
        event.setPitch(event.getPitch() + pitch);
    }
}
