package io.redspace.skillcasting.irons_spellbooks;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class AbstractSpellSkill extends AbstractSkill {

    int baseMana, manaPerLevel;

    public int getBaseManaCost(int spellLevel) {
        return baseMana + manaPerLevel * (spellLevel - 1);
    }

    public int getManaCost(CastContext castContext) {
        if (castContext.has(SpellcastingComponentTypes.IGNORE_MANA)) {
            return 0;
        }
        return castContext.getOrDefault(SpellcastingComponentTypes.MANA_COST, 0);
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SpellcastingComponentTypes.MANA_COST, getBaseManaCost(castContext.getSkillLevel()));
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
}
