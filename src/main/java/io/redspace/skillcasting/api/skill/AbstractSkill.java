package io.redspace.skillcasting.api.skill;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Registry object describing a castable skill. Identity comes from the registry holder (no stored
 * id), mirroring {@code AbstractSpell}. Subclasses override lifecycle hooks; the
 * {@code SkillcastingManager} drives them through the cast state machine.
 */
public abstract class AbstractSkill {
    private ResourceLocation cachedId;

    public abstract CastType getCastType();

    /**
     * Registry id for this skill instance.
     */
    public final ResourceLocation getSkillId() {
        if (cachedId == null) {
            cachedId = Objects.requireNonNull(SkillcastingRegistries.SKILLS.getKey(this));
        }
        return cachedId;
    }

    public final String getDescriptionId() {
        return Util.makeDescriptionId("skill", getSkillId());
    }

    /**
     * Default icon path; host mods may override.
     */
    public ResourceLocation getIconLocation() {
        return getSkillId().withPrefix("textures/gui/skill_icons/").withSuffix(".png");
    }

    /**
     * Default cast channel time in ticks for {@link CastType#LONG} and {@link CastType#CONTINUOUS}. Ignored for {@link CastType#INSTANT}
     */
    public int getCastTimeTicks() {
        return 0;
    }

    /**
     * Tick interval between {@link #onCast} executions for {@link CastType#CONTINUOUS}.
     */
    public int continuousInterval() {
        return 10;
    }

    /**
     * Base cooldown in ticks applied on successful completion (before {@code BuildCooldownEvent}).
     */
    public int getCooldownTicks() {
        return 0;
    }

    /**
     * Contribute or override components during INIT, after the required skeleton is in place.
     */
    public void buildContextComponents(CastContext castContext) {
    }

    // ---- validation ----------------------------------------------------------------------------

    public CastResult canBeCastBy(CastContext castContext) {
        long gameTime = castContext.level().getGameTime();
        if (castContext.getSkillcastingData().cooldowns().isOnCooldown(this, gameTime)) {
            // todo: lang
            return CastResult.failure(Component.literal("{} on cooldown").withStyle(ChatFormatting.RED));
        }
        return CastResult.isSuccess();
    }

    public boolean checkPreCastConditions(CastContext castContext) {
        return true;
    }

    // ---- server lifecycle ----------------------------------------------------------------------

    public void onServerPreCast(CastContext castContext) {
    }

    public void onServerCastTick(CastContext castContext) {
    }

    /**
     * The payload of the skill, invoked on EXECUTE.
     */
    public abstract void onCast(CastContext castContext);

    public void onServerCastComplete(CastContext castContext, CastEndReason reason) {
    }

    // ---- client --------------------------------------------------------------------------------

    public void onClientCast(CastContext castContext) {
    }

    // ---- AI ------------------------------------------------------------------------------------

    public boolean shouldAIStopCasting(CastContext castContext, @Nullable LivingEntity target) {
        return false;
    }

    // ---- recast --------------------------------------------------------------------------------

    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.empty();
    }

    public void onRecastFinished(CastContext castContext, RecastResult result) {
    }
}
