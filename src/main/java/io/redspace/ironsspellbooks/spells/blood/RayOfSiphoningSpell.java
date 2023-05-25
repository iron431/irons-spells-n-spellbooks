package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.network.spell.ClientboundBloodSiphonParticles;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;

public class RayOfSiphoningSpell extends AbstractSpell {
    public RayOfSiphoningSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getTickDamage(caster), 1)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(0), 1)));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchool(SchoolType.BLOOD)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public RayOfSiphoningSpell(int level) {
        super(SpellType.RAY_OF_SIPHONING_SPELL);
        this.setLevel(level);
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
        var hitResult = Utils.raycastForEntity(level, entity, getRange(0), true, .15f);
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

    @Override
    public boolean shouldAIStopCasting(AbstractSpellCastingMob mob, LivingEntity target) {
        return mob.distanceToSqr(target) > (getRange(getLevel(mob)) * getRange(getLevel(mob))) * 1.2;
    }
}
