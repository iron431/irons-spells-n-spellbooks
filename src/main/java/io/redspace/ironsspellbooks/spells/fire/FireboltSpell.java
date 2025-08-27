package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpellSkill;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellSkillDamageSource;
import io.redspace.ironsspellbooks.entity.spells.firebolt.FireboltProjectile;
import io.redspace.skillcastingapi.data.ICastContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;

@AutoSpellConfig
public class FireboltSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(ICastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(castContext), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(1)
            .build();

    public FireboltSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 0.5f;
        this.castTime = 0;
        this.baseManaCost = 10;
    }

    @Override
    public io.redspace.skillcastingapi.core.CastType getCastType() {
        return io.redspace.skillcastingapi.core.CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public void onCast(ICastContext castContext) {
        var world = castContext.getLevel();
        var owner = castContext.getEntity() instanceof LivingEntity livingEntity ? livingEntity : null;
        FireboltProjectile firebolt = new FireboltProjectile(world, owner);
        firebolt.setPos(castContext.getPosition().add(0, -firebolt.getBoundingBox().getYsize() * .5f, 0));
        firebolt.shoot(castContext.getForward());
        firebolt.setDamage(getDamage(castContext));
        world.addFreshEntity(firebolt);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFireTicks(60);
    }

    private float getDamage(ICastContext castContext) {
        return getSpellPower(castContext);
    }


}
