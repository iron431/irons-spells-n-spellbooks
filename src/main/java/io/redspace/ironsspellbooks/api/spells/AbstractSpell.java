package io.redspace.ironsspellbooks.api.spells;

import com.google.common.util.concurrent.AtomicDouble;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.entity.IAnimatedCastingMob;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.render.animation.AnimationHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.data.cast.CastResult;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.component.TargetedEntitiesData;
import io.redspace.skillcasting.data.skill.SkillWheelInfo;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.data.selection.SkillSelectionManager;
import io.redspace.skillcasting.util.SkillcastingUtils;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_CONTINUOUS_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_INSTANT_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_LONG_CAST;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.ANIMATION_LONG_CAST_FINISH;

public abstract class AbstractSpell extends AbstractSkill {
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

    public SpellDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return SpellDamageSource.source(this, level, projectile, attacker, null);
    }

    public final SpellDamageSource getDamageSourceDirect(CastContext castContext) {
        var source = getDamageSource(castContext.level(), castContext.asEntityCaster(), castContext.asEntityCaster());
        source.sourcePosition = castContext.position(PositionAnchor.BOTTOM_CENTER);
        return source;
    }

    public final SpellDamageSource getDamageSourceIndirect(CastContext castContext) {
        var source = getDamageSource(castContext.level(), null, castContext.asEntityCaster());
        source.sourcePosition = castContext.position(PositionAnchor.BOTTOM_CENTER);
        return source;
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
        if (castContext.getCastSource().name().equals(SpellCastSources.SCROLL)) {
            castContext.set(SpellcastingComponentTypes.IGNORE_MANA, Unit.INSTANCE);
            // todo: likely remove anti-cooldown in future balance patch.
            castContext.set(SkillcastingComponentTypes.IGNORE_COOLDOWN, Unit.INSTANCE);
            if (castContext.asEntityCaster() instanceof ServerPlayer serverPlayer) {
                EquipmentSlot slot = EquipmentSlot.CODEC.byName(castContext.getCastSource().equipmentSlot());
                if (slot != null) {
                    castContext.set(SpellcastingComponentTypes.SCROLL_STACK, serverPlayer.getItemBySlot(slot));
                }
            }
        }
        castContext.set(SpellcastingComponentTypes.CAST_START_ANIMATION, getCastStartAnimation());
        castContext.set(SpellcastingComponentTypes.CAST_FINISH_ANIMATION, getCastFinishAnimation());
        if (castContext.asEntityCaster() instanceof LivingEntity livingEntity) {
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
        castContext.set(SpellcastingComponentTypes.ANIMATION_SPEED, SkillcastingUtils.getCastRateSpeed(castContext));
    }

    /**
     * @return Scaled spell power value based on the {@link AbstractSpell#baseSpellPower} and {@link AbstractSpell#spellPowerPerLevel}
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
        if (castContext.has(SkillcastingComponentTypes.RECAST_CONFIG) && castContext.getCastSource().name().equals(SpellCastSources.SCROLL) && !((castContext.asEntityCaster() instanceof Player player && player.isCreative()))) {
            return CastResult.failure(Component.translatable("ui.irons_spellbooks.cast_error_scroll", Component.translatable(castContext.skill().value().getDescriptionId())).withStyle(ChatFormatting.RED));
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
        if (castContext.skill().value().getCastType() == CastType.CONTINUOUS && manaCost > magicData.getMana()) {
            SkillcastingManager.cancelCast(castContext.caster(), CastEndReason.INTERRUPTED);
            if (castContext.asEntityCaster() instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("ui.irons_spellbooks.cast_error_mana", Component.translatable(castContext.skill().value().getDescriptionId())).withStyle(ChatFormatting.RED), true);
            }
        }
        SyncManaPacket.syncFor(castContext.caster());
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(getSchoolType().getCastSound()).toOpt();
    }

    @Override
    public float getBaseCastingMovespeedMultiplier() {
        return 0.2f;
    }

    @Override
    public int getCooldownTicks() {
        return (int) (SpellConfigManager.getSpellConfigValue(this, SpellConfigParameter.COOLDOWN_IN_SECONDS) * 20);
    }

    public SchoolType getSchoolType() {
        return SpellConfigManager.getSpellConfigValue(this, SpellConfigParameter.SCHOOL);

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
        MagicData.get(castContext.caster().get()).setCachedCastingEquipmentSlot(castContext.getCastSource().equipmentSlot());
        handleCastStartAnimation(castContext);
    }

    protected void handleCastFinishAnimation(CastContext castContext, CastEndReason castEndReason) {
        AnimationHolder finishAnimation = castContext.getOrDefault(SpellcastingComponentTypes.CAST_FINISH_ANIMATION, AnimationHolder.pass());
        boolean cancelled = castEndReason == CastEndReason.INTERRUPTED;
        if (finishAnimation.getType() == AnimationHolder.Type.PASS && !cancelled) {
            return;
        }
        if (castContext.asEntityCaster() instanceof Player player) {
            if (finishAnimation.getAnimation().isPresent() && !cancelled) {
                float speedModifier = castContext.getOrDefault(SpellcastingComponentTypes.ANIMATION_SPEED, 1f);
                AnimationHelper.animatePlayerStart(player, finishAnimation.getAnimation().get(), speedModifier);
            } else {
                AnimationHelper.cancelPlayerAnimation((AbstractClientPlayer) player);
            }
        } else if (castContext.asEntityCaster() instanceof IAnimatedCastingMob animatedCastingMob) {
            animatedCastingMob.playCastingAnimation(cancelled ? AnimationHolder.stop() : finishAnimation, castContext.getOrDefault(SpellcastingComponentTypes.ANIMATION_SPEED, 1f));
        }
    }

    protected void handleCastStartAnimation(CastContext castContext) {
        AnimationHolder animation = castContext.getOrDefault(SpellcastingComponentTypes.CAST_START_ANIMATION, AnimationHolder.pass());
        if (animation.getType() != AnimationHolder.Type.ANIMATION) {
            return;
        }
        if (castContext.asEntityCaster() instanceof Player player) {
            float speedModifier = castContext.getOrDefault(SpellcastingComponentTypes.ANIMATION_SPEED, 1f);
            animation.getAnimation().ifPresent(resourceLocation -> AnimationHelper.animatePlayerStart(player, resourceLocation, speedModifier));
        } else if (castContext.asEntityCaster() instanceof IAnimatedCastingMob animatedCastingMob) {
            animatedCastingMob.playCastingAnimation(animation, castContext.getOrDefault(SpellcastingComponentTypes.ANIMATION_SPEED, 1f));
        }
    }

    @Override
    public SkillWheelInfo buildSpellWheelInfo(CastContext castContext, SkillSelectionManager.SelectionOption selectionOption) {
        SkillWheelInfo info = new SkillWheelInfo();
        info.leftText().add(
                Component.translatable("tooltip.skillcasting.level", TooltipsUtils.getLevelNumberComponent(selectionOption.skillData, castContext))
                        .withStyle(this.getRarity(castContext.getSkillLevel()).getDisplayName().getStyle())
        );
        TooltipsUtils.getManaCostComponent(castContext).ifPresent(
                component -> info.leftText().add(component.withStyle(ChatFormatting.AQUA))
        );
        TooltipsUtils.getCooldownComponent(castContext).ifPresent(
                component -> info.leftText().add(component.withStyle(ChatFormatting.YELLOW))
        );
        getUniqueInfo(castContext).forEach(component -> info.rightText().add(component.withStyle(Style.EMPTY.withColor(0x3be33b))));
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

    public boolean canBeInterrupted(@Nullable LivingEntity livingEntity) {
        return this.getCastType() == CastType.LONG && !ItemRegistry.CONCENTRATION_AMULET.get().isEquippedBy(livingEntity);
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
        if (livingEntity == null) {
            return 1f;
        }
        return getSpellPowerMultiplier(SkillcastingManager.buildCastContext(CasterRef.entity(livingEntity), holder(), 0, CastSource.EMPTY));
    }

    public void resetRarityWeights() {
        rarityWeights = null;
    }

    private void initializeRarityWeights() {
        synchronized (this) {
            if (rarityWeights == null) {
                int minRarity = getMinRarity().getValue();
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
        int minLevel = getMinLevel();
        int maxRarity = getMaxRarity();
        if (maxLevel <= minLevel) {
            return getMinRarity();
        }
        if (level >= maxLevel) {
            return SpellRarity.LEGENDARY;
        }
        double percentOfMaxLevel = Math.max(0d, (double) (level - minLevel) / (double) (maxLevel - minLevel));

        int lookupOffset = maxRarity + 1 - rarityWeights.size();

        for (int i = 0; i < rarityWeights.size(); i++) {
            if (percentOfMaxLevel <= rarityWeights.get(i)) {
                return SpellRarity.values()[i + lookupOffset];
            }
        }

        return SpellRarity.COMMON;
    }

    @Override
    public int getMaxLevel() {
        return SpellConfigManager.getSpellConfigValue(this, SpellConfigParameter.MAX_LEVEL);
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


    public SpellRarity getMinRarity() {
        return SpellConfigManager.getSpellConfigValue(this, SpellConfigParameter.MIN_RARITY);
    }

    public boolean allowCrafting() {
        return SpellConfigManager.getSpellConfigValue(this, SpellConfigParameter.ALLOW_CRAFTING);
    }

    public int getMinLevelForRarity(SpellRarity rarity) {
        if (rarityWeights == null) {
            initializeRarityWeights();
        }

        int minRarity = getMinRarity().getValue();
        int maxLevel = getMaxLevel();
        if (rarity.getValue() < minRarity) {
            return 0;
        }

        if (rarity.getValue() == minRarity) {
            return 1;
        }

        return (int) (rarityWeights.get(rarity.getValue() - (1 + minRarity)) * maxLevel) + 1;
    }

    @Override
    public void setupAIContext(CastContext castContext, Mob mob) {
        super.setupAIContext(castContext, mob);
        castContext.set(SpellcastingComponentTypes.IGNORE_MANA, Unit.INSTANCE);
        castContext.set(SkillcastingComponentTypes.IGNORE_COOLDOWN, Unit.INSTANCE);
        if (mob.getTarget() != null && !castContext.has(SkillcastingComponentTypes.TARGETED_ENTITIES)) {
            castContext.set(SkillcastingComponentTypes.TARGETED_ENTITIES, new TargetedEntitiesData(mob.getTarget()));
        }
    }
}
