package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.spell.ClientboundBloodSiphonParticles;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class RayOfSiphoningSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "ray_of_siphoning");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getTickDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(spellLevel), 1)));
    }

    public RayOfSiphoningSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 8;
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
    public ResourceLocation getSpellResource() {
        return spellId;
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        var hitResult = Utils.raycastForEntity(level, entity, getRange(0), true, .15f);
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            if (target instanceof LivingEntity) {
                if (DamageSources.applyDamage(target, getTickDamage(spellLevel, entity), getDamageSource(entity), getSchoolType())) {
                    entity.heal(getTickDamage(spellLevel, entity));
                    Messages.sendToPlayersTrackingEntity(new ClientboundBloodSiphonParticles(target.position().add(0, target.getBbHeight() / 2, 0), entity.position().add(0, entity.getBbHeight() / 2, 0)), entity, true);
                }
            }
        }
        super.onCast(level, spellLevel, entity, playerMagicData);
    }

    public static float getRange(int level) {
        return 12;
    }

    private float getTickDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * .25f;
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        return mob.distanceToSqr(target) > (getRange(getLevel(spellLevel, mob)) * getRange(getLevel(spellLevel, mob))) * 1.2;
    }
}
