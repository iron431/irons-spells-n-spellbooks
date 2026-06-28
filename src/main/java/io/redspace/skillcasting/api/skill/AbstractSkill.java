package io.redspace.skillcasting.api.skill;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.client.SkillcastClientTickManager;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
            cachedId = Objects.requireNonNull(SkillcastingRegistries.SKILLS.getKey(this));
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
     * Sound played when {@link #onCast(Level, CastContext)} executes.
     */
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return Optional.empty();
    }

    /**
     * Contribute or override components during cast context building, after the required skeleton is in place.
     */
    public void buildContextComponents(CastContext castContext) {
        getCastStartSound(castContext).ifPresent(sound -> castContext.set(SkillcastingComponentTypes.CAST_CHANNEL_SOUND, sound));
        getOnCastSound(castContext).ifPresent(sound -> castContext.set(SkillcastingComponentTypes.ON_CAST_SOUND, sound));
        if (castContext.asEntityCaster() instanceof LivingEntity livingEntity) {
            // fixme: migrate attributes to skillcasting
            // todo: castContext#mutate?
            castContext.find(SkillcastingComponentTypes.COOLDOWN_TICKS).ifPresent(ticks -> castContext.set(SkillcastingComponentTypes.COOLDOWN_TICKS,
                    (int) (ticks * (2 - Utils.softCapFormula(livingEntity.getAttributeValue(AttributeRegistry.COOLDOWN_REDUCTION))))));
            castContext.find(SkillcastingComponentTypes.CAST_TIME).ifPresent(ticks -> castContext.set(SkillcastingComponentTypes.CAST_TIME,
                    (int) (ticks * (2 - Utils.softCapFormula(livingEntity.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION))))));
            // todo: all attributes (piercing, ricochet, etc)
        }
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

    public void onServerCastStart(CastContext castContext) {
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
    public abstract void onCast(Level level, CastContext castContext);

    /**
     * Called in tandem with {@link AbstractSkill#onCast(Level, CastContext)}, useful for compartmentalizing side effect logic, such as playing sounds, or consuming resources.
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
        // fixme: sounds are currently not synced. also, cannot get client-only sound manager here
//        if (reason == CastEndReason.INTERRUPTED && stopSoundOnCancel()) {
//            castContext.find(SkillcastingComponentTypes.ON_CAST_SOUND).ifPresent((sound) -> Minecraft.getInstance().getSoundManager().stop(sound.soundEventHolder().value().getLocation(), null));
//        }
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
        // todo: no cooldown handling
        info.leftText().add(Component.translatable("tooltip.skillcasting.cooldown_length", castContext.getOrDefault(SkillcastingComponentTypes.COOLDOWN_TICKS, 0) / 20.0 + "s"));
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
    public boolean shouldAIStopCasting(ActiveCast cast, Mob mob, LivingEntity target) {
        return false;
    }

    /**
     * @return Whether to force-stop {@link AbstractSkill#getCastStartSound(CastContext)} if the cast is interrupted. Useful to prevent spam for long or all-encompassing sounds
     */
    public boolean stopSoundOnCancel() {
        return false;
    }
}
