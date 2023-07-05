package io.redspace.ironsspellbooks.spells.fire;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.spells.fire_breath.FireBreathProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class FireBreathSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "fire_breath");

    public FireBreathSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchool(SchoolType.FIRE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public FireBreathSpell(int level) {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 5;

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
        return Optional.of(SoundRegistry.FIRE_BREATH_LOOP.get());
    }

    @Override
    public void onCast(Level world, LivingEntity entity, MagicData playerMagicData) {

        if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() == this.getLegacyID()
                && playerMagicData.getAdditionalCastData() instanceof EntityCastData entityCastData
                && entityCastData.getCastingEntity() instanceof AbstractConeProjectile cone) {
            cone.setDealDamageActive();
        } else {
            FireBreathProjectile fireBreathProjectile = new FireBreathProjectile(world, entity);
            fireBreathProjectile.setPos(entity.position().add(0, entity.getEyeHeight() * .7, 0));
            fireBreathProjectile.setDamage(getDamage(entity));
            world.addFreshEntity(fireBreathProjectile);

            playerMagicData.setAdditionalCastData(new EntityCastData(fireBreathProjectile));
        }
        super.onCast(world, entity, playerMagicData);
    }

    public float getDamage(LivingEntity caster) {
        return 1 + getSpellPower(caster) * .75f;
    }

    @Override
    public boolean shouldAIStopCasting(Mob mob, LivingEntity target) {
        return mob.distanceToSqr(target) > (10 * 10) * 1.2;
    }
}
