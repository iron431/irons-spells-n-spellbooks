package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpellSkill;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcastingapi.core.CastType;
import io.redspace.skillcastingapi.data.ICastContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class FireballSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(ICastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(castContext), 2)),
                Component.translatable("ui.irons_spellbooks.radius", getRadius(castContext))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(25)
            .build();

    public FireballSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 40;
        this.baseManaCost = 60;
    }

    @Override
    public io.redspace.skillcastingapi.core.CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.FIREBALL_START.get());
    }

    @Override
    public void onCast(ICastContext castContext) {
        Vec3 origin = castContext.getPosition();

        MagicFireball fireball = new MagicFireball(castContext.getLevel());
        fireball.setOwner(castContext.getEntity());

        fireball.setDamage(getDamage(castContext));
        fireball.setExplosionRadius(getRadius(castContext));

        var direction = castContext.getForward();
        fireball.setPos(origin.add(direction).subtract(0, fireball.getBbHeight() / 2, 0));
        fireball.shoot(direction);

        castContext.getLevel().addFreshEntity(fireball);
    }

    public float getDamage(ICastContext castContext) {
        return 5 + 5 * getSpellPower(castContext);
    }

    public int getRadius(ICastContext castContext) {
        return 2 + (int) getSpellPower(castContext);
    }
}
