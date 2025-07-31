package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class ThunderStepSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "thunder_step");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(spellLevel, caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(8)
            .build();

    public ThunderStepSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 75;
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
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ILLUSIONER_PREPARE_BLINDNESS);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var teleportData = (TeleportSpell.TeleportData) playerMagicData.getAdditionalCastData();
        Vec3 dest = null;
        if (teleportData != null) {
            var potentialTarget = teleportData.getTeleportTargetPosition();
            if (potentialTarget != null) {
                dest = potentialTarget;
            }
        }

        if (dest == null) {
            dest = TeleportSpell.findTeleportLocation(level, entity, getDistance(spellLevel, entity));
        }

        zapEntitiesBetween(entity, spellLevel, dest);
        Vec3 travel = dest.subtract(entity.position());
        for (int i = 0; i < 7; i++) {
            Vec3 random1 = Utils.getRandomVec3(0.5f).multiply(entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth());
            Vec3 random2 = Utils.getRandomVec3(0.8f).multiply(entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth());
            float yOffset = i / 7f * entity.getBbHeight();
            Vec3 midpoint = entity.position().add(travel.scale(0.5f)).add(random2);
            ((ServerLevel) level).sendParticles(new ZapParticleOption(random1.add(entity.getX(), entity.getY() + yOffset, entity.getZ())), midpoint.x, midpoint.y, midpoint.z, 1, 0, 0, 0, 0);
            ((ServerLevel) level).sendParticles(new ZapParticleOption(random1.scale(-1f).add(dest.x, dest.y + yOffset, dest.z)), midpoint.x, midpoint.y, midpoint.z, 1, 0, 0, 0, 0);
        }


        if (entity.isPassenger()) {
            entity.stopRiding();
        }
        entity.teleportTo(dest.x, dest.y, dest.z);
        entity.resetFallDistance();

        playerMagicData.resetAdditionalCastData();

        entity.playSound(getCastFinishSound().get(), 2.0f, 1.0f);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void zapEntitiesBetween(LivingEntity caster, int spellLevel, Vec3 blockEnd) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = blockEnd.add(0, caster.getEyeHeight(), 0);
        AABB range = caster.getBoundingBox().expandTowards(end.subtract(start));
        List<? extends Entity> entities = caster.level.getEntities(caster, range);
        for (Entity target : entities) {
            Vec3 height = new Vec3(0, caster.getEyeHeight(), 0);
            //Raycast from eyes and from feet. Rectangular zone of zapping.
            if (Utils.checkEntityIntersecting(target, start, end, 1f).getType() != HitResult.Type.MISS
                    || Utils.checkEntityIntersecting(target, start.subtract(height), end.subtract(height), 1f).getType() != HitResult.Type.MISS) {
                DamageSources.applyDamage(target, getDamage(spellLevel, caster), this.getDamageSource(caster));
            }
        }
    }

    private float getDistance(int spellLevel, LivingEntity sourceEntity) {
        return getSpellPower(spellLevel, sourceEntity);
    }

    private float getDamage(int spellLevel, LivingEntity sourceEntity) {
        return getSpellPower(spellLevel, sourceEntity);
    }

}
