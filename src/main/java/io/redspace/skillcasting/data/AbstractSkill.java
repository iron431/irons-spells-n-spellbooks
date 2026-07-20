package io.redspace.skillcasting.data;

import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.cast.CastResult;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.skill.SkillWheelInfo;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.component.TargetedEntitiesData;
import io.redspace.skillcasting.data.recast.RecastConfig;
import io.redspace.skillcasting.data.recast.RecastResult;
import io.redspace.skillcasting.client.ClientSkillCastHelper;
import io.redspace.skillcasting.client.render.ClientSkillTicker;
import io.redspace.skillcasting.client.render.SkillcastClientTickManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingAttributes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import io.redspace.skillcasting.data.selection.SkillSelectionManager;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractSkill {
    private ResourceLocation cachedId;
    protected String cachedDescriptionId, cachedDeathMessageId;

    public abstract CastType getCastType();

    /**
     * Registry id for this skill instance.
     */
    public final ResourceLocation getSkillId() {
        if (cachedId == null) {
            cachedId = Objects.requireNonNull(SkillcastingRegistries.SKILL_REGISTRY.getKey(this));
        }
        return cachedId;
    }

    public String getDescriptionId() {
        if (cachedDescriptionId == null) {
            cachedDescriptionId = Util.makeDescriptionId("skill", getSkillId());
        }
        return cachedDescriptionId;
    }

    public String getDeathMessageId() {
        if (cachedDeathMessageId == null) {
            cachedDeathMessageId = getSkillId().toString().replace(':', '.');
        }

        return cachedDeathMessageId;
    }

    public ResourceLocation getIconLocation() {
        return getSkillId().withPrefix("textures/gui/skill_icons/").withSuffix(".png");
    }

    public MutableComponent getDisplayName(@Nullable Player player) {
        return Component.translatable(getDescriptionId());
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
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return Optional.empty();
    }

    /**
     * Sound played when {@link #onCast(ServerLevel, CastContext)} executes.
     */
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return Optional.empty();
    }

    /**
     * Contribute or override components during cast context building, after the required skeleton is in place.
     */
    public void buildContextComponents(CastContext castContext) {
        getCastStartSound(castContext).ifPresent(sound -> castContext.set(SkillcastingComponentTypes.CAST_START_SOUND, sound));
        getOnCastSound(castContext).ifPresent(sound -> castContext.set(SkillcastingComponentTypes.ON_CAST_SOUND, sound));
        castContext.set(SkillcastingComponentTypes.CASTING_MOVESPEED_MULTIPLIER, getBaseCastingMovespeedMultiplier());
        if (castContext.asEntityCaster() instanceof LivingEntity livingEntity) {
            castContext.mutate(SkillcastingComponentTypes.COOLDOWN_TICKS, ticks -> (int) (ticks * (2 - SkillcastingUtils.softCapFormula(livingEntity.getAttributeValue(SkillcastingAttributes.COOLDOWN_REDUCTION)))));
            if (getCastType() == CastType.CONTINUOUS) {
                castContext.mutate(SkillcastingComponentTypes.CAST_TIME, ticks -> (int) (ticks * livingEntity.getAttributeValue(SkillcastingAttributes.CAST_TIME_REDUCTION)));
            } else {
                castContext.mutate(SkillcastingComponentTypes.CAST_TIME, ticks -> (int) (ticks * (2 - SkillcastingUtils.softCapFormula(livingEntity.getAttributeValue(SkillcastingAttributes.CAST_TIME_REDUCTION)))));
            }
            castContext.mutate(SkillcastingComponentTypes.CASTING_MOVESPEED_MULTIPLIER, multiplier -> multiplier + (float) livingEntity.getAttributeValue(SkillcastingAttributes.CASTING_MOVESPEED) - 1);
        }
    }

    public float getBaseCastingMovespeedMultiplier() {
        return 1f;
    }

    /**
     * Checks a caster's logical capability to cast a skill, such as cooldowns, resources, or other metrics.
     * <br> By default, only checks cooldown.
     *
     * @return {@link CastResult} permitting or preventing the cast
     */
    public CastResult canBeCastBy(CastContext castContext) {
        if (!castContext.has(SkillcastingComponentTypes.IGNORE_COOLDOWN) && castContext.getSkillcastingData().cooldowns().isOnCooldown(this)) {
            return CastResult.failure(Component.translatable("ui.skillcasting.cast_error_cooldown", Component.translatable(this.getDescriptionId())).withStyle(ChatFormatting.RED));
        }
        return CastResult.success();
    }

    /**
     * Checks a skill's physical ability to be cast, such as if a target is required but cannot be found.
     * <br> By default, nothing is checked.
     *
     * @return whether the cast is able to proceed.
     */
    public boolean checkPreCastConditions(CastContext castContext) {
        return true;
    }

    public void onServerCastStart(CastContext castContext) {
        Vec3 origin = castContext.position(PositionAnchor.ORIGIN);
        // fixme: what to use for sound source? expose on caster reference?
        castContext.find(SkillcastingComponentTypes.CAST_START_SOUND)
                .ifPresent(sound -> castContext.level().playSound(null, origin.x, origin.y, origin.z, sound.soundEventHolder(), SoundSource.PLAYERS, sound.volume(), sound.samplePitch(castContext.level().getRandom())));

    }

    public void onServerCastTick(CastContext castContext) {
    }

    /**
     * Entrypoint into skill casting functionality. Put skill logic here. Called once immediately for {@link CastType#INSTANT} casts, once at the end of a channel for {@link CastType#LONG} casts, and once every {@link AbstractSkill#continuousInterval()} ticks for {@link CastType#CONTINUOUS} casts.
     */
    public abstract void onCast(ServerLevel level, CastContext castContext);

    /**
     * Called in tandem with {@link AbstractSkill#onCast(ServerLevel, CastContext)}, useful for compartmentalizing side effect logic, such as playing sounds, or consuming resources.
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
        if (reason == CastEndReason.INTERRUPTED && stopSoundOnCancel()) {
            castContext.find(SkillcastingComponentTypes.CAST_START_SOUND).ifPresent(ClientSkillCastHelper::stopSound);
        }
    }

    /**
     * Called on the client when a channeled cast ({@link CastType#LONG}, {@link CastType#CONTINUOUS}) begins. CastContext only has synced parameters.
     */
    public void onClientCastStart(CastContext castContext) {
        createClientTicker().ifPresent(ticker -> SkillcastClientTickManager.track(castContext.caster(), ticker));
    }

    public Optional<RecastConfig> provideRecastConfig(CastContext castContext) {
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
        levelComponent = Component.translatable("tooltip.skillcasting.level", levelComponent);
        info.leftText().add(levelComponent);
        int cooldownTicks = castContext.getOrDefault(SkillcastingComponentTypes.COOLDOWN_TICKS, 0);
        if (cooldownTicks > 0) {
            info.leftText().add(Component.translatable("tooltip.skillcasting.cooldown_length",
                    String.format("%.2fs", cooldownTicks / 20.0)));
        }
        return info;
    }

    /**
     * Accent color used for rendering various builtin effects, like target color outline, or recast overlay tinting.
     *
     * @return (R,G,B) color [0-1]
     */
    public Vector3f getAccentColor() {
        return new Vector3f(1, 1, 1);
    }

    /**
     * Mob-oriented helper where skills can provide hooks for when to terminate a skillcast based on certain context, such as if a skill has a max range which the target has exceeded.
     */
    public boolean shouldAIStopCasting(CastContext castContext, Mob mob, LivingEntity target) {
        return false;
    }

    /**
     * @return Whether to force-stop {@link AbstractSkill#getCastStartSound(CastContext)} if the cast is interrupted. Useful to prevent spam for long or all-encompassing sounds
     */
    public boolean stopSoundOnCancel() {
        return false;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinLevel() {
        return 1;
    }

    public Holder<AbstractSkill> holder() {
        return SkillRegistry.holder(this);
    }

    /**
     * Provides a simple hook before a mob initiates a cast in order to tailor its behavior.
     */
    public void setupAIContext(CastContext castContext, Mob mob) {
        if (mob.getTarget() != null && !castContext.has(SkillcastingComponentTypes.TARGETED_ENTITIES)) {
            castContext.set(SkillcastingComponentTypes.TARGETED_ENTITIES, new TargetedEntitiesData(mob.getTarget()));
        }
    }
}
