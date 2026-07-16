package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.lightning_lance.LightningLanceProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class LightningLanceSpell extends AbstractSpell {

    private static final float PROJECTILE_BASE_SPEED = 3f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public LightningLanceSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 14;
        this.spellPowerPerLevel = 2;
        this.castTime = 40;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)));
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.LIGHTNING_LANCE_CAST).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.LIGHTNING_WOOSH_01).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, PROJECTILE_BASE_SPEED);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        LightningLanceProjectile lance = new LightningLanceProjectile(level, castContext.asEntityCaster());
        Vec3 origin = castContext.position(PositionAnchor.CASTING_POSITION);
        lance.setPos(origin.add(0, lance.getBoundingBox().getYsize() * 0.25f, 0).add(castContext.direction()));
        lance.shootFromContext(lance, castContext);
        level.addFreshEntity(lance);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CHARGED_CAST;
    }
}
