package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.network.ClientboundOpenEldritchScreen;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EldritchManuscript extends Item {
    private static final Component description = Component.translatable("item.irons_spellbooks.eldritch_manuscript_desc").withStyle(ChatFormatting.GRAY);

    public EldritchManuscript(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pUsedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayer(new ClientboundOpenEldritchScreen(pUsedHand), serverPlayer);
        }
        return super.use(level, player, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, lines, pIsAdvanced);
        lines.add(description);
    }
}
