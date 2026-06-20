package io.redspace.skillcasting.irons_spellbooks;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastResult;
import io.redspace.skillcasting.data.PlayableSound;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

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
        // fixme: i think we might actually just need to save the multipliers. or what else happens to damage?
        //  how do spells make custom damage/power formulas?
        castContext.set(SpellcastingComponentTypes.SPELL_POWER, baseSpellPower + spellPowerPerLevel * scaledLevel);
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
        return Optional.of(PlayableSound.of(getSchoolType().getCastSound(), 2f, 0.9f, 1.1f));
    }

    @Override
    public int getCooldownTicks() {
        // fixme: full skill takeover
        return (int) (getDefaultConfig().cooldownInSeconds * 20);
    }

    public SchoolType getSchoolType() {
        // fixme: full skill takeover
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
}
