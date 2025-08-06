package io.redspace.ironsspellbooks.api.spells;

import com.google.common.util.concurrent.AtomicDouble;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.SpellSkillDamageSource;
import io.redspace.ironsspellbooks.network.casting.CastingAnimationPacket;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.skillcastingapi.core.CastType;
import io.redspace.skillcastingapi.data.AbstractSkill;
import io.redspace.skillcastingapi.data.ICastContext;
import io.redspace.skillcastingapi.data.context_parameter.ContextParameterMap;
import io.redspace.skillcastingapi.data.context_parameter.DefaultContextParameters;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;
import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.*;

public abstract class AbstractSpellSkill extends AbstractSkill {
    public static final Style ELDRITCH_OBFUSCATED_STYLE = Style.EMPTY.withObfuscated(true).withFont(ResourceLocation.withDefaultNamespace("alt"));

    private String deathMessageId = null;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected float baseSpellPower;
    protected float spellPowerPerLevel;

    public AbstractSpellSkill() {
    }

    public int getMinRarity() {
        return ServerConfigs.getSpellConfig(this).minRarity().getValue();
    }

    public int getMaxLevel() {
        return ServerConfigs.getSpellConfig(this).maxLevel();
    }

    public int getMinLevel() {
        return 1;
    }

    public MutableComponent getDisplayName(Player player) {
        boolean obfuscateName = player != null && this.obfuscateStats(player);
        return Component.translatable(getDescriptionId()).withStyle(obfuscateName ? ELDRITCH_OBFUSCATED_STYLE : Style.EMPTY);
    }


    public abstract DefaultConfig getDefaultConfig();

    public abstract io.redspace.skillcastingapi.core.CastType getCastType();

    public SchoolType getSchoolType() {
        return ServerConfigs.getSpellConfig(this).school();
    }

    public ResourceLocation getIconLocation() {
        return getId().withPrefix("textures/gui/spell_icons/").withSuffix(".png");
    }

    public Vector3f getTargetingColor() {
        return this.getSchoolType().getTargetingColor();
    }

    /**
     * @return Returns total affinity level bonuses equipped by the LivingEntity
     */
    public final int getAffinityBonus(@NotNull LivingEntity caster) {
        return CuriosApi.getCuriosInventory(caster)
                .map(inv -> inv.findCurios(AffinityData::hasAffinityData).stream()
                        .mapToInt(slot -> AffinityData.getAffinityData(slot.stack()).getBonusFor(this)).sum()).orElse(0);
    }

    public int getManaCost(int level) {
        return (int) ((baseManaCost + manaCostPerLevel * (level - 1)) *
                ServerConfigs.getSpellConfig(this).manaMultiplier()
        );
    }

    @Override
    public void buildContextParameters(ICastContext castContext, ContextParameterMap params) {
        super.buildContextParameters(castContext, params);
        if (castContext.getEntity() instanceof LivingEntity caster) {
            params.mutate(DefaultContextParameters.SKILL_LEVEL, level -> level + getAffinityBonus(caster));

            double castTimeModifier = getCastType() != CastType.CONTINUOUS ?
                    2 - Utils.softCapFormula(caster.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION)) :
                    caster.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION);
            params.mutate(DefaultContextParameters.CAST_TIME, castTicks -> Math.round(castTicks * (float) castTimeModifier));

