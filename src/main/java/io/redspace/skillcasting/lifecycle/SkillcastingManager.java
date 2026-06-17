package io.redspace.skillcasting.lifecycle;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.event.BuildCastContextEvent;
import io.redspace.skillcasting.api.event.BuildCooldownEvent;
import io.redspace.skillcasting.api.event.SkillCastCompleteEvent;
import io.redspace.skillcasting.api.event.SkillPreCastEvent;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.api.recast.RecastManager;
import io.redspace.skillcasting.api.recast.RecastResult;
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
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

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
        return attemptInitiateCast(caster, SkillRegistry.holder(selected.getSkill()), selected.getLevel(), selected.equipmentSlot);
    }

    public static boolean attemptInitiateFromQuickCastSlot(CasterRef caster, int globalIndex) {
        SkillcastingData data = caster.skillcastingData();
        SkillSelectionManager.SelectionOption option = data.selectionManager().getOptionAt(globalIndex);
        if (option == null) {
            return false;
        }
        return attemptInitiateCast(caster, SkillRegistry.holder(option.skillData.getSkill()), option.skillData.getLevel(), option.equipmentSlot);
    }

    public static CastContext buildCastContext(CasterRef caster, Holder<AbstractSkill> skillHolder, int baseLevel, @Nullable String equipmentSlot) {
        SkillcastingData skillcastingData = caster.skillcastingData();
        CastContext context = new CastContext(skillHolder, caster, caster.level());
        AbstractSkill skill = skillHolder.value();

        RecastManager recastManager = skillcastingData.recasts();
        if (recastManager.hasRecast(skillHolder)) {
            context.components().applyFrom(recastManager.get(skillHolder).components());
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
        BuildCastContextEvent.Level levelEvent = new BuildCastContextEvent.Level(context, baseLevel);
        NeoForge.EVENT_BUS.post(levelEvent);
        context.set(SkillcastingComponentTypes.SKILL_LEVEL, levelEvent.getLevel());
        skill.buildContextComponents(context);
        NeoForge.EVENT_BUS.post(new BuildCastContextEvent.Post(context));
        return context;
    }

    public static boolean initiateCast(CasterRef caster, CastContext castContext) {
        if (caster.level().isClientSide() || !caster.isValid()) {
            return false;
        }
        SkillcastingData skillcastingData = caster.skillcastingData();
        Holder<AbstractSkill> skillHolder = castContext.skill();
        AbstractSkill skill = skillHolder.value();
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

        skill.onServerPreCast(castContext);
        if (skill.getCastType() == CastType.INSTANT) {
            onCast(castContext);
            onCastComplete(caster, skillcastingData, castContext, CastEndReason.COMPLETED);
            return true;
        }

        skillcastingData.activateCast(new ActiveCast(castContext));
        castContext.components().markAllSyncedDirty();
        track(caster);
        SkillcastingNetwork.syncCastStart(caster, skillcastingData.getActiveCast());
        return true;
    }

    public static boolean attemptInitiateCast(CasterRef caster, Holder<AbstractSkill> skillHolder, int baseLevel, @Nullable String equipmentSlot) {
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
        CastContext castContext = buildCastContext(caster, skillHolder, baseLevel, equipmentSlot);
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

    public static void handleRecastTimeout(CasterRef caster, Holder<AbstractSkill> skill, RecastInstance instance) {
        SkillcastingNetwork.syncRecastRemove(caster, skill);
        CastContext castContext = new CastContext(skill, caster, caster.level());
        castContext.components().applyFrom(instance.components());
        skill.value().onRecastFinished(castContext, RecastResult.TIMEOUT);
        triggerCooldown(castContext);
    }

    private static void tickActiveCast(CasterRef caster, SkillcastingData data, ActiveCast active) {
        CastContext castContext = active.context();
        AbstractSkill skill = castContext.skill().value();
        SkillcastingNetwork.syncDirtyCastComponents(caster, castContext);
        long gameTime = caster.level().getGameTime();

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
                    SkillcastingNetwork.syncRecast(caster, skillHolder, recast);
                }
            } else {
                RecastConfig recastConfig = castContext.get(SkillcastingComponentTypes.RECAST_CONFIG);
                if (recastConfig != null) {
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
        SkillcastingNetwork.syncCastEnd(caster, reason);
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
