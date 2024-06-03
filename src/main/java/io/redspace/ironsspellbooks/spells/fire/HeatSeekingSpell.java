package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.*;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.eldritch_blast.EldritchBlastVisualEntity;
import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.eldritch.AbstractEldritchSpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class HeatSeekingSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "heat_seeking");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", (int) (getRecastCount(spellLevel, caster)))
        );
    }

    public HeatSeekingSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 80;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 5;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        IronsSpellbooks.LOGGER.debug("Heat Seeking checkPreCastConditions");
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 64, .15f);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        IronsSpellbooks.LOGGER.debug("Heat Seeking onCast");
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetEntityCastData) {
            IronsSpellbooks.LOGGER.debug("TargetData: {}", targetEntityCastData.getTargetUUID());
            var recasts = playerMagicData.getPlayerRecasts();
            if (!recasts.hasRecastForSpell(getSpellId())) {
                recasts.addRecast(new RecastInstance(getSpellId(), spellLevel, getRecastCount(spellLevel, entity), 80, castSource, new MultiTargetEntityCastData()), playerMagicData);
            }
            var instance = recasts.getRecastInstance(this.getSpellId());
            IronsSpellbooks.LOGGER.debug("instance: {}", instance.toString());
            if (instance != null && instance.getCastData() instanceof MultiTargetEntityCastData targetingData) {
                targetingData.addTarget(targetEntityCastData.getTargetUUID());
                IronsSpellbooks.LOGGER.debug("total targets:");
                targetingData.getTargets().forEach(u -> IronsSpellbooks.LOGGER.debug("{}", u));

            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        var level = serverPlayer.level;
        Vec3 origin = serverPlayer.getEyePosition().add(serverPlayer.getForward().normalize().scale(.2f));
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 2.0f, 1.0f);
        if (castDataSerializable instanceof MultiTargetEntityCastData targetingData) {
            targetingData.getTargets().forEach(uuid -> {
                var target = (LivingEntity) ((ServerLevel) serverPlayer.level).getEntity(uuid);
                if (target != null) {
                    SmallMagicFireball fireball = new SmallMagicFireball(level, serverPlayer);
                    fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
                    var vec = target.getBoundingBox().getCenter().subtract(serverPlayer.getEyePosition()).normalize();
                    var inaccuracy = (float) Mth.clampedLerp(.2f, 1.4f, target.position().distanceToSqr(serverPlayer.position()) / (32 * 32));
                    fireball.shoot(vec.scale(.75f), inaccuracy);
                    fireball.setDamage(getDamage(recastInstance.getSpellLevel(), serverPlayer));
                    fireball.setHomingTarget(target);
                    level.addFreshEntity(fireball);
                }
            });
        }
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new MultiTargetEntityCastData();
    }
}
