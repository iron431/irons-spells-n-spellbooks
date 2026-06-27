package io.redspace.skillcasting.irons_spellbooks;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.IAnimatedAttacker;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.render.animation.AnimationHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastResult;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.api.skill.SkillWheelInfo;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_CONTINUOUS_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_INSTANT_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_LONG_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_LONG_CAST_FINISH;

public abstract class AbstractSpellSkill extends AbstractSkill {

    protected float baseSpellPower, spellPowerPerLevel;
    protected int baseManaCost, manaCostPerLevel;
    protected int castTime;

    @Override
    public int getCastTimeTicks() {
        return castTime;
    }

    public int getManaCost(CastContext castContext) {
        if (castContext.has(SpellcastingComponentTypes.IGNORE_MANA)) {
            return 0;
        }
        return castContext.getOrDefault(SpellcastingComponentTypes.MANA_COST, 0);
    }

    public abstract DefaultConfig getDefaultConfig();

    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of();
    }

    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return SpellSkillDamageSource.source(level, projectile, attacker, this);
    }

    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity attacker) {
        return getDamageSource(level, attacker, attacker);
    }

    public MutableComponent getDisplayName(@Nullable Player player) {
        // fixme: implement learning
//        boolean obfuscateName = player != null && this.obfuscateStats(player);
//        return Component.translatable(getComponentId()).withStyle(obfuscateName ? ELDRITCH_OBFUSCATED_STYLE : Style.EMPTY);
        return Component.translatable(getDescriptionId());
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int scaledLevel = castContext.getSkillLevel() - 1;
        castContext.set(SpellcastingComponentTypes.MANA_COST, baseManaCost + manaCostPerLevel * scaledLevel);
        if (castContext.asEntityCaster() instanceof LivingEntity livingEntity) {
            // todo: all the school powers, and other attributes (cast time movespeed?)
            castContext.set(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, (float) livingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER));
        }
        if (castContext.asEntityCaster() instanceof Player player && player.getAbilities().instabuild) {
            if (!ServerConfigs.CREATIVE_COOLDOWN.get()) {
                castContext.set(SkillcastingComponentTypes.IGNORE_COOLDOWN, Unit.INSTANCE);
            }
            if (!ServerConfigs.CREATIVE_MANA_COST.get()) {
                castContext.set(SpellcastingComponentTypes.IGNORE_MANA, Unit.INSTANCE);
            }
        }
    }

    /**
     * @return Scaled spell power value based on the {@link AbstractSpellSkill#baseSpellPower} and {@link AbstractSpellSkill#spellPowerPerLevel}
     */
    public float getSpellPower(CastContext castContext) {
        return (baseSpellPower + spellPowerPerLevel * (castContext.getSkillLevel() - 1)) * getSpellPowerMultiplier(castContext);
    }

    /**
     * @return composite multipliers saved to this cast context based on generic spell power and school spell power
     */
    public float getSpellPowerMultiplier(CastContext castContext) {
        // todo: implement school power scaling
        return castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, 1f);
    }

    @Override
    public CastResult canBeCastBy(CastContext castContext) {
        MagicData magicData = castContext.caster().get().getData(DataAttachmentRegistry.MAGIC_DATA);
        int manaCost = getManaCost(castContext);
        if (manaCost > magicData.getMana()) {
            return CastResult.failure(Component.translatable("ui.irons_spellbooks.cast_error_mana", Component.translatable(castContext.skill().value().getDescriptionId())).withStyle(ChatFormatting.RED));
        }
        return super.canBeCastBy(castContext);
    }

    @Override
    public void onPostCast(CastContext castContext) {
        super.onPostCast(castContext);
        MagicData magicData = castContext.caster().get().getData(DataAttachmentRegistry.MAGIC_DATA);
        int manaCost = getManaCost(castContext);
        magicData.setMana(magicData.getMana() - manaCost);
        // fixme: blocks should be able to have magic data as well (post magic data refactor)
        if (castContext.asEntityCaster() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(magicData));
        }
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(getSchoolType().getCastSound()).toOpt();
    }

    @Override
    public int getCooldownTicks() {
        // fixme: full skill takeover (config)
        return (int) (getDefaultConfig().cooldownInSeconds * 20);
    }

    public SchoolType getSchoolType() {
        // fixme: full skill takeover (config)
        return SchoolRegistry.getSchool(getDefaultConfig().schoolResource);
    }

    @Override
    public String getDescriptionId() {
        if (cachedDescriptionId == null) {
            cachedDescriptionId = Util.makeDescriptionId("spell", getSkillId());
        }
        return cachedDescriptionId;
    }

    @Override
    public ResourceLocation getIconLocation() {
        return getSkillId().withPrefix("textures/gui/spell_icons/").withSuffix(".png");
    }

    @Override
    public void onClientCastComplete(CastContext castContext, CastEndReason castEndReason) {
        super.onClientCastComplete(castContext, castEndReason);
        // todo: get animation from cast context
        AnimationHolder finishAnimation = getCastFinishAnimation();
        boolean cancelled = castEndReason == CastEndReason.INTERRUPTED;
        // fixme: need pipeline for mobs to starting and canceling animations
        if (castContext.asEntityCaster() instanceof Player player) {
            if (finishAnimation.getForPlayer().isPresent() && !cancelled) {
                AnimationHelper.animatePlayerStart(player, finishAnimation.getForPlayer().get());
            } else if (finishAnimation != AnimationHolder.pass() || cancelled) {
                AnimationHelper.cancelPlayerAnimation((AbstractClientPlayer) player);
            }
        }
    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        // todo: get animation from cast context
        AnimationHolder holder = getCastStartAnimation();
        if (holder.isPass) {
            return;
        }
        if (castContext.asEntityCaster() instanceof Player player) {
            holder.getForPlayer().ifPresent(animation -> AnimationHelper.animatePlayerStart(player, animation));
        } else if (castContext.asEntityCaster() instanceof IAnimatedAttacker animatedAttacker) {
            //fixme: need dedicated pipeline for animating mobs, or rename IAnimatedAttacker or something
            // also, string? what about other mods or animation files?
            holder.getForPlayer().ifPresent(animation -> animatedAttacker.playAnimation(animation.getPath()));
        }
    }

    @Override
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
        int manaCost = castContext.has(SpellcastingComponentTypes.IGNORE_MANA) ? 0 : castContext.getOrDefault(SpellcastingComponentTypes.MANA_COST, 0);
        if (manaCost > 0) {
            if (this.getCastType() == CastType.CONTINUOUS) {
                info.leftText().add(Component.translatable("tooltip.irons_spellbooks.mana_cost_per_second", manaCost * 20 / continuousInterval()));
            } else {
                info.leftText().add(Component.translatable("tooltip.irons_spellbooks.mana_cost", manaCost));
            }
        }
        // fixme: add ignore cooldown handling
        info.leftText().add(Component.translatable("tooltip.skillcasting.cooldown_length", castContext.getOrDefault(SkillcastingComponentTypes.COOLDOWN_TICKS, 0) / 20.0 + "s"));
        info.rightText().addAll(getUniqueInfo(castContext));
        return info;
    }

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public AnimationHolder getCastStartAnimation() {
        return switch (getCastType()) {
            case CONTINUOUS -> ANIMATION_CONTINUOUS_CAST;
            case LONG -> ANIMATION_LONG_CAST;
            default -> AnimationHolder.none();
        };
    }

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public AnimationHolder getCastFinishAnimation() {
        return switch (getCastType()) {
            case LONG -> ANIMATION_LONG_CAST_FINISH;
            case INSTANT -> ANIMATION_INSTANT_CAST;
            default -> AnimationHolder.none();
        };
    }

    public boolean canBeInterrupted(@Nullable Player player) {
        // fixme: is player acceptable here? is long cast interruption a player only mechanic?
        return this.getCastType() == CastType.LONG && !ItemRegistry.CONCENTRATION_AMULET.get().isEquippedBy(player);
    }

    public boolean allowLooting() {
        return this.getSchoolType().allowLooting();
    }
}
