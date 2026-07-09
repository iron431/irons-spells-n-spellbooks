package io.redspace.skillcasting.irons_spellbooks;

import com.google.common.util.concurrent.AtomicDouble;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellCastSources;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
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
import io.redspace.skillcasting.data.CastSource;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_CONTINUOUS_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_INSTANT_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_LONG_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_LONG_CAST_FINISH;

public abstract class AbstractSpellSkill extends AbstractSkill {
    public static final Style ELDRITCH_OBFUSCATED_STYLE = Style.EMPTY.withObfuscated(true).withFont(ResourceLocation.withDefaultNamespace("alt"));

    protected float baseSpellPower, spellPowerPerLevel;
    protected int baseManaCost, manaCostPerLevel;
    protected int castTime;

    private volatile List<Double> rarityWeights;

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

    @Override
    public MutableComponent getDisplayName(@Nullable Player player) {
        boolean obfuscateName = player != null && this.obfuscateStats(player);
        return Component.translatable(getDescriptionId()).withStyle(obfuscateName ? ELDRITCH_OBFUSCATED_STYLE : Style.EMPTY);
    }

    public boolean isLearned(@Nullable IAttachmentHolder attachmentHolder) {
        if (attachmentHolder == null) {
            return false;
        } else {
            return MagicData.get(attachmentHolder).getLearnedSpellData().isLearned(this);
        }
    }

    public boolean requiresLearning() {
        return this.getSchoolType().requiresLearning();
    }

    public boolean isEnabled() {
        return SpellConfigManager.getSpellConfigValue(this, SpellConfigParameter.ENABLED);
    }

    public boolean obfuscateStats(@Nullable Player player) {
        return requiresLearning() && !isLearned(player);
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int scaledLevel = castContext.getSkillLevel() - 1;
        castContext.set(SpellcastingComponentTypes.MANA_COST, baseManaCost + manaCostPerLevel * scaledLevel);
        if (castContext.getRecastsRemaining() > 0) {
            castContext.set(SpellcastingComponentTypes.IGNORE_MANA, Unit.INSTANCE);
        }
        if (castContext.find(SkillcastingComponentTypes.CAST_SOURCE).filter(source -> source.name().equals(SpellCastSources.SCROLL)).isPresent()) {
            castContext.set(SpellcastingComponentTypes.IGNORE_MANA, Unit.INSTANCE);
            // todo: likely remove anti-cooldown in future balance patch.
            castContext.set(SkillcastingComponentTypes.IGNORE_COOLDOWN, Unit.INSTANCE);
            if (castContext.asEntityCaster() instanceof ServerPlayer serverPlayer) {
                EquipmentSlot slot = EquipmentSlot.CODEC.byName(castContext.getOrDefault(SkillcastingComponentTypes.CAST_SOURCE, CastSource.EMPTY).equipmentSlot());
                if (slot != null) {
                    castContext.set(SpellcastingComponentTypes.SCROLL_STACK, serverPlayer.getItemBySlot(slot));
                }
            }
        }
        castContext.set(SpellcastingComponentTypes.CAST_START_ANIMATION, getCastStartAnimation());
        castContext.set(SpellcastingComponentTypes.CAST_FINISH_ANIMATION, getCastFinishAnimation());
        if (castContext.asEntityCaster() instanceof LivingEntity livingEntity) {
            // todo: other attributes (cast time movespeed?)
            castContext.set(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, (float) livingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER));
            for (SchoolType type : SchoolRegistry.REGISTRY) {
                castContext.set(type.getPowerComponent(), (float) type.getPowerFor(livingEntity));
            }
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
     * @return calculates the composite multiplier saved to this cast context based on generic spell power and school spell power
     */
    public float getSpellPowerMultiplier(CastContext castContext) {
        return castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, 1f)
                * castContext.getOrDefault(getSchoolType().getPowerComponent(), 1f);
    }

