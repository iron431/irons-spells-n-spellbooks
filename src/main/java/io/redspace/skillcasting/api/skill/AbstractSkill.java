package io.redspace.skillcasting.api.skill;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.client.SkillcastClientTickManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

/**
 * Registry object describing a castable skill. Identity comes from the registry holder (no stored
 * id), mirroring {@code AbstractSpell}. Subclasses override lifecycle hooks; the
 * {@code SkillcastingManager} drives them through the cast state machine.
 */
public abstract class AbstractSkill {
    private ResourceLocation cachedId;
    private String cachedDescriptionId;

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
        if (cachedDescriptionId == null) {
            cachedDescriptionId = Util.makeDescriptionId("skill", getSkillId());
        }
        return cachedDescriptionId;
    }

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

    public int getCooldownTicks() {
        return 0;
    }

    /**
     * Contribute or override components during cast context building, after the required skeleton is in place.
     */
    public void buildContextComponents(CastContext castContext) {
    }

    public CastResult canBeCastBy(CastContext castContext) {
        if (castContext.getSkillcastingData().cooldowns().isOnCooldown(this)) {
            return CastResult.failure(Component.translatable("ui.skillcasting.cast_error_cooldown", Component.translatable(this.getDescriptionId())).withStyle(ChatFormatting.RED));
        }
        return CastResult.isSuccess();
    }

    public boolean checkPreCastConditions(CastContext castContext) {
        return true;
    }

    public void onServerPreCast(CastContext castContext) {
    }

    public void onServerCastTick(CastContext castContext) {
    }

    public abstract void onCast(CastContext castContext);

    public void onServerCastComplete(CastContext castContext, CastEndReason reason) {
    }

    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.empty();
    }

    /**
     * Called on the client when any cast is finished. CastContext only has synced parameters.
     */
    public void onClientCastComplete(CastContext castContext, CastEndReason reason) {

    }

    /**
     * Called on the client when a channeled cast ({@link CastType#LONG}, {@link CastType#CONTINUOUS}) begins. CastContext only has synced parameters.
     */
    public void onClientCastStart(CastContext castContext) {
        createClientTicker().ifPresent(ticker -> SkillcastClientTickManager.track(castContext.caster(), ticker));
    }

    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.empty();
    }

    public void onRecastFinished(CastContext castContext, RecastResult result) {
    }

    public SkillWheelInfo buildSpellWheelInfo(CastContext castContext, SkillSelectionManager.SelectionOption selectionOption) {
        SkillWheelInfo info = new SkillWheelInfo();
        Component levelComponent;
        int levelTotal = castContext.getSkillLevel();
        int diff = levelTotal - selectionOption.getLevel();
        if (diff > 0) {
            levelComponent = Component.translatable("tooltip.skillcasting.level_plus", levelTotal, diff);
        } else if (diff < 0) {
            levelComponent = Component.translatable("tooltip.skillcasting.level_minus", levelTotal, diff);
        } else {
            levelComponent = Component.literal(String.valueOf(levelTotal));
        }
        info.leftText().add(levelComponent);
        info.leftText().add(Component.translatable("tooltip.skillcasting.cooldown_length", castContext.get(SkillcastingComponentTypes.COOLDOWN_TICKS) / 20.0 + "s"));
        return info;
    }

    /**
     * Accent color used for rendering various builtin effects, like target color outline, or recast overlay tinting.
     * @return R,G,B color 0-1f
     */
    public Vector3f getAccentColor() {
        return new Vector3f(1, 1, 1);
    }
}
