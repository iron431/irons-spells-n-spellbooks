package io.redspace.skillcasting.util;

import io.redspace.ironsspellbooks.entity.spells.root.PreventDismount;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.EntityCasterRef;
import io.redspace.skillcasting.api.component.MultiTargetEntityCastComponent;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SkillcastingUtils {

    /**
     * Unpositioned cone layer templates matching {@code AbstractConeProjectile} part sizes.
     */
    private static final List<AABB> CONE_LAYER_TEMPLATES = List.of(
            new AABB(0, 0, 0, 1, 1, 1),
            new AABB(0, 0, 0, 2.5, 1.5, 2.5),
            new AABB(0, 0, 0, 3.5, 2, 3.5),
            new AABB(0, 0, 0, 4.5, 3, 4.5)
    );

    /**
     * @return Cone origin used by breath/cone continuous skills (slightly below cast position).
     */
    public static Vec3 defaultConeOrigin(CastContext castContext) {
        return castContext.position().subtract(0, 0.5, 0);
    }

    /**
     * Builds world-space AABBs for a forward-facing cone emanating from {@code origin} along {@code direction}.
     */
    public static List<AABB> buildConeHitboxes(Vec3 origin, Vec3 direction) {
        List<AABB> coneColliders = new ArrayList<>(CONE_LAYER_TEMPLATES.size());
        for (AABB template : CONE_LAYER_TEMPLATES) {
            coneColliders.add(new AABB(template.minX, template.minY, template.minZ, template.maxX, template.maxY, template.maxZ));
        }
        for (int i = 0; i < coneColliders.size(); i++) {
            AABB collider = coneColliders.get(i);
            double distance = 1 + (i * collider.getXsize() / 2);
            Vec3 position = origin.add(direction.scale(distance));
            position = position.subtract(collider.getXsize() / 2, 0, collider.getZsize() / 2);
            coneColliders.set(i, collider.move(position));
        }
        return coneColliders;
    }

    /**
     * Collects unique entities intersecting a cone, excluding {@code caster}.
     */
    public static Set<Entity> collectConeTargets(Level level, @Nullable Entity caster, Vec3 origin, Vec3 direction, Predicate<Entity> filter) {
        return buildConeHitboxes(origin, direction).stream()
                .flatMap(aabb -> level.getEntities(caster, aabb).stream())
                .filter(filter)
                .collect(Collectors.toSet());
    }

    public static Set<Entity> collectConeTargets(CastContext castContext, Predicate<Entity> filter) {
        return collectConeTargets(
                castContext.level(),
                castContext.asEntityCaster(),
                defaultConeOrigin(castContext),
                castContext.direction(),
                filter
        );
    }

    public static boolean isConeProjectileTarget(Level level, Vec3 origin, Entity target) {
        return target.canBeHitByProjectile()
                && io.redspace.ironsspellbooks.api.util.Utils.hasLineOfSight(level, origin, target.getBoundingBox().getCenter(), true);
    }

    public static boolean isSameItemSameComponentsIgnoreDurability(ItemStack a, ItemStack b) {
        ItemStack left = a.copy();
        ItemStack right = b.copy();
        left.remove(DataComponents.DAMAGE);
        right.remove(DataComponents.DAMAGE);
        return ItemStack.isSameItemSameComponents(left, right);
    }

    public static boolean shouldCancelCastOnEquipmentChange(
            ActiveCast activeCast,
            ItemStack from,
            ItemStack to,
            EquipmentSlot changedSlot) {
        if (activeCast == null) {
            return false;
        }
        String castSource = activeCast.context().getOrNull(SkillcastingComponentTypes.CAST_SOURCE);
        if (castSource != null
                && castSource.equals(changedSlot.getName())
                && !SkillcastingUtils.isSameItemSameComponentsIgnoreDurability(from, to)) {
            return true;
        }
        if (ISkillContainer.isSkillContainer(from)) {
            AbstractSkill skill = activeCast.context().skill().value();
            if (ISkillContainer.get(from).getIndexForSkill(skill) >= 0
                    && !SkillcastingUtils.isSameItemSameComponentsIgnoreDurability(from, to)) {
                return true;
            }
        }
        return false;
    }

    public static boolean preCastTargetHelper(CastContext castContext, int range, float aimAssist) {
        return preCastTargetHelper(castContext, range, aimAssist, true);
    }

    public static boolean preCastTargetHelper(CastContext castContext, int range, float aimAssist, boolean sendFailureMessage) {
        return preCastTargetHelper(castContext, range, aimAssist, sendFailureMessage, entity -> true);
    }

    public static boolean preCastTargetHelper(
            CastContext castContext,
            int range,
            float aimAssist,
            boolean sendFailureMessage,
            Predicate<LivingEntity> filter) {
        Entity caster = castContext.caster() instanceof EntityCasterRef entityCaster ? entityCaster.entity() : null;
        HitResult target = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, range)
                .checkForBlocks(true)
                .bbInflation(aimAssist)
                .build();
        LivingEntity livingTarget = resolveLivingTarget(target, filter);
        if (livingTarget == caster) {
            return false;
        }
        if (livingTarget != null) {
            castContext.set(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES, new MultiTargetEntityCastComponent(livingTarget));
            AbstractSkill skill = castContext.skill().value();
            if (caster instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable(
                                "ui.irons_spellbooks.spell_target_success",
                                livingTarget.getDisplayName().getString(),
                                Component.translatable(skill.getDescriptionId())
                        ).withStyle(ChatFormatting.GREEN)));
            }
            if (livingTarget instanceof ServerPlayer serverPlayer) {
                MutableComponent message = caster == null ? Component.translatable("ui.irons_spellbooks.spell_target_warning_no_source", Component.translatable(skill.getDescriptionId())) : Component.translatable(
                        "ui.irons_spellbooks.spell_target_warning",
                        caster.getDisplayName().getString(),
                        Component.translatable(skill.getDescriptionId())
                );
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(message.withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
            return true;
        }
        if (sendFailureMessage && caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("ui.irons_spellbooks.cast_error_target").withStyle(ChatFormatting.RED)));
        }
        return false;
    }

    @Nullable
    // todo: should this be living entities only? its looking like probably
    private static LivingEntity resolveLivingTarget(HitResult target, Predicate<LivingEntity> filter) {
        if (!(target instanceof EntityHitResult entityHit)) {
            return null;
        }
        if (entityHit.getEntity() instanceof LivingEntity livingEntity && filter.test(livingEntity)) {
            return livingEntity;
        }
        if (entityHit.getEntity() instanceof PartEntity<?> partEntity
                && partEntity.getParent() instanceof LivingEntity livingParent
                && filter.test(livingParent)) {
            return livingParent;
        }
        if (entityHit.getEntity() instanceof PreventDismount
                && entityHit.getEntity().getFirstPassenger() instanceof LivingEntity livingRooted) {
            return livingRooted;
        }
        return null;
    }
}
