package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ChainCreeperSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_COUNT, 0))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(15)
            .build();

    public ChainCreeperSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 0;
        this.castTime = 30;
        this.baseManaCost = 40;
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
        return PlayableSound.standard(SoundEvents.CREEPER_PRIMED).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.EVOKER_CAST_SPELL).toOpt();
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, 0.25f, false);
        return true;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int level = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 48f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_COUNT, 3 + level - 1);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Vec3 spawn = SkillcastingUtils.getTargetedEntityPosition(level, castContext)
                .orElse(RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                        .checkForBlocks(true)
                        .bbInflation(0.35f)
                        .build()
                        .getLocation());

        int count = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_COUNT, 0);
        summonCreeperRing(level, spawn.add(0, 0.5, 0), castContext, count);
    }

    public static void summonCreeperRing(Level level, Vec3 origin, CastContext castContext, int count) {
        if (count < 3) {
            count = 3;
        }
        int degreesPerCreeper = 360 / count;
        for (int i = 0; i < count; i++) {
            Vec3 motion = new Vec3(0, 0, .4 + count * .015f);
            motion = motion.xRot(75 * Mth.DEG_TO_RAD);
            motion = motion.yRot(degreesPerCreeper * i * Mth.DEG_TO_RAD);

            CreeperHeadProjectile head = new CreeperHeadProjectile(level, castContext.asEntityCaster());
            head.applyContext(castContext);
            head.setChainOnKill(true);
            head.setChainCount(count - 2);
            head.setDeltaMovement(motion);
            Vec3 spawn = origin.add(motion.multiply(1, 0, 1).normalize().scale(.6f));
            var angle = Utils.rotationFromDirection(motion);

            head.moveTo(spawn.x, spawn.y - head.getBoundingBox().getYsize() / 2, spawn.z, angle.y, angle.x);
            level.addFreshEntity(head);
        }
    }
}