            double playerCooldownModifier = caster.getAttributeValue(COOLDOWN_REDUCTION);
            float itemCoolDownModifer = 1;
            //fixme: figure out cast sources
//            if (castSource == CastSource.SWORD) {
//                itemCoolDownModifer = ServerConfigs.SWORDS_CD_MULTIPLIER.get().floatValue();
//            }
            params.mutate(DefaultContextParameters.COOLDOWN, cdTicks -> Math.round(cdTicks * (2 - (float) Utils.softCapFormula(playerCooldownModifier)) * itemCoolDownModifer));
        }
    }

    @Override
    public void onServerPreCast(ICastContext castContext) {
        super.onServerPreCast(castContext);
        playAnimation(castContext, false, getCastStartAnimation());
    }

    @Override
    public void onServerPostCast(ICastContext castContext, boolean completed) {
        super.onServerPostCast(castContext, completed);
        playAnimation(castContext, !completed, getCastFinishAnimation());
    }

    private void playAnimation(ICastContext castContext, boolean cancel, AnimationHolder animationHolder) {
        //todo: simplify this
        if (animationHolder.isPass && !cancel) {
            return;
        }
        var animation = !cancel ? animationHolder.getForPlayer().orElse(IronsSpellbooks.id("none")) : IronsSpellbooks.id("none");
        var caster = castContext.caster();
        caster.type().handlePacketDistribution((ServerLevel) castContext.getLevel(), caster, new CastingAnimationPacket(caster, animation));
    }

    @Override
    public int getCooldownTicks() {
        return ServerConfigs.getSpellConfig(this).cooldownInTicks();
    }

    @Override
    public Optional<SoundEvent> getOnCastSound() {
        return Optional.of(defaultCastSound());
    }

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public AnimationHolder getCastStartAnimation() {
        return switch (getCastType()) {
            case INSTANT -> ANIMATION_INSTANT_CAST;
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
            case INSTANT -> AnimationHolder.pass();
            default -> AnimationHolder.none();
        };
    }

    public float getSpellPower(ICastContext castContext) {

        var spellLevel = castContext.getSpellLevel();
        double entitySpellPowerModifier = 1;
        double entitySchoolPowerModifier = 1;

        float configPowerModifier = (float) ServerConfigs.getSpellConfig(this).powerMultiplier();
        if (castContext.getEntity() instanceof LivingEntity livingEntity) {
            entitySpellPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER);
            entitySchoolPowerModifier = this.getSchoolType().getPowerFor(livingEntity);
        }

        return (float) ((baseSpellPower + spellPowerPerLevel * (spellLevel - 1)) * entitySpellPowerModifier * entitySchoolPowerModifier * configPowerModifier);
    }

    public float getEntityPowerMultiplier(@Nullable LivingEntity entity) {
        float base = (float) ServerConfigs.getSpellConfig(this).powerMultiplier();
        if (entity == null) {
            return base;
        }
        var entitySpellPowerModifier = (float) entity.getAttributeValue(AttributeRegistry.SPELL_POWER);
        var entitySchoolPowerModifier = this.getSchoolType().getPowerFor(entity);
        return (float) (base * entitySpellPowerModifier * entitySchoolPowerModifier);
    }

    @Override
    public void castSkill(ICastContext castContext) {
        super.castSkill(castContext);
        //todo: mana cost
//        if (castSource.consumesMana() && !playerAlreadyHasRecast && !(serverPlayer.isCreative() && !ServerConfigs.CREATIVE_MANA_COST.get())) {
//            var newMana = Math.max(magicData.getMana() - event.getManaCost(), 0);
//            magicData.setMana(newMana);
//            PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(magicData));
//        }
    }

    //todo: figure out castsource
