package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public class HealSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(30)
            .build();

    public HealSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.healing",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f), 1)));
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
        castContext.set(SkillcastingComponentTypes.HEALING, getSpellPower(castContext));
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        float healAmount = castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f);
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            entity.heal(NeoForge.EVENT_BUS.post(new SpellHealEvent(castContext.caster(), entity, healAmount, this)).getHealAmount());
            spawnHeartParticles(level, entity.position());
        }
    }

    private static void spawnHeartParticles(Level level, Vec3 center) {
        int count = 16;
        float radius = 1.25f;
        for (int i = 0; i < count; i++) {
            double theta = Math.toRadians(360.0 / count) * i;
            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;
            MagicManager.spawnParticles(level, ParticleTypes.HEART,
                    center.x + x, center.y, center.z + z, 1, 0, 0, 0, 0.1, false);
        }
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
