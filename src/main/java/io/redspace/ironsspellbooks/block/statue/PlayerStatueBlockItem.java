package io.redspace.ironsspellbooks.block.statue;

import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatueBlockItem extends BlockItem {
    ArrayList<Component> shiftTooltip;

    public PlayerStatueBlockItem(Properties properties) {
        super(BlockRegistry.PLAYER_STATUE_BLOCK.get(), properties);
        this.shiftTooltip = new ArrayList<>();
        shiftTooltip.add(Component.literal(" * ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("block.irons_spellbooks.player_statue.guide.1").withStyle(ChatFormatting.WHITE)));
        shiftTooltip.add(Component.literal(" * ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("block.irons_spellbooks.player_statue.guide.2").withStyle(ChatFormatting.WHITE)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        MutableComponent playername = Component.translatable("block.irons_spellbooks.player_statue.unset_player");
        if (stack.has(ComponentRegistry.STATUE_ITEM_DATA)) {
            var data = stack.get(ComponentRegistry.STATUE_ITEM_DATA);
            var profile = PatreonHandler.profileFromUUID(data.uuid());
            if (profile == PatreonHandler.Profile.NULL) {
                playername = Component.translatable("block.irons_spellbooks.player_statue.unknown_player");
            } else {
                playername = Component.literal(profile.username());
            }
        }
        tooltipComponents.add(playername.withStyle(ChatFormatting.YELLOW));
        TooltipsUtils.addShiftTooltip(tooltipComponents, shiftTooltip);
    }
}
