package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.ImpulseCastData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;


public class AscensionSpell extends AbstractSpell {
    public AscensionSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(caster), 1)));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchool(SchoolType.LIGHTNING)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public AscensionSpell(int level) {
        super(SpellType.ASCENSION_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new ImpulseCastData();
    }

    @Override
    public void onClientCast(Level level, LivingEntity entity, ICastData castData) {
        if (castData instanceof ImpulseCastData data) {
            entity.hasImpulse = data.hasImpulse;
            double y = Math.max(entity.getDeltaMovement().y, data.y);
            entity.setDeltaMovement(data.x, y, data.z);
        }
        super.onClientCast(level, entity, castData);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, MagicData playerMagicData) {

        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ASCENSION.get(), 80, 0, false, false, true));

        Vec3 vec = entity.position();
        for (int i = 0; i < 32; i++) {
            if (!level.getBlockState(new BlockPos(vec).below()).isAir())
                break;
            vec = vec.subtract(0, 1, 0);
        }
        Vec3 strikePos = vec;

        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
        lightningBolt.setVisualOnly(true);
        lightningBolt.setDamage(0);
        lightningBolt.setPos(strikePos);
        level.addFreshEntity(lightningBolt);

        //livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100));
        float radius = 5;
        level.getEntities(entity, entity.getBoundingBox().inflate(radius)).forEach(target -> {
            double distance = target.distanceToSqr(strikePos);
            if (distance < radius * radius) {
                float finalDamage = (float) (getDamage(entity) * (1 - distance / (radius * radius)));
                DamageSources.applyDamage(target, finalDamage, SpellType.ASCENSION_SPELL.getDamageSource(lightningBolt, entity), SchoolType.LIGHTNING);
                if (target instanceof Creeper creeper)
                    creeper.thunderHit((ServerLevel) level, lightningBolt);
            }
        });

        Vec3 motion = entity.getLookAngle().multiply(1, 0, 1).normalize().add(0, 5, 0).scale(.125);
        playerMagicData.setAdditionalCastData(new ImpulseCastData((float) motion.x, (float) motion.y, (float) motion.z, true));
        entity.setDeltaMovement(entity.getDeltaMovement().add(motion));
        entity.hasImpulse = true;


        super.onCast(level, entity, playerMagicData);
    }

    private int getDamage(LivingEntity caster) {
        return (int) getSpellPower(caster);
    }
}
