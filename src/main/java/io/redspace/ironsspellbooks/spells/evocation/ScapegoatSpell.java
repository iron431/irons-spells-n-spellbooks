package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.scapegoat.ScapegoatEntity;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ScapegoatSpell extends AbstractSpell {

    private static final float TAUNT_RANGE = 12f;
    private static final int GOAT_DURATION_TICKS = 20 * 15;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(45)
            .build();

    public ScapegoatSpell() {
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 10;
        this.castTime = 0;
        this.baseManaCost = 30;
        this.manaCostPerLevel = 15;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.taunt_range", Utils.stringTruncation(TAUNT_RANGE, 1)),
                Component.translatable("ui.irons_spellbooks.hp",
                        Utils.stringTruncation(castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_HEALTH, 0f), 1))
        );
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
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 18f);
        castContext.set(SpellcastingComponentTypes.SUMMON_HEALTH, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, GOAT_DURATION_TICKS);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Vec3 eyePos = castContext.position(PositionAnchor.CASTING_POSITION);
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 18f);
        Vec3 targetPos = Utils.moveToRelativeGroundLevel(level,
                Utils.raycastForBlock(level, eyePos, eyePos.add(castContext.direction().scale(range)), ClipContext.Fluid.NONE).getLocation(),
                4, 24);

        ScapegoatEntity goat = new ScapegoatEntity(level);
        goat.setOwner(castContext.asEntityCaster());
        goat.setTargetPos(BlockPos.containing(targetPos));
        float yRot = -castContext.getYRot() * Mth.RAD_TO_DEG;
        goat.setYBodyRot(yRot);
        goat.yBodyRotO = yRot;
        goat.setYRot(yRot);
        goat.yRotO = yRot;
        goat.yHeadRot = yRot;
        goat.yHeadRotO = yRot;
        goat.moveTo(eyePos.add(castContext.direction().scale(2)));
        float health = castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_HEALTH, 0f);
        goat.getAttributes().getInstance(Attributes.MAX_HEALTH).setBaseValue(health);
        goat.setHealth(goat.getMaxHealth());
        goat.poofParticles(20, 1f);
        goat.setDurationRemaining(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, GOAT_DURATION_TICKS));
        goat.invulnerableTime = 60;
        level.addFreshEntity(goat);
        Utils.performTaunt(goat, TAUNT_RANGE, castContext.asEntityCaster() == null ? entity -> entity instanceof Enemy : Utils.tauntPredicate(castContext.asEntityCaster()));
    }
}
