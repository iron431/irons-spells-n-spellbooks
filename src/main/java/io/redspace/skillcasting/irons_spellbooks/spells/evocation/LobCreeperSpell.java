package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class LobCreeperSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(2)
            .build();

    public LobCreeperSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 0.5f;
        this.castTime = 0;
        this.baseManaCost = 20;
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
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.CREEPER_HURT).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, (10 + castContext.getSkillLevel()) * 0.08f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        CreeperHeadProjectile head = new CreeperHeadProjectile(EntityRegistry.CREEPER_HEAD_PROJECTILE.get(), level);
        head.shootFromContext(head, castContext);
        head.moveTo(castContext.position().subtract(0, head.getBbHeight() * 0.5, 0), castContext.getYRot(), castContext.getXRot());
        level.addFreshEntity(head);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setIFrames(0);
    }
}
