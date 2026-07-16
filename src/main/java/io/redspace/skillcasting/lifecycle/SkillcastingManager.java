package io.redspace.skillcasting.lifecycle;

import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.data.cast.ActiveCast;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CasterId;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.api.event.BuildCastContextEvent;
import io.redspace.skillcasting.api.event.BuildCooldownEvent;
import io.redspace.skillcasting.api.event.SkillCastCompleteEvent;
import io.redspace.skillcasting.api.event.SkillPreCastEvent;
import io.redspace.skillcasting.data.recast.RecastConfig;
import io.redspace.skillcasting.data.recast.RecastInstance;
import io.redspace.skillcasting.data.recast.RecastManager;
import io.redspace.skillcasting.data.resolver.CasterDirectionResolver;
import io.redspace.skillcasting.data.resolver.CasterPositionResolver;
import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.data.cast.CastResult;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.cooldown.CooldownInstance;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.data.selection.SkillSelection;
import io.redspace.skillcasting.data.selection.SkillSelectionManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillcastingManager {
    /**
     * Live refs for active skillcasts being tracked by the manager
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
        return attemptInitiateCast(caster, SkillRegistry.holder(selected.getSkill()), selected.getLevel(), CastSource.of(selected.equipmentSlot));
    }

    public static boolean attemptInitiateFromQuickCastSlot(CasterRef caster, int globalIndex) {
        SkillcastingData data = caster.skillcastingData();
        SkillSelectionManager.SelectionOption option = data.selectionManager().getOptionAt(globalIndex);
        if (option == null) {
            return false;
        }
        return attemptInitiateCast(caster, SkillRegistry.holder(option.skillData.getSkill()), option.skillData.getLevel(), CastSource.of(option.equipmentSlot));
    }

    public static CastContext buildCastContext(CasterRef caster, Holder<AbstractSkill> skillHolder, int baseLevel, CastSource castSource) {
        CastContext context = new CastContext(skillHolder, caster, caster.level());
        AbstractSkill skill = skillHolder.value();
        context.set(SkillcastingComponentTypes.POSITION_RESOLVER, CasterPositionResolver.INSTANCE);
        context.set(SkillcastingComponentTypes.DIRECTION_RESOLVER, CasterDirectionResolver.INSTANCE);
        context.set(SkillcastingComponentTypes.CAST_TIME, skill.getCastTimeTicks());
        context.set(SkillcastingComponentTypes.COOLDOWN_TICKS, skill.getCooldownTicks());
        context.set(SkillcastingComponentTypes.CAST_SOURCE, castSource);

        BuildCastContextEvent.Level levelEvent = new BuildCastContextEvent.Level(context, baseLevel);
        NeoForge.EVENT_BUS.post(levelEvent);
        context.set(SkillcastingComponentTypes.SKILL_LEVEL, levelEvent.getLevel());

        skill.buildContextComponents(context);
        NeoForge.EVENT_BUS.post(new BuildCastContextEvent.Post(context));

        skill.provideRecastConfig(context).ifPresent(recast -> context.set(SkillcastingComponentTypes.RECAST_CONFIG, recast));
        return context;
    }

    /**
     * Checks physical ability constraints via {@link AbstractSkill#checkPreCastConditions(CastContext)}, then initiates a cast absent any other criteria
     *
     * @return whether the cast is initiated
     */
    public static boolean initiateCast(CasterRef caster, CastContext castContext) {
        if (caster.level().isClientSide() || !caster.isValid()) {
            return false;
        }
        SkillcastingData skillcastingData = caster.skillcastingData();
        Holder<AbstractSkill> skillHolder = castContext.skill();
        AbstractSkill skill = skillHolder.value();
        RecastManager recastManager = skillcastingData.recasts();
        if (recastManager.hasRecast(skillHolder)) {
            castContext.components().applyFrom(recastManager.get(skillHolder).components());
        }
        if (skillcastingData.getActiveCast() != null) {
            endCast(caster, skillcastingData, skillcastingData.getActiveCast(), CastEndReason.INTERRUPTED);
        }
        if (!skill.checkPreCastConditions(castContext)) {
            return false;
        }
        SkillPreCastEvent preCast = new SkillPreCastEvent(castContext);
        NeoForge.EVENT_BUS.post(preCast);
        if (preCast.isCanceled()) {
            return false;
        }

        skillcastingData.activateCast(new ActiveCast(castContext));
        skill.onServerCastStart(castContext);
        castContext.components().markAllSyncedDirty();
        track(caster);
        SkillcastingNetwork.syncCastStart(caster, skillcastingData.getActiveCast());
        return true;
    }

    /**
     * Helper for building a {@link CastContext}, and evaluating the capability for a caster to initiate a cast via {@link AbstractSkill#canBeCastBy(CastContext)}. Forwards to {@link SkillcastingManager#initiateCast(CasterRef, CastContext)}.
     *
     * @return whether the cast is successfully initiated
     */
    public static boolean attemptInitiateCast(CasterRef caster, Holder<AbstractSkill> skillHolder, int baseLevel, CastSource castSource) {
        if (caster.level().isClientSide() || !caster.isValid()) {
            return false;
        }
        SkillcastingData skillcastingData = caster.skillcastingData();
        ActiveCast existingCast = skillcastingData.getActiveCast();
        if (existingCast != null) {
            endCast(caster, skillcastingData, existingCast, CastEndReason.INTERRUPTED);
            if (existingCast.context().skill().equals(skillHolder)) {
                return false;
            }
        }
        CastContext castContext = buildCastContext(caster, skillHolder, baseLevel, castSource);
        CastResult result = skillHolder.value().canBeCastBy(castContext);
        if (caster.get() instanceof ServerPlayer serverPlayer && result.message() != null) {
            serverPlayer.displayClientMessage(result.message(), true);
        }
        if (result.isFailure()) {
            return false;
        }
        return initiateCast(caster, castContext);
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
        endCast(caster, caster.skillcastingData(), active, reason);
        if (triggerCooldown) {
            triggerCooldown(context);
        }
    }

    public static void serverTick() {
        for (Map.Entry<CasterId, CasterRef> entry : TRACKED.entrySet()) {
            CasterId id = entry.getKey();
            CasterRef caster = entry.getValue();

            if (!caster.isValid()) {
                TRACKED.remove(id);
                continue;
            }

            SkillcastingData skillcastingData = caster.skillcastingData();
            ActiveCast active = skillcastingData.getActiveCast();
            if (active != null) {
                tickActiveCast(caster, skillcastingData, active);
            } else {
                TRACKED.remove(id);
            }
        }
    }

    public static void serverStopped() {
        TRACKED.clear();
    }

    private static void tickActiveCast(CasterRef caster, SkillcastingData data, ActiveCast active) {
        CastContext castContext = active.context();
        AbstractSkill skill = castContext.skill().value();
        SkillcastingNetwork.syncDirtyCastComponents(caster, castContext);
        long gameTime = caster.level().getGameTime();
        CastType castType = skill.getCastType();
        if (castType == CastType.INSTANT) {
            onCast(castContext);
            endCast(caster, data, active, CastEndReason.COMPLETED);
            return;
        }
        skill.onServerCastTick(castContext);
        int elapsed = active.elapsedTicks(gameTime);
        if (castType == CastType.CONTINUOUS) {
            int interval = Math.max(1, skill.continuousInterval());
            if (elapsed % interval == 1) {
                onCast(castContext);
            }
        }
        if (elapsed >= active.durationTicks()) {
            if (castType == CastType.LONG) {
                onCast(castContext);
            }
            endCast(caster, data, active, CastEndReason.COMPLETED);
        }
    }

    // ---- execution / completion ----------------------------------------------------------------

    private static void onCast(CastContext castContext) {
        // todo: cast event
        castContext.skill().value().onCast((ServerLevel) castContext.level(), castContext);
        castContext.skill().value().onPostCast(castContext);
    }

    private static void endCast(CasterRef caster, SkillcastingData data, ActiveCast active, CastEndReason reason) {
        data.endActiveCast();
        onCastComplete(caster, data, active.context(), reason);
    }

    private static void onCastComplete(CasterRef caster, SkillcastingData data, CastContext castContext, CastEndReason reason) {
        AbstractSkill skill = castContext.skill().value();
        // on cast complete
        skill.onServerCastComplete(castContext, reason);
        NeoForge.EVENT_BUS.post(new SkillCastCompleteEvent(castContext, reason));
        // handle recasting
        boolean isOnRecast = false;
        boolean completedToFruition = reason.isCompletion() || skill.getCastType() == CastType.CONTINUOUS;
        if (completedToFruition) {
            RecastManager recasts = castContext.getSkillcastingData().recasts();
            Holder<AbstractSkill> skillHolder = castContext.skill();
            if (recasts.hasRecast(skillHolder)) {
                isOnRecast = recasts.handleRecastConsumption(castContext);
                RecastInstance recast = recasts.get(skillHolder);
                if (recast == null) {
                    SkillcastingNetwork.syncRecastRemove(caster, skillHolder);
                } else {
                    recast.components().applyFrom(castContext.components());
                    SkillcastingNetwork.syncRecast(caster, skillHolder, recast);
                }
            } else {
                RecastConfig recastConfig = castContext.getOrNull(SkillcastingComponentTypes.RECAST_CONFIG);
                if (recastConfig != null && recastConfig.totalCasts() > 1) {
                    RecastInstance instance = new RecastInstance(recastConfig, castContext);
                    data.recasts().addRecast(instance);
                    isOnRecast = true;
                    SkillcastingNetwork.syncRecast(caster, instance.skill(), instance);
                }
            }
        }
        // handle cooldown
        if (completedToFruition && !isOnRecast) {
            triggerCooldown(castContext);
        }
        // sync
        SkillcastingNetwork.syncCastEnd(caster, castContext, reason);
    }

    public static void triggerCooldown(CastContext castContext) {
        Holder<AbstractSkill> skill = castContext.skill();
        if (castContext.has(SkillcastingComponentTypes.IGNORE_COOLDOWN)) {
            return;
        }
        int cooldownTicks = castContext.getOrDefault(SkillcastingComponentTypes.COOLDOWN_TICKS, skill.value().getCooldownTicks());
        if (cooldownTicks == 0) {
            return;
        }
        triggerCooldown(castContext, cooldownTicks);
    }

    public static void triggerCooldown(CastContext castContext, int cooldownTicks) {
        Holder<AbstractSkill> skill = castContext.skill();
        BuildCooldownEvent cooldownEvent = new BuildCooldownEvent(castContext, cooldownTicks);
        NeoForge.EVENT_BUS.post(cooldownEvent);
        if (cooldownEvent.getTicks() > 0) {
            CooldownInstance instance = CooldownInstance.of(cooldownEvent.getTicks());
            castContext.getSkillcastingData().cooldowns().addCooldown(skill, instance);
            SkillcastingNetwork.syncCooldown(castContext.caster(), skill, instance);
        }
    }

    private static void track(CasterRef caster) {
        TRACKED.put(caster.id(), caster);
    }
}
