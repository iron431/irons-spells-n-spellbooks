package io.redspace.skillcasting.util;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.root.PreventDismount;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.component.TargetedEntitiesData;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.data.CastSource;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SkillcastingUtils {

    private static final List<AABB> CONE_LAYER_TEMPLATES = List.of(
            new AABB(0, 0, 0, 1, 1, 1),
            new AABB(0, 0, 0, 2.5, 1.5, 2.5),
            new AABB(0, 0, 0, 3.5, 2, 3.5),
            new AABB(0, 0, 0, 4.5, 3, 4.5)
    );

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

    public static Set<Entity> collectConeTargets(Level level, @Nullable Entity caster, Vec3 origin, Vec3 direction, float range, Predicate<Entity> filter) {
        float radius = range / 2;
        Vec3 end = origin.add(direction.scale(range));
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right;
        if (Math.abs(up.dot(direction)) > .99) {
            up = new Vec3(1, 0, 0);
            right = new Vec3(0, 0, 1);
        } else {
            right = direction.cross(up).normalize();
            up = right.cross(direction).normalize();
        }
        AABB box = new AABB(end, origin)
                .expandTowards(up.scale(radius))
                .expandTowards(up.scale(-radius))
                .expandTowards(right.scale(radius))
                .expandTowards(right.scale(-radius))
                .inflate(1);
        float heightRatio = 4.5f / 3f; // magic numbers based on the original cone height/width ratio
        List<? extends Entity> entities = level.getEntities(caster, box, filter);
        HashSet<Entity> set = new HashSet<>();
        float threshold = Mth.cos(30 * Mth.DEG_TO_RAD);
        for (Entity e : entities) {
            Vec3 delta = e.getBoundingBox().getCenter().subtract(origin.subtract(direction));
            if (delta.lengthSqr() <= (range + 1) * (range + 1) &&
                    delta.multiply(1, heightRatio, 1).normalize().dot(direction) >= threshold &&
                    Utils.hasLineOfSight(level, origin, e.getBoundingBox().getCenter(), true)
            ) {
                set.add(e);
            }
        }
        boolean debugParticles = false;
        if (debugParticles) {
            float density = 1f;
            for (float x = 0; x < box.getXsize(); x += density) {
                for (float y = 0; y < box.getYsize(); y += density) {
                    for (float z = 0; z < box.getZsize(); z += density) {
                        Vec3 particle = box.getMinPosition().add(x, y, z);
                        Vec3 delta = particle.subtract(origin);
                        boolean predicate = (delta.lengthSqr() <= range * range && delta.multiply(1, heightRatio, 1).normalize().dot(direction) >= threshold);
                        MagicManager.spawnParticles(level, predicate ? ParticleHelper.ELECTRICITY : ParticleHelper.SNOWFLAKE, particle.x, particle.y, particle.z, 1, 0, 0, 0, 0, true);
                    }
                }
            }
        }
        return set;
    }

    public static Set<Entity> collectConeTargets(CastContext castContext, Predicate<Entity> filter) {
        return collectConeTargets(
                castContext.level(),
                castContext.asEntityCaster(),
                castContext.position(PositionAnchor.CASTING_POSITION_CENTER),
                castContext.direction(),
                castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 7.75f),
                filter
        );
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
        CastSource castSource = activeCast.context().getOrNull(SkillcastingComponentTypes.CAST_SOURCE);
        boolean isChangingActiveCast = castSource != null && castSource.equipmentSlot().equals(changedSlot.getName())
                || (ISkillContainer.isSkillContainer(from) && (ISkillContainer.get(from).getIndexForSkill(activeCast.context().skill().value()) >= 0));
        if (!isChangingActiveCast) {
            return false;
        }
        return !SkillcastingUtils.isSameItemSameComponentsIgnoreDurability(from, to) || from.getCount() != to.getCount();
    }

    public static @Nullable Entity getTargetedEntity(ServerLevel level, CastContext castContext) {
        return castContext.find(SkillcastingComponentTypes.TARGETED_ENTITIES)
                .map(data -> data.getFirstEntityTarget(level))
                .orElse(null);
    }

    public static Optional<Vec3> getTargetedEntityPosition(ServerLevel level, CastContext castContext) {
        return Optional.ofNullable(getTargetedEntity(level, castContext)).map(Entity::position);
    }

    public static @Nullable LivingEntity getTargetedLivingEntity(ServerLevel level, CastContext castContext) {
        return castContext.find(SkillcastingComponentTypes.TARGETED_ENTITIES)
                .map(data -> data.getFirstLivingEntityTarget(level))
                .orElse(null);
    }

    public static boolean preCastTargetHelper(CastContext castContext, float aimAssist) {
        return preCastTargetHelper(castContext, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), aimAssist);
    }

    public static boolean preCastTargetHelper(CastContext castContext, float aimAssist, boolean sendFailureMessage) {
        return preCastTargetHelper(castContext, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), aimAssist, sendFailureMessage);
    }

    public static boolean preCastTargetHelper(
            CastContext castContext,
            float aimAssist,
            boolean sendFailureMessage,
            Predicate<LivingEntity> filter) {
        return preCastTargetHelper(castContext, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), aimAssist, sendFailureMessage, filter);
    }

    public static boolean preCastTargetHelper(CastContext castContext, float range, float aimAssist) {
        return preCastTargetHelper(castContext, range, aimAssist, true);
    }

    public static boolean preCastTargetHelper(CastContext castContext, float range, float aimAssist, boolean sendFailureMessage) {
        return preCastTargetHelper(castContext, range, aimAssist, sendFailureMessage, entity -> true);
    }

    public static boolean preCastTargetHelper(
            CastContext castContext,
            float range,
            float aimAssist,
            boolean sendFailureMessage,
            Predicate<LivingEntity> filter) {
        Entity caster = castContext.asEntityCaster();
        HitResult hitResult = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, range)
                .checkForBlocks(true)
                .bbInflation(aimAssist)
                .build();
        LivingEntity livingTarget = resolveLivingTarget(hitResult, filter);
        if (livingTarget == caster) {
            return false;
        }
        if (livingTarget != null) {
            castContext.set(SkillcastingComponentTypes.TARGETED_ENTITIES, new TargetedEntitiesData(livingTarget));
            AbstractSkill skill = castContext.skill().value();
            if (caster instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("ui.irons_spellbooks.spell_target_success",
                                livingTarget.getDisplayName().getString(),
                                Component.translatable(skill.getDescriptionId())
                        ).withStyle(ChatFormatting.GREEN)));
            }
            if (livingTarget instanceof ServerPlayer serverPlayer) {
                MutableComponent message = caster == null ?
                        Component.translatable("ui.irons_spellbooks.spell_target_warning_no_source", Component.translatable(skill.getDescriptionId())) :
                        Component.translatable("ui.irons_spellbooks.spell_target_warning", caster.getDisplayName().getString(), Component.translatable(skill.getDescriptionId()));
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
