package io.redspace.skillcasting.irons_spellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.component.MultiTargetEntityCastComponent;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class SacrificeSpell extends AbstractSpellSkill {

    private static final float SACRIFICE_RANGE = 25f;
    private static final float BASE_RADIUS = 3f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(1)
            .build();

    public SacrificeSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.base_damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.radius", BASE_RADIUS)
        );
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float summonDamageMult = 1f;
        if (castContext.asEntityCaster() instanceof LivingEntity living) {
            summonDamageMult = (float) living.getAttributeValue(AttributeRegistry.SUMMON_DAMAGE);
        }
        castContext.set(SkillcastingComponentTypes.DAMAGE, (10 + getSpellPower(castContext)) * summonDamageMult);
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return Optional.empty();
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        HitResult target = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, SACRIFICE_RANGE)
                .checkForBlocks(true)
                .bbInflation(0.25f)
                .filter(e -> e instanceof IMagicSummon summon && summon.getSummoner() == caster)
                .build();
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            castContext.set(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES,
                    new MultiTargetEntityCastComponent(livingTarget));
            if (caster instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("ui.irons_spellbooks.spell_target_success",
                                livingTarget.getDisplayName().getString(),
                                Component.translatable(getDescriptionId())).withStyle(ChatFormatting.GREEN)));
            }
            return true;
        }
        if (caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("ui.irons_spellbooks.sacrifice_target_failure").withStyle(ChatFormatting.RED)));
        }
        return false;
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData == null /*|| caster == null */|| !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity targetEntity = targetData.getFirstLivingEntityTarget(serverLevel);
        // fixme: previously, we verified via entity-strict uuid
        //  not only does that not work,
        //  pre cast conditions will never fire since its also a strict entity check via #getSummoner
        if (targetEntity instanceof IMagicSummon summon /*&& summon.getSummoner().getUUID().equals(caster.getUUID())*/) {
            float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f) + targetEntity.getHealth() * 0.5f;
            float explosionRadius = BASE_RADIUS * (1f + 0.5f * targetEntity.getHealth() / targetEntity.getMaxHealth());
            doSacrificeExplosion(level, getDamageSource(level, targetEntity, castContext.asEntityCaster()), damage, explosionRadius, targetEntity.getBoundingBox().getCenter());
            targetEntity.remove(Entity.RemovalReason.KILLED);
        }
    }

    public static void doSacrificeExplosion(Level level, DamageSource damageSource, float damage, float explosionRadius, Vec3 pos) {
        MagicManager.spawnParticles(level, ParticleHelper.BLOOD, pos.x, pos.y, pos.z, 100, 0.03, 0.4, 0.03, 0.4, true);
        MagicManager.spawnParticles(level, ParticleHelper.BLOOD, pos.x, pos.y, pos.z, 100, 0.03, 0.4, 0.03, 0.4, false);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(SchoolRegistry.BLOOD.get().getTargetingColor(), explosionRadius), pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, true);
        var entities = level.getEntities(null, AABB.ofSize(pos, explosionRadius, explosionRadius, explosionRadius));
        for (Entity victim : entities) {
            double distanceSqr = victim.distanceToSqr(pos);
            if (victim.canBeHitByProjectile() && distanceSqr < explosionRadius * explosionRadius
                    && Utils.hasLineOfSight(level, pos, victim.getBoundingBox().getCenter(), true)) {
                float p = (float) (distanceSqr / (explosionRadius * explosionRadius));
                p = 1 - p * p * p;
                DamageSources.applyDamage(victim, damage * p, damageSource);
            }
        }
       CameraShakeManager.addCameraShake(
                new CameraShakeData(level, 10, pos, 20, 20));
        level.playSound(null, BlockPos.containing(pos), SoundRegistry.BLOOD_EXPLOSION.get(), SoundSource.PLAYERS,
                3, Utils.random.nextIntBetweenInclusive(8, 12) * 0.1f);
    }
}
