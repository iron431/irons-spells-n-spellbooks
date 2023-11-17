package io.redspace.ironsspellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public abstract class AbstractEldritchSpell extends AbstractSpell {

    public static final Style ELDRITCH_OBFUSCATED_STYLE = Style.EMPTY.withObfuscated(true).withFont(new ResourceLocation("alt"));

    //TODO: make sure clientbound targeting notifications are passing in the correct player
    @Override
    public MutableComponent getDisplayName(Player player) {
        boolean obfuscateName = player != null && this.obfuscateStats(player);
        return super.getDisplayName(player).withStyle(obfuscateName ? ELDRITCH_OBFUSCATED_STYLE : Style.EMPTY);
    }

    @Override
    public boolean allowLooting() {
        return false;
    }

    @Override
    public boolean canBeCraftedBy(Player player) {
        return isLearned(player);
    }

    @Override
    public boolean obfuscateStats(Player player) {
        return !isLearned(player);
    }

    @Override
    public CastResult canBeCastedBy(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player) {
        if (!isLearned(player)) {
            return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_unlearned").withStyle(ChatFormatting.RED));
        }
        return super.canBeCastedBy(spellLevel, castSource, playerMagicData, player);
    }

    @Override
    public boolean isLearned(Player player) {
        if (player.level.isClientSide) {
            return ClientMagicData.getSyncedSpellData(player).isSpellLearned(this);
        } else {
            return MagicData.getPlayerMagicData(player).getSyncedData().isSpellLearned(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ResourceLocation getSpellIconResource() {
        if (Minecraft.getInstance().player != null && isLearned(Minecraft.getInstance().player)) {
            return new ResourceLocation(getSpellResource().getNamespace(), "textures/gui/spell_icons/" + getSpellName() + ".png");
        } else {
            return SpellRegistry.none().getSpellIconResource();
        }
    }
}
