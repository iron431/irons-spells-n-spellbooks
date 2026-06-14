package io.redspace.skillcasting.lifecycle;

import io.redspace.skillcasting.SkillcastingTime;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.event.BuildCooldownEvent;
import io.redspace.skillcasting.api.event.BuildSkillLevelEvent;
import io.redspace.skillcasting.api.event.SkillCastCompleteEvent;
import io.redspace.skillcasting.api.event.SkillPreCastEvent;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.api.recast.RecastManager;
import io.redspace.skillcasting.api.resolver.DirectionResolver;
import io.redspace.skillcasting.api.resolver.PositionResolver;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastResult;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.cooldown.CooldownInstance;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.selection.SkillSelection;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillcastingManager {
    /**
     * Live refs for every caster the manager must tick: active cast, cooldowns, or recasts
     * Fixme: that means it must be repopulated from disk for cooldowns and recasts, unless those become player-only again.
     *  alternatively, it may not be player-only, but simply self-ticked (players self tick, BE's must implement their own cooldown ticking, etc).
     *  I really don't like this.
     */
    private static final Map<CasterId, CasterRef> TRACKED = new ConcurrentHashMap<>();

    private SkillcastingManager() {
    }

    // ---- generic core --------------------------------------------------------------------------

    public static boolean attemptInitiateFromSelection(CasterRef caster) {
        SkillcastingData data = caster.skillcastingData();
        SkillSelectionManager.SelectionOption selected = data.selectionManager().getSelection();
        if (selected == null) {
            return false;
        }
        return initiateCast(caster, SkillRegistry.holder(selected.getSkill()), selected.getLevel(), selected.equipmentSlot);
    }

    public static boolean attemptInitiateFromQuickCastSlot(CasterRef caster, int globalIndex) {
        SkillcastingData data = caster.skillcastingData();
        SkillSelectionManager.SelectionOption option = data.selectionManager().getOptionAt(globalIndex);
        if (option == null) {
            return false;
        }
        return initiateCast(caster, SkillRegistry.holder(option.skillData.getSkill()), option.skillData.getLevel(), option.equipmentSlot);
    }

    public static boolean initiateCast(CasterRef caster, Holder<AbstractSkill> skillHolder, int baseLevel, @Nullable String equipmentSlot) {
        if (caster.level().isClientSide() || !caster.isValid()) {
            return false;
        }
        SkillcastingData skillcastingData = caster.skillcastingData();
        AbstractSkill skill = skillHolder.value();

        ActiveCast existingCast = skillcastingData.getActiveCast();
        if (existingCast != null) {
            endCast(caster, skillcastingData, existingCast, CastEndReason.REPLACED);
            if (existingCast.context().skill().equals(skillHolder)) {
                return false;
            }
        }
        CastContext context = new CastContext(skillHolder, caster, caster.level());
        RecastManager recastManager = skillcastingData.recasts();
        if (recastManager.hasRecast(skill)) {
            context.components().applyFrom(recastManager.get(skill.getSkillId()).components());
        } else {
            skill.getRecastConfig(context).ifPresent(recast -> context.set(SkillcastingComponentTypes.RECAST_CONFIG, recast));
        }
        context.set(SkillcastingComponentTypes.POSITION_RESOLVER, PositionResolver.Caster.INSTANCE);
        context.set(SkillcastingComponentTypes.DIRECTION_RESOLVER, DirectionResolver.Caster.INSTANCE);
        context.set(SkillcastingComponentTypes.CAST_TIME, skill.getCastTimeTicks());
        context.set(SkillcastingComponentTypes.COOLDOWN_TICKS, skill.getCooldownTicks());
        if (equipmentSlot != null) {
            context.set(SkillcastingComponentTypes.CAST_SOURCE, equipmentSlot);
        }

        BuildSkillLevelEvent levelEvent = new BuildSkillLevelEvent(context, baseLevel);
        NeoForge.EVENT_BUS.post(levelEvent);
        context.set(SkillcastingComponentTypes.SKILL_LEVEL, levelEvent.getLevel());
        //todo: create additional event post afterwards for 4th party interactions? (ie addon changing mana cost)
        skill.buildContextComponents(context);

        CastResult result = skill.canBeCastBy(context);
        if (caster.get() instanceof ServerPlayer serverPlayer && result.message() != null) {
            serverPlayer.displayClientMessage(result.message(), true);
        }
        if (result.isFailure()) {
            return false;
        }
        if (!skill.checkPreCastConditions(context)) {
            return false;
        }
        SkillPreCastEvent preCast = new SkillPreCastEvent(context);
        NeoForge.EVENT_BUS.post(preCast);
        if (preCast.isCanceled()) {
            return false;
        }


        skill.onServerPreCast(context);

        if (skill.getCastType() == CastType.INSTANT) {
            onCast(context);
            onCastComplete(caster, skillcastingData, context, CastEndReason.COMPLETED);
            return true;
        }

        skillcastingData.activateCast(new ActiveCast(context, context.level().getGameTime()));
        context.components().markAllSyncedDirty();
        track(caster);
        SkillcastingNetwork.syncCastStart(caster, skillcastingData.getActiveCast());
        return true;
    }

    public static void select(ServerPlayer player, SkillSelection selection) {
        SkillcastingData.get(player).selectionManager().applySelection(selection);
    }

    public static void cancelCast(CasterRef caster, CastEndReason reason) {
        cancelCast(caster, reason, reason == CastEndReason.COMPLETED || caster.skillcastingData().getActiveCastType() == CastType.CONTINUOUS);
    }

    public static void cancelCast(CasterRef caster, CastEndReason reason, boolean triggerCooldown) {
        if (caster.level().isClientSide()) {
            return;
        }
        ActiveCast active = caster.skillcastingData().getActiveCast();
        if (active == null) {
            return;
        }
        CastContext context = active.context();
        AbstractSkill skill = context.skill().value();
        int cooldownTicks = context.get(SkillcastingComponentTypes.COOLDOWN_TICKS);
        endCast(caster, caster.skillcastingData(), active, reason);
        if (triggerCooldown && cooldownTicks > 0) {
            triggerCooldown(context, skill, cooldownTicks);
        }
    }

    public static void serverTick() {
        for (Map.Entry<CasterId, CasterRef> entry : TRACKED.entrySet()) {
            CasterId id = entry.getKey();
            CasterRef caster = entry.getValue();

            if (!caster.isValid()) {
                forceDrop(id, caster);
                continue;
            }

            SkillcastingData skillcastingData = caster.skillcastingData();
            long gameTime = SkillcastingTime.gameTime(caster.level());
            boolean recastsChanged = skillcastingData.recasts().pruneExpired(caster, gameTime);
            boolean cooldownsChanged = skillcastingData.cooldowns().pruneExpired(gameTime);
            if (recastsChanged) {
                // todo: individual syncs would be more efficient
                SkillcastingNetwork.syncAllRecasts(caster, skillcastingData);
            }
            if (cooldownsChanged || recastsChanged) {
                // todo: individual syncs would be more efficient
                SkillcastingNetwork.syncAllCooldowns(caster, skillcastingData);
            }

            ActiveCast active = skillcastingData.getActiveCast();
            if (active != null) {
                tickActive(caster, skillcastingData, active, gameTime);
            }
            if (!skillcastingData.hasLiveTimers(gameTime)) {
                TRACKED.remove(id);
            }
        }
    }

    public static void serverStopped() {
        TRACKED.clear();
    }

    private static void tickActive(CasterRef caster, SkillcastingData data, ActiveCast active, long gameTime) {
        CastContext castContext = active.context();
        AbstractSkill skill = castContext.skill().value();
        SkillcastingNetwork.syncDirtyCastComponents(caster, castContext);

        skill.onServerCastTick(castContext);
        int elapsed = active.elapsedTicks(gameTime);
        if (skill.getCastType() == CastType.CONTINUOUS) {
            int interval = Math.max(1, skill.continuousInterval());
            if (elapsed % interval == 1) {
                onCast(castContext);
            }
        }
        if (elapsed >= castContext.get(SkillcastingComponentTypes.CAST_TIME)) {
            if (skill.getCastType() == CastType.LONG) {
                onCast(castContext);
            }
            endCast(caster, data, active, CastEndReason.COMPLETED);
        }
    }

    // ---- execution / completion ----------------------------------------------------------------

    private static void onCast(CastContext castContext) {
        castContext.skill().value().onCast(castContext);
    }

    private static void endCast(CasterRef caster, SkillcastingData data, ActiveCast active, CastEndReason reason) {
        data.endActiveCast();
        onCastComplete(caster, data, active.context(), reason);
    }

    private static void onCastComplete(CasterRef caster, SkillcastingData data, CastContext castContext, CastEndReason reason) {
        AbstractSkill skill = castContext.skill().value();
        ResourceLocation skillId = skill.getSkillId();
        // on cast complete
        skill.onServerCastComplete(castContext, reason);
        NeoForge.EVENT_BUS.post(new SkillCastCompleteEvent(castContext, reason));
        // handle recasting
        boolean isOnRecast = false;
        // fixme: cast end reason is conflating "totally completed" vs "completed to fruition", which is different for long/continuous casts
        //  this is a sign of a deeper issue, but for now we ball
        boolean completedToFruition = reason.isCompletion() || skill.getCastType() == CastType.CONTINUOUS;
        if (completedToFruition) {
            RecastManager recasts = castContext.getSkillcastingData().recasts();
            if (recasts.hasRecast(castContext.skill())) {
                isOnRecast = recasts.handleRecastConsumption(castContext);
                // todo: individual syncs would be more efficient
                SkillcastingNetwork.syncAllRecasts(caster, caster.skillcastingData());
            } else {
                RecastConfig recastConfig = castContext.get(SkillcastingComponentTypes.RECAST_CONFIG);
                if (recastConfig != null) {
                    data.recasts().addRecast(castContext.skill(), new RecastInstance(recastConfig, castContext));
                    track(caster);
                    isOnRecast = true;
                    // todo: individual syncs would be more efficient
                    SkillcastingNetwork.syncAllRecasts(caster, caster.skillcastingData());
                }
            }
        }
        // handle cooldown
        // todo: ignore cooldown flags? or we we expect something to set the cooldown to zero by now. prob flag.
        int cooldownDuration = castContext.get(SkillcastingComponentTypes.COOLDOWN_TICKS);
        if (cooldownDuration > 0 && completedToFruition && !isOnRecast) {
            triggerCooldown(castContext, skill, cooldownDuration);
        }
        // sync
        SkillcastingNetwork.syncCastEnd(caster);
    }

    public static void triggerCooldown(CastContext castContext, AbstractSkill skill, int cooldownTicks) {
        BuildCooldownEvent cooldownEvent = new BuildCooldownEvent(castContext, cooldownTicks);
        NeoForge.EVENT_BUS.post(cooldownEvent);
        if (cooldownEvent.getTicks() > 0) {
            castContext.getSkillcastingData().cooldowns().addCooldown(skill, CooldownInstance.startingNow(cooldownEvent.getTicks(), castContext.level().getGameTime()));
            track(castContext.caster());
            // todo: individual syncs would be more efficient
            SkillcastingNetwork.syncAllCooldowns(castContext.caster(), castContext.getSkillcastingData());
        }
    }

    /**
     * Caster became invalid (unloaded/removed): drop in-flight state without world side effects.
     */
    @Deprecated
    private static void forceDrop(CasterId id, CasterRef caster) {
        ActiveCast active = caster.skillcastingData().getActiveCast();
        if (active != null) {
            CastContext castContext = active.context();
            castContext.skill().value().onServerCastComplete(castContext, CastEndReason.SYSTEM);
            NeoForge.EVENT_BUS.post(new SkillCastCompleteEvent(castContext, CastEndReason.SYSTEM));
        }
        TRACKED.remove(id);
    }

    // ---- helpers -------------------------------------------------------------------------------

    private static void track(CasterRef caster) {
        TRACKED.put(caster.id(), caster);
    }
}
