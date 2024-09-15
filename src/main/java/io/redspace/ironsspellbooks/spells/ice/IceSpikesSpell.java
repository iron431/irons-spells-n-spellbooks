package io.redspace.ironsspellbooks.spells.ice;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.ice_spike.IceSpikeEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@AutoSpellConfig
public class IceSpikesSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "ice_spikes");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.spike_count", getCount(spellLevel, caster))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public IceSpikesSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
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
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, (int) (getCount(spellLevel, entity) * 1.25f), .15f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 forward = entity.getForward().multiply(1, 0, 1).normalize();
        Vec3 start = entity.getEyePosition().add(forward.scale(1.5));

        //TODO: damage/effects per spike per size etc
        float damage = getDamage(spellLevel, entity);
        //TODO: scale based on spell attributes
        float minScale = 1f;
        float maxScale = 2f;
        int count = getCount(spellLevel, entity);
        boolean hasTarget = false;
        Vec3 targetPos = Vec3.ZERO;
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            targetPos = castTargetingData.getTargetPosition((ServerLevel) level);
            hasTarget = targetPos != null;
        }
        start = Utils.moveToRelativeGroundLevel(level, start, 1, 3);
        for (int i = 0; i < count; i++) {
            float f = (float) i / count;
            f *= f;
            float scale = Mth.lerp(f, minScale, maxScale);
            Vec3 spawn = start.add(forward.scale(i));
            var ground = Utils.moveToRelativeGroundLevel(level, spawn, 8);
            spawn = ground.subtract(spawn).scale(Mth.clamp(i / 3f, 0, 1)).add(spawn);
            boolean isFinalSpike = i == count - 1 || (hasTarget && spawn.distanceToSqr(targetPos) < 1);
            if (isFinalSpike) {
                //the final spike does full damage, the small spikes to half damage
                scale = maxScale * 1.5f;
            }

            forward = forward.normalize().scale((scale - 1) * .5f + 1).scale(0.8f);
            int delay = i;

            IceSpikeEntity spike = new IceSpikeEntity(level, entity);
            if (i % 2 == count % 2) {
                spike.setSilent(true);
            }
            spike.setSpikeSize(scale);
            spike.moveTo(spawn.add(0, -0.5, 0));
            spike.setWaitTime(delay);
            spike.setDamage(damage * (isFinalSpike ? 1 : 0.5f));
            spike.setYRot((entity.getYRot() - 45 + Utils.random.nextIntBetweenInclusive(-20, 20)));
            level.addFreshEntity(spike);
            if (isFinalSpike) {
                break;
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        float f = this.getCount(spellLevel, mob) * 1.5f;
        return mob.distanceToSqr(target) > (f * f);
    }

    private int getCount(int spellLevel, LivingEntity entity) {
        return 7 + 3 * spellLevel / 2;
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity);
    }
}
