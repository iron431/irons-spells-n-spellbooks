package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CounterspellSpell extends AbstractSpell {
    public static final List<MobEffect> MAGICAL_EFFECTS = List.of(MobEffectRegistry.ABYSSAL_SHROUD.get(), MobEffectRegistry.ASCENSION.get(), MobEffectRegistry.ANGEL_WINGS.get(), MobEffectRegistry.CHARGED.get(), MobEffectRegistry.EVASION.get(), MobEffectRegistry.HEARTSTOP.get(), MobEffectRegistry.FORTIFY.get(), MobEffectRegistry.TRUE_INVISIBILITY.get());
    public CounterspellSpell() {
        this(1);
    }

    public CounterspellSpell(int level) {
        super(SpellType.COUNTERSPELL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
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
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        HitResult hitResult = raycast(entity.level, entity);
        Vec3 forward = entity.getForward().normalize();
        if (hitResult instanceof EntityHitResult entityHitResult) {
            double distance = entity.distanceTo(entityHitResult.getEntity());
            for (float i = 1; i < distance; i += .5f) {
                Vec3 pos = entity.getEyePosition().add(forward.scale(i));
                MagicManager.spawnParticles(world, ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
            }
            if (entityHitResult.getEntity() instanceof AntiMagicSusceptible antiMagicSusceptible)
                antiMagicSusceptible.onAntiMagic(playerMagicData);
            else if (entityHitResult.getEntity() instanceof ServerPlayer serverPlayer)
                Utils.serverSideCancelCast(serverPlayer, true);
            else if (entityHitResult.getEntity() instanceof AbstractSpellCastingMob abstractSpellCastingMob)
                abstractSpellCastingMob.cancelCast();

            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity)
                for (MobEffect mobEffect : MAGICAL_EFFECTS)
                    livingEntity.removeEffect(mobEffect);
        }else{
            for (float i = 1; i < 40; i += .5f) {
                Vec3 pos = entity.getEyePosition().add(forward.scale(i));
                MagicManager.spawnParticles(world, ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
            }
        }
        super.onCast(world, entity, playerMagicData);
    }

    private static HitResult raycast(Level level, Entity originEntity) {
        Vec3 start = originEntity.getEyePosition();
        Vec3 end = start.add(originEntity.getForward().normalize().scale(80));
        AABB range = originEntity.getBoundingBox().expandTowards(end.subtract(start));

        List<HitResult> hits = new ArrayList<>();
        List<? extends Entity> entities = level.getEntities(originEntity, range, Utils::validAntiMagicTarget);
        List<Vec3> aimAssist = List.of(new Vec3(0, 0, 0), new Vec3(20, 0, 20), new Vec3(-20, 0, -20), new Vec3(0, 20, 0), new Vec3(0, -20, 0));
        for (Entity target : entities) {
            for (Vec3 vec3 : aimAssist) {
                HitResult hit = Utils.checkEntityIntersecting(target, start, end.add(vec3));
                if (hit.getType() != HitResult.Type.MISS) {
                    hits.add(hit);
                    break;
                }
            }
        }

        if (hits.size() > 0) {
            hits.sort((o1, o2) -> (int) (o1.getLocation().distanceToSqr(start) - o2.getLocation().distanceToSqr(start)));
            return hits.get(0);
        } else {
            return BlockHitResult.miss(end, Direction.UP, new BlockPos(end));
        }
    }

}