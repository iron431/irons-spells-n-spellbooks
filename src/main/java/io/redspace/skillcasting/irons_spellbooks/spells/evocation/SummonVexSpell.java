package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedVex;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Optional;

public class SummonVexSpell extends AbstractSpellSkill {

    private static final int SUMMON_DURATION_TICKS = 20 * 60 * 10;

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_COUNT, 0))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(150)
            .build();

    public SummonVexSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 20;
        this.baseManaCost = 50;
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
        return PlayableSound.standard(SoundEvents.EVOKER_PREPARE_SUMMON).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.EVOKER_CAST_SPELL).toOpt();
    }

    @Override
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(2, SUMMON_DURATION_TICKS));
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SpellcastingComponentTypes.SUMMON_COUNT, castContext.getSkillLevel() + 2);
        castContext.set(SpellcastingComponentTypes.SUMMON_HEALTH, 14f);
        castContext.set(SpellcastingComponentTypes.SUMMON_ATTACK_DAMAGE, 4f);
    }

    @Override
    public void onRecastFinished(CastContext castContext, RecastResult result) {
        SummonManager.recastFinishedHelper(castContext, result);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (castContext.getSkillcastingData().recasts().hasRecast(this)) {
            return;
        }

        Entity caster = castContext.asEntityCaster();
        SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
        int count = castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_COUNT, 0);
        Vec3 spawnBase = castContext.position(PositionAnchor.CASTING_POSITION);

        for (int i = 0; i < count; i++) {
            SummonedVex vex = new SummonedVex(EntityRegistry.SUMMONED_VEX.get(), level);
            vex.moveTo(spawnBase.add(new Vec3(Utils.getRandomScaled(2), 1, Utils.getRandomScaled(2))));
            vex.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(vex.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            vex.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_ATTACK_DAMAGE, 15f));
            vex.getAttribute(Attributes.MAX_HEALTH).setBaseValue(castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_HEALTH, 15f));
            vex.setHealth(vex.getMaxHealth());
            Entity creature = vex;
            if (caster instanceof LivingEntity living) {
                creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(living, vex, getSkillId(), castContext.getSkillLevel())).getCreature();
            }
            level.addFreshEntity(creature);
            SummonManager.initSummon(caster, creature, SUMMON_DURATION_TICKS, summonedEntitiesCastData);
        }
        castContext.set(SpellcastingComponentTypes.SUMMONED_ENTITY_DATA, summonedEntitiesCastData);
    }
}
