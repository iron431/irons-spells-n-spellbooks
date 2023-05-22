package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.network.spell.ClientboundBloodSiphonParticles;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class RayOfSiphoningSpell extends AbstractSpell {
    public RayOfSiphoningSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getTickDamage(caster), 1)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(level), 1)));
    }

    public RayOfSiphoningSpell(int level) {
        super(SpellType.RAY_OF_SIPHONING_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 8;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.RAY_OF_SIPHONING.get());
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        var hitResult = Utils.raycastForEntity(level, entity, getRange(this.level), true, .15f);
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            if (target instanceof LivingEntity) {
                if (DamageSources.applyDamage(target, getTickDamage(entity), getSpellType().getDamageSource(entity), SchoolType.BLOOD)) {
                    entity.heal(getTickDamage(entity));
                    Messages.sendToPlayersTrackingEntity(new ClientboundBloodSiphonParticles(target.position().add(0, target.getBbHeight() / 2, 0), entity.position().add(0, entity.getBbHeight() / 2, 0)), entity, true);
                }
            }
        }
        super.onCast(level, entity, playerMagicData);
    }

    public static float getRange(int level) {
        return 12;
    }

    private float getTickDamage(Entity caster) {
        return getSpellPower(caster) * .25f;
    }

    public static void doRayParticles(LivingEntity livingEntity, int level) {
        int range = (int) (getRange(level) + .85f);
        Vec3 origin = livingEntity.getEyePosition().subtract(0, .25, 0);
        float scalar = 1.25f;
        Vec3 forward = livingEntity.getForward().normalize().scale(1 / scalar);
        for (int i = 1; i < range * scalar; i++) {
            Vec3 pos = origin.add(forward.scale(i).scale(livingEntity.getRandom().nextInt(5, 10) * .1f)).subtract(forward.scale(.25f));
            livingEntity.getLevel().addParticle(ParticleHelper.SIPHON, pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }

    @Override
    public boolean shouldAIStopCasting(AbstractSpellCastingMob mob, LivingEntity target) {
        return mob.distanceToSqr(target) > (getRange(level) * getRange(level)) * 1.2;
    }
}
