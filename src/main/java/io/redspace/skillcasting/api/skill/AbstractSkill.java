package io.redspace.skillcasting.api.skill;

import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.client.SkillcastClientTickManager;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

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
     * Sound played when a channeled cast ({@link CastType#LONG} or {@link CastType#CONTINUOUS}) begins.
     */
    public Optional<PlayableSound> getCastChannelSound(CastContext castContext) {
        return Optional.empty();
    }

    /**
     * Sound played when {@link #onCast(CastContext)} executes.
     */
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return Optional.empty();
    }

    /**
     * Contribute or override components during cast context building, after the required skeleton is in place.
     */
    public void buildContextComponents(CastContext castContext) {
        getCastChannelSound(castContext).ifPresent(sound -> castContext.set(SkillcastingComponentTypes.CAST_CHANNEL_SOUND, sound));
        getOnCastSound(castContext).ifPresent(sound -> castContext.set(SkillcastingComponentTypes.ON_CAST_SOUND, sound));
    }

    public CastResult canBeCastBy(CastContext castContext) {
        if (!castContext.has(SkillcastingComponentTypes.IGNORE_COOLDOWN) && castContext.getSkillcastingData().cooldowns().isOnCooldown(this)) {
            return CastResult.failure(Component.translatable("ui.skillcasting.cast_error_cooldown", Component.translatable(this.getDescriptionId())).withStyle(ChatFormatting.RED));
        }
        return CastResult.isSuccess();
    }

    public boolean checkPreCastConditions(CastContext castContext) {
        return true;
    }

    public void onServerPreCast(CastContext castContext) {
        Vec3 origin = castContext.position(PositionAnchor.ORIGIN);
        // fixme: what to use for sound source? expose on caster reference?
        castContext.find(SkillcastingComponentTypes.CAST_CHANNEL_SOUND)
                .ifPresent(sound -> castContext.level().playSound(null, origin.x, origin.y, origin.z, sound.soundEventHolder(), SoundSource.PLAYERS, sound.volume(), sound.samplePitch(castContext.level().getRandom())));

    }

    public void onServerCastTick(CastContext castContext) {
    }

    /**
     * Entrypoint into skill casting functionality. Put skill logic here. Called once immediately for {@link CastType#INSTANT} casts, once at the end of a channel for {@link CastType#LONG} casts, and once every {@link AbstractSkill#continuousInterval()} ticks for {@link CastType#CONTINUOUS} casts.
     */
    public abstract void onCast(CastContext castContext);

    /**
     * Called in tandem with {@link AbstractSkill#onCast(CastContext)}, useful for compartmentalizing side effect logic, such as playing sounds, or consuming resources.
     * <br>
     * By default, it plays the {@link SkillcastingComponentTypes#ON_CAST_SOUND}
     */
    public void onPostCast(CastContext castContext) {
        Vec3 origin = castContext.position(PositionAnchor.ORIGIN);
        // fixme: what to use for sound source? expose on caster reference?
        castContext.find(SkillcastingComponentTypes.ON_CAST_SOUND)
                .ifPresent(sound -> castContext.level().playSound(null, origin.x, origin.y, origin.z, sound.soundEventHolder(), SoundSource.PLAYERS, sound.volume(), sound.samplePitch(castContext.level().getRandom())));
    }

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
     * @return (R,G,B) color [0-1]
     */
    public Vector3f getAccentColor() {
        return new Vector3f(1, 1, 1);
    }
}
