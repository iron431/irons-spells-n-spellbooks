package io.redspace.skillcasting.irons_spellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.casting.SyncCastingMobAimingDataPacket;
import io.redspace.ironsspellbooks.network.particles.BloodSiphonParticlesPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.CastingMobAimingData;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class RayOfSiphoningSpell extends AbstractSpellSkill {

    private static final float SIPHON_RANGE = 12f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public RayOfSiphoningSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0.25f;
        this.castTime = 100;
        this.baseManaCost = 8;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, SIPHON_RANGE), 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.RAY_OF_SIPHONING).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, SIPHON_RANGE);
    }

    @Override
    public void onServerCastStart(CastContext castContext) {
        if (castContext.asEntityCaster() instanceof Mob) {
            castContext.set(SpellcastingComponentTypes.CASTING_MOB_AIMING_DATA, new CastingMobAimingData());
        }
    }

    @Override
    public void onServerCastTick(CastContext castContext) {
        CastingMobAimingData aimData = castContext.getOrNull(SpellcastingComponentTypes.CASTING_MOB_AIMING_DATA);
        if (aimData == null || !(castContext.asEntityCaster() instanceof Mob mob)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (target != null) {
            aimData.updateAim(target, 0.15f);
            // fixme: can this be parallelized via new client ticker?
            PacketDistributor.sendToPlayersTrackingEntity(mob, new SyncCastingMobAimingDataPacket(mob.getId(), aimData));
        }
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 forward = castContext.direction();
        CastingMobAimingData aimData = castContext.getOrNull(SpellcastingComponentTypes.CASTING_MOB_AIMING_DATA);
        Entity caster = castContext.asEntityCaster();
        if (aimData != null && caster instanceof Mob mob) {
            // fixme: does this aim construct make sense anymore? surely we just make a "mob aim directon resolver", right?
            forward = aimData.getForward(mob);
        }
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, SIPHON_RANGE);
        Vec3 start = castContext.position(PositionAnchor.CASTING_POSITION);
        var hitResult = RaycastBuilder.begin(level, caster)
                .start(start)
                .end(start.add(forward.scale(range)))
                .checkForBlocks(true)
                .bbInflation(0.15f)
                .filter(Utils::canHitWithRaycast)
                .build();

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            if (target.canBeHitByProjectile()) {
                if (DamageSources.applyDamage(target, castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f),
                        getDamageSource(level, null, caster))) {
                    Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
                    Vec3 casterPos = castContext.position(PositionAnchor.CENTER);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster,
                            new BloodSiphonParticlesPacket(targetPos, casterPos));
                }
            }
        }
    }

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of((caster, data, cast) -> {
                    HitResult hit = RaycastBuilder.fromCast(cast.context(), PositionAnchor.CASTING_POSITION, cast.context().getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 15f))
                            .checkForBlocks(true)
                            .build();
                    Vec3 impact = hit.getLocation().subtract(0, .25, 0);
                    for (int i = 0; i < 8; i++) {
                        Vec3 motion = new Vec3(
                                Utils.getRandomScaled(.2f),
                                Utils.getRandomScaled(.2f),
                                Utils.getRandomScaled(.2f)
                        );
                        caster.level().addParticle(ParticleHelper.SIPHON, impact.x + motion.x, impact.y + motion.y, impact.z + motion.z, motion.x, motion.y, motion.z);
                    }
                }
        );
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setLifestealPercent(1f).indirect();
    }

    @Override
    public boolean shouldAIStopCasting(ActiveCast cast, Mob mob, LivingEntity target) {
        float range = cast.context().getOrDefault(SkillcastingComponentTypes.CAST_RANGE, SIPHON_RANGE);
        return mob.distanceToSqr(target) > range * range * 1.2;
    }
}