    @Override
    public CastResult canBeCastBy(CastContext castContext) {
        MagicData magicData = castContext.caster().get().getData(DataAttachmentRegistry.MAGIC_DATA);
        if (this.requiresLearning() && !isLearned(castContext.caster().get())) {
            return CastResult.failure(Component.translatable("ui.irons_spellbooks.cast_error_unlearned", Component.translatable(castContext.skill().value().getDescriptionId())).withStyle(ChatFormatting.RED));
        }
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
        // fixme: is mana player-only? (blocks default to 0 mana and immediately cancel)
        if (castContext.asEntityCaster() instanceof Player && castContext.skill().value().getCastType() == CastType.CONTINUOUS && manaCost > magicData.getMana()) {
            SkillcastingManager.cancelCast(castContext.caster(), CastEndReason.INTERRUPTED);
            if (castContext.asEntityCaster() instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("ui.irons_spellbooks.cast_error_mana", Component.translatable(castContext.skill().value().getDescriptionId())).withStyle(ChatFormatting.RED), true);
            }
        }
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
        handleCastFinishAnimation(castContext, castEndReason);
    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        MagicData.get(castContext.caster().get()).setCachedCastingEquipmentSlot(castContext.getOrDefault(SkillcastingComponentTypes.CAST_SOURCE, CastSource.EMPTY).equipmentSlot());
        handleCastStartAnimation(castContext);
    }

    protected void handleCastFinishAnimation(CastContext castContext, CastEndReason castEndReason) {
        AnimationHolder finishAnimation = castContext.getOrDefault(SpellcastingComponentTypes.CAST_FINISH_ANIMATION, AnimationHolder.pass());
        boolean cancelled = castEndReason == CastEndReason.INTERRUPTED;
        // fixme: need pipeline for mobs to starting and canceling animations
        if (finishAnimation.getType() == AnimationHolder.Type.PASS) {
            return;
        }
        if (castContext.asEntityCaster() instanceof Player player) {
            if (finishAnimation.getAnimationResource().isPresent() && !cancelled) {
                AnimationHelper.animatePlayerStart(player, finishAnimation.getAnimationResource().get());
            } else if (finishAnimation.getType() == AnimationHolder.Type.STOP || cancelled) {
                AnimationHelper.cancelPlayerAnimation((AbstractClientPlayer) player);
            }
        }
    }

    protected void handleCastStartAnimation(CastContext castContext) {
        AnimationHolder animation = castContext.getOrDefault(SpellcastingComponentTypes.CAST_START_ANIMATION, AnimationHolder.pass());
        if (animation.getType() != AnimationHolder.Type.ANIMATION) {
            return;
        }
        if (castContext.asEntityCaster() instanceof Player player) {
            animation.getAnimationResource().ifPresent(resourceLocation -> AnimationHelper.animatePlayerStart(player, resourceLocation));
        } else if (castContext.asEntityCaster() instanceof IAnimatedAttacker animatedAttacker) {
            //fixme: need dedicated pipeline for animating mobs, or rename IAnimatedAttacker or something
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
            default -> AnimationHolder.pass();
        };
    }

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public AnimationHolder getCastFinishAnimation() {
        return switch (getCastType()) {
            case LONG -> ANIMATION_LONG_CAST_FINISH;
            case INSTANT -> ANIMATION_INSTANT_CAST;
            default -> AnimationHolder.stop();
        };
    }

    public boolean canBeInterrupted(@Nullable Player player) {
        // fixme: is player acceptable here? is long cast interruption a player only mechanic?
        return this.getCastType() == CastType.LONG && !ItemRegistry.CONCENTRATION_AMULET.get().isEquippedBy(player);
    }

    /**
     * Returns whether this spell can be generated from random loot when no other criteria are specified
     */
    public boolean allowLooting() {
        return this.getSchoolType().allowLooting();
    }

    @Override
    public Vector3f getAccentColor() {
        return this.getSchoolType().getTargetingColor();
    }

    @Deprecated(forRemoval = true)
    public float getEntityPowerMultiplier(@Nullable LivingEntity livingEntity) {
        // fixme: definitely a nice helper, but does not have the power of a cast context behind it
        //  should cases which need this (spell effect scaling usually) create their own context and manually calculate?
        //  should this method take a context and fetch the parameter multipliers based on our school?
        //  either way, a wrapper directly touching attributes is not the way to go
        if (livingEntity == null) {
            return 1f;
        }
        return (float) this.getSchoolType().getPowerFor(livingEntity) * (float) livingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER);
    }

    public void resetRarityWeights() {
        rarityWeights = null;
    }

    private void initializeRarityWeights() {
        synchronized (this) {
            if (rarityWeights == null) {
                int minRarity = getMinRarity();
                int maxRarity = getMaxRarity();
                List<Double> rarityRawConfig = SpellRarity.getRawRarityConfig();
                List<Double> rarityConfig = SpellRarity.getRarityConfig();

                if (minRarity != 0) {
                    //Must balance remaining weights
                    var subList = rarityRawConfig.subList(minRarity, maxRarity + 1);
                    double subtotal = subList.stream().reduce(0d, Double::sum);
                    List<Double> rarityRawWeights = subList.stream().map(item -> ((item / subtotal) * (1 - subtotal)) + item).toList();

                    var counter = new AtomicDouble();
                    var weights = new ArrayList<Double>();
                    rarityRawWeights.forEach(item -> weights.add(counter.addAndGet(item)));
                    rarityWeights = weights;
                } else {
                    rarityWeights = rarityConfig;
                }
            }
        }
    }

    public int getMaxRarity() {
        return SpellRarity.LEGENDARY.getValue();
    }

    public SpellRarity getRarity(int level) {
        if (rarityWeights == null) {
            initializeRarityWeights();
        }

        int maxLevel = getMaxLevel();
        int maxRarity = getMaxRarity();
        if (maxLevel == 1) {
            return SpellRarity.values()[getMinRarity()];
        }
        if (level >= maxLevel) {
            return SpellRarity.LEGENDARY;
        }
        double percentOfMaxLevel = (double) level / (double) maxLevel;

        int lookupOffset = maxRarity + 1 - rarityWeights.size();

        for (int i = 0; i < rarityWeights.size(); i++) {
            if (percentOfMaxLevel <= rarityWeights.get(i)) {
                return SpellRarity.values()[i + lookupOffset];
            }
        }

        return SpellRarity.COMMON;
    }

    public Component getLockedMessage() {
        // fixme: expose parameters?
        return Component.translatable("ui.irons_spellbooks.unlearned_error");
    }

    /**
     * Returns an additional condition for whether this spell can be crafted by a player. This does NOT omit it from the scroll forge entirely
     */
    public boolean canBeCraftedBy(Player player) {
        return !requiresLearning() || isLearned(player);
    }

    /**
     * Returns an additional condition for whether this spell can be crafted in the scroll forge, or whether it will be omitted
     */
    public int getMinRarity() {
        return SpellConfigManager.getSpellConfigValue(this, SpellConfigParameter.MIN_RARITY).getValue();
    }

    public boolean allowCrafting() {
        return SpellConfigManager.getSpellConfigValue(this, SpellConfigParameter.ALLOW_CRAFTING);
    }

    public int getMinLevelForRarity(SpellRarity rarity) {
        if (rarityWeights == null) {
            initializeRarityWeights();
        }

        int minRarity = getMinRarity();
        int maxLevel = getMaxLevel();
        if (rarity.getValue() < minRarity) {
            return 0;
        }

        if (rarity.getValue() == minRarity) {
            return 1;
        }

        return (int) (rarityWeights.get(rarity.getValue() - (1 + minRarity)) * maxLevel) + 1;
    }
}
