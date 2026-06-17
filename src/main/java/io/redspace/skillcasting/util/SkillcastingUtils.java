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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public final class SkillcastingUtils {
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
        String castSource = activeCast.context().get(SkillcastingComponentTypes.CAST_SOURCE);
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