//    @Override
//    public io.redspace.skillcastingapi.data.CastResult canBeCastedBy(ServerPlayer player, ICastContext castContext) {
//        if (ServerConfigs.DISABLE_ADVENTURE_MODE_CASTING.get()) {
//            if (player instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
//                return new io.redspace.skillcastingapi.data.CastResult(io.redspace.skillcastingapi.data.CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_adventure").withStyle(ChatFormatting.RED));
//            }
//        }
//        var playerMana = playerMagicData.getMana();
//
//        boolean hasEnoughMana = playerMana - getManaCost(spellLevel) >= 0;
//        boolean isSpellOnCooldown = playerMagicData.getPlayerCooldowns().isOnCooldown(this);
//        boolean hasRecastForSpell = playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId());
//        if (requiresLearning() && !isLearned(player)) {
//            return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_unlearned").withStyle(ChatFormatting.RED));
//        } else if (castSource == CastSource.SCROLL && this.getRecastCount(spellLevel, player) > 0) {
//            return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_scroll", getDisplayName(player)).withStyle(ChatFormatting.RED));
//        } else if ((castSource == CastSource.SPELLBOOK || castSource == CastSource.SWORD) && isSpellOnCooldown && !(player.isCreative() && !ServerConfigs.CREATIVE_COOLDOWN.get())) {
//            return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_cooldown", getDisplayName(player)).withStyle(ChatFormatting.RED));
//        } else if (!hasRecastForSpell && castSource.consumesMana() && !hasEnoughMana && !(player.isCreative() && !ServerConfigs.CREATIVE_MANA_COST.get())) {
//            return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_mana", getDisplayName(player)).withStyle(ChatFormatting.RED));
//        } else {
//            return new CastResult(CastResult.Type.SUCCESS);
//        }
//    }

    private SoundEvent defaultCastSound() {
        return this.getSchoolType().getCastSound();
    }

    /**
     * Used by AbstractSpellCastingMob to determine if the cast is no longer valid (ie player out of range of a particular spell). Override to create spell-specific criteria
     */
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        return false;
    }

    public List<MutableComponent> getUniqueInfo(ICastContext castContext) {
        return List.of();
    }

    private volatile List<Double> rarityWeights;

    private void initializeRarityWeights() {
        synchronized (SpellRegistry.none()) {
            if (rarityWeights == null) {
                int minRarity = getMinRarity();
                int maxRarity = getMaxRarity();
                List<Double> rarityRawConfig = SpellRarity.getRawRarityConfig();
                List<Double> rarityConfig = SpellRarity.getRarityConfig();
                //IronsSpellbooks.LOGGER.debug("rarityRawConfig: {} rarityConfig:{}, {}, {}", rarityRawConfig.size(), rarityConfig.size(), this.hashCode(), this.name());

                List<Double> rarityRawWeights;
                if (minRarity != 0) {
                    //Must balance remaining weights

                    var subList = rarityRawConfig.subList(minRarity, maxRarity + 1);
                    double subtotal = subList.stream().reduce(0d, Double::sum);
                    rarityRawWeights = subList.stream().map(item -> ((item / subtotal) * (1 - subtotal)) + item).toList();

                    var counter = new AtomicDouble();
                    rarityWeights = new ArrayList<>();
                    rarityRawWeights.forEach(item -> {
                        rarityWeights.add(counter.addAndGet(item));
                    });
                } else {
                    //rarityRawWeights = rarityRawConfig;
                    rarityWeights = rarityConfig;
                }
            }
        }
    }

    private final int maxRarity = SpellRarity.LEGENDARY.getValue();

    public SpellRarity getRarity(int level) {
        if (rarityWeights == null) {
            initializeRarityWeights();
        }

        int maxLevel = getMaxLevel();
        int maxRarity = getMaxRarity();
        if (maxLevel == 1)
            return SpellRarity.values()[getMinRarity()];
        if (level >= maxLevel) {
            return SpellRarity.LEGENDARY;
        }
        double percentOfMaxLevel = (double) level / (double) maxLevel;

        //irons_spellbooks.LOGGER.debug("getRarity: {} {} {} {} {} {}", this.toString(), rarityRawWeights, rarityWeights, percentOfMaxLevel, minRarity, maxRarity);

        int lookupOffset = maxRarity + 1 - rarityWeights.size();

        for (int i = 0; i < rarityWeights.size(); i++) {
            if (percentOfMaxLevel <= rarityWeights.get(i)) {
                return SpellRarity.values()[i + lookupOffset];
            }
        }

        return SpellRarity.COMMON;
    }

    public String getDeathMessageId() {
        if (deathMessageId == null) {
            deathMessageId = getId().toString().replace(':', '.');
        }

        return deathMessageId;
    }

    public final SpellSkillDamageSource getDamageSource(Entity attacker) {
        return getDamageSource(attacker, attacker);
    }

    public SpellSkillDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return SpellSkillDamageSource.source(projectile, attacker, this);
    }

    public boolean isEnabled() {
        return ServerConfigs.getSpellConfig(this).enabled();
    }

    public int getMaxRarity() {
        return maxRarity;
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

    /**
     * Returns whether this spell can be generated from random loot when no other criteria are specified
     */
    public boolean allowLooting() {
        return this.getSchoolType().allowLooting;
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
    public boolean allowCrafting() {
        return ServerConfigs.getSpellConfig(this).allowCrafting();
    }

    public boolean obfuscateStats(@Nullable Player player) {
        return requiresLearning() && !isLearned(player);
    }

    public boolean isLearned(@Nullable Player player) {
        return false; // todo: magic data port
//        if (player == null) {
//            return false;
//        } else if (player.level.isClientSide) {
//            return ClientMagicData.getSyncedSpellData(player).isSpellLearned(this);
//        } else {
//            return MagicData.getPlayerMagicData(player).getSyncedData().isSpellLearned(this);
//        }
    }

    public boolean requiresLearning() {
        return this.getSchoolType().requiresLearning;
    }

    public boolean canBeInterrupted(@Nullable Player player) {
        return this.getCastType() == CastType.LONG && !ItemRegistry.CONCENTRATION_AMULET.get().isEquippedBy(player);
    }

    public boolean stopSoundOnCancel() {
        return false;
    }
}
