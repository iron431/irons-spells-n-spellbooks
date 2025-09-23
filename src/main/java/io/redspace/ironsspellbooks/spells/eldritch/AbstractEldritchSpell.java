package io.redspace.ironsspellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Legacy Eldritch Spell class. Looting and crafting status are now controlled dynamically by the school, and do not need to be hardcoded.
 * Learn status is also now integrated directly to all spells. Switch to school-based mechanics by using AbstractSpell directly
 */
@Deprecated(forRemoval = true)
public abstract class AbstractEldritchSpell extends AbstractSpell {

    public static final Style ELDRITCH_OBFUSCATED_STYLE = Style.EMPTY.withObfuscated(true).withFont(ResourceLocation.withDefaultNamespace("alt"));

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
        if (player == null) {
            return false;
        } else if (player.level.isClientSide) {
            return ClientMagicData.getSyncedSpellData(player).isSpellLearned(this);
        } else {
            return MagicData.getPlayerMagicData(player).getSyncedData().isSpellLearned(this);
        }
    }

    @Override
    public boolean requiresLearning() {
        return true;
    }
}
