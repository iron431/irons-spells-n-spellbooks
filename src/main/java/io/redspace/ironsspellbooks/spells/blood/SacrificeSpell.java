package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.network.casting.SyncTargetingDataPacket;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SacrificeSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sacrifice");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(1)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.base_damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", 3)
        );
    }

    public SacrificeSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
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
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        float aimAssist = .25f;
        float range = 25f;
        Vec3 start = entity.getEyePosition();
        Vec3 end = entity.getLookAngle().normalize().scale(range).add(start);
        var target = RaycastBuilder.begin(entity.level, entity)
                .start(start)
                .end(end)
                .checkForBlocks(true)
                .bbInflation(aimAssist)
                .filter(e -> e instanceof IMagicSummon summon && summon.getSummoner() == entity)
                .build();
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            playerMagicData.setAdditionalCastData(new TargetEntityCastData(livingTarget));
            if (entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncTargetingDataPacket(livingTarget, this));
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.spell_target_success", livingTarget.getDisplayName().getString(), this.getDisplayName(serverPlayer)).withStyle(ChatFormatting.GREEN)));
            }
            return true;
        } else if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.sacrifice_target_failure").withStyle(ChatFormatting.RED)));
        }
        return false;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) level);
            if (targetEntity instanceof IMagicSummon summon && summon.getSummoner().getUUID().equals(entity.getUUID())) {
                float damage = getDamage(spellLevel, entity) + targetEntity.getHealth() * .5f;
                float explosionRadius = getRadius(targetEntity);
                doSacrificeExplosion(level, getDamageSource(targetEntity, entity), damage, explosionRadius, targetEntity.getBoundingBox().getCenter());
                targetEntity.remove(Entity.RemovalReason.KILLED);
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static void doSacrificeExplosion(Level level, DamageSource damageSource, float damage, float explosionRadius, Vec3 pos) {
        MagicManager.spawnParticles(level, ParticleHelper.BLOOD, pos.x, pos.y, pos.z, 100, .03, .4, .03, .4, true);
        MagicManager.spawnParticles(level, ParticleHelper.BLOOD, pos.x, pos.y, pos.z, 100, .03, .4, .03, .4, false);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(SchoolRegistry.BLOOD.get().getTargetingColor(), explosionRadius), pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, true);
        var entities = level.getEntities(null, AABB.ofSize(pos, explosionRadius, explosionRadius, explosionRadius));
        for (Entity victim : entities) {
            double distanceSqr = victim.distanceToSqr(pos);
            if (victim.canBeHitByProjectile() && distanceSqr < explosionRadius * explosionRadius && Utils.hasLineOfSight(level, pos, victim.getBoundingBox().getCenter(), true)) {
                float p = (float) (distanceSqr / (explosionRadius * explosionRadius));
                p = 1 - p * p * p;
                DamageSources.applyDamage(victim, damage * p, damageSource);
            }
        }
        CameraShakeManager.addCameraShake(new CameraShakeData(level, 10, pos, 20));
        level.playSound(null, BlockPos.containing(pos), SoundRegistry.BLOOD_EXPLOSION.get(), SoundSource.PLAYERS, 3, Utils.random.nextIntBetweenInclusive(8, 12) * .1f);
    }

    public float getDamage(int spellLevel, @Nullable LivingEntity caster) {
        return (10 + getSpellPower(spellLevel, caster)) *
                (caster == null ? 1f : (float) caster.getAttributeValue(AttributeRegistry.SUMMON_DAMAGE));
    }

    public float getRadius(LivingEntity target) {
        return 3f * (1f + 0.5f * target.getHealth() / target.getMaxHealth());
    }
}
