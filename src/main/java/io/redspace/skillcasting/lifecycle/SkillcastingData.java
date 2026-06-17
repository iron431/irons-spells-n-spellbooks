package io.redspace.skillcasting.lifecycle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.api.recast.RecastManager;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.cooldown.CooldownInstance;
import io.redspace.skillcasting.cooldown.CooldownManager;
import io.redspace.skillcasting.registry.SkillcastingAttachments;
import io.redspace.skillcasting.selection.SkillSelection;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class SkillcastingData {
    public static final Codec<SkillcastingData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            CooldownManager.CODEC.fieldOf("cooldowns").forGetter(SkillcastingData::cooldowns),
            RecastManager.CODEC.fieldOf("recasts").forGetter(SkillcastingData::recasts),
            SkillSelection.CODEC.fieldOf("selection").forGetter(SkillcastingData::selection)
    ).apply(builder, (cooldowns, recasts, selection) -> {
        SkillcastingData data = new SkillcastingData();
        data.cooldowns.replaceFrom(cooldowns);
        data.recasts.replaceFrom(recasts);
        data.selection.copyFrom(selection);
        return data;
    }));

    public static SkillcastingData get(IAttachmentHolder holder) {
        return holder.getData(SkillcastingAttachments.SKILLCASTING_DATA.get());
    }

    public static boolean has(IAttachmentHolder holder) {
        return holder.hasData(SkillcastingAttachments.SKILLCASTING_DATA.get());
    }

    private final CooldownManager cooldowns = new CooldownManager();
    private final RecastManager recasts = new RecastManager();
    private final SkillSelection selection = new SkillSelection();
    private final SkillSelectionManager selectionManager = new SkillSelectionManager(selection);
    @Nullable
    private ActiveCast activeCast;

    public boolean isCasting() {
        return activeCast != null;
    }

    public void tick(CasterRef caster) {
        cooldowns.tick();
        recasts.tick(caster);
    }

    public CooldownManager cooldowns() {
        return cooldowns;
    }

    public RecastManager recasts() {
        return recasts;
    }

    public SkillSelection selection() {
        return selection;
    }

    public SkillSelectionManager selectionManager() {
        return selectionManager;
    }

    public void applySyncedCooldowns(Map<Holder<AbstractSkill>, CooldownInstance> synced) {
        cooldowns.replaceFrom(synced);
    }

    public void applySyncedRecasts(Map<Holder<AbstractSkill>, RecastInstance> syncedRecasts) {
        recasts.replaceFrom(syncedRecasts);
    }

    public void applySyncedCooldown(Holder<AbstractSkill> skill, @Nullable CooldownInstance instance) {
        cooldowns.applySynced(skill, instance);
    }

    public void applySyncedRecast(Holder<AbstractSkill> skill, @Nullable RecastInstance instance) {
        recasts.applySynced(skill, instance);
    }

    @Nullable
    public AbstractSkill getActiveSkill() {
        return activeCast != null ? activeCast.context().skill().value() : null;
    }

    @Nullable
    public ActiveCast getActiveCast() {
        return activeCast;
    }

    public void activateCast(ActiveCast activeCast) {
        this.activeCast = activeCast;
    }

    public void endActiveCast() {
        this.activeCast = null;
    }

    @Nullable
    public CastType getActiveCastType() {
        AbstractSkill skill = getActiveSkill();
        return skill == null ? null : skill.getCastType();
    }

    public int castDuration() {
        return activeCast == null ? 0 : activeCast.durationTicks();
    }

    public int castDurationRemaining(long gameTime) {
        return activeCast == null ? 0 : activeCast.remainingTicks(gameTime);
    }

    public float castCompletionPercent(long gameTime) {
        if (activeCast == null) {
            return 0;
        }
        float percent = activeCast.completionPercent(gameTime);
        if (getActiveCastType() == CastType.CONTINUOUS) {
            return 1 - percent;
        }
        return percent;
    }

    /**
     * @return <code>true</code> if there is live state beyond an "empty" skillcasting data.
     */
    // todo: rename this. not a great name
    public boolean isLive() {
        return !cooldowns.isEmpty() || !recasts.isEmpty() || !selection.isEmpty();
    }
}
