package io.redspace.skillcasting.demo;

import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.network.particles.HealParticlesPacket;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.EntityCasterRef;
import io.redspace.skillcasting.api.component.MultiTargetEntityCastComponent;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

public final class DemoBlessingOfLifeSkill extends AbstractSkill {
    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public int getCastTimeTicks() {
        return 30;
    }

    @Override
    public int getCooldownTicks() {
        return 200;
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        return SkillcastingUtils.preCastTargetHelper(castContext, 64, 0.35f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        MultiTargetEntityCastComponent targets = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targets == null) {
            return;
        }
        LivingEntity target = targets.getFirstLivingEntityTarget(serverLevel);
        if (target == null) {
            return;
        }
        float healAmount = getHealAmount(castContext.getSkillLevel());
        if (castContext.caster() instanceof EntityCasterRef entityCasterRef && entityCasterRef.get() instanceof LivingEntity caster) {
            NeoForge.EVENT_BUS.post(new SpellHealEvent(caster, target, healAmount, SchoolRegistry.HOLY.get()));
        }
        target.heal(healAmount);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new HealParticlesPacket(target.position()));
    }

    @Override
    public Vector3f getAccentColor() {
        return new Vector3f(0.85f, 0f, 0f);
    }

    private float getHealAmount(int skillLevel) {
        return 6 + skillLevel;
    }
}
