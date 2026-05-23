package io.redspace.ironsspellbooks.item.consumables;

import io.redspace.ironsspellbooks.mixin.VaultServerDataAccessor;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class TinctureOfForgetfulnessItem extends Item {
    private static final Component DESCRIPTION = Component.translatable("item.irons_spellbooks.tincture_of_forgetfulness.desc").withStyle(ChatFormatting.GRAY);

    public TinctureOfForgetfulnessItem() {
        super(ItemPropertiesHelper.material(16));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @NotNull TooltipContext context, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(DESCRIPTION);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        if (player == null || !(state.getBlock() instanceof VaultBlock)) {
            return super.useOn(context);
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof VaultBlockEntity vaultBlockEntity)) {
            return InteractionResult.PASS;
        }

        VaultServerData serverData = vaultBlockEntity.getServerData();
        if (serverData == null) {
            return InteractionResult.PASS;
        }
        Set<java.util.UUID> rewardedPlayers = ((VaultServerDataAccessor) serverData).irons_spellbooks$getRewardedPlayers();
        boolean removed = rewardedPlayers.remove(player.getUUID());
        if (!removed) {
            return InteractionResult.PASS;
        }

        ((VaultServerDataAccessor) serverData).irons_spellbooks$markChanged();
        vaultBlockEntity.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
        level.playSound(null, pos, SoundEvents.APPLY_EFFECT_BAD_OMEN, SoundSource.BLOCKS, 1f, 1f);
        level.playSound(null, pos, SoundEvents.HONEY_DRINK, SoundSource.BLOCKS, 1f, 1f);
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
