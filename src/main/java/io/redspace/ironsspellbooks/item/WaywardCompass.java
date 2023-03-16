package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaywardCompass extends Item {

    public WaywardCompass() {
        super(new Properties().tab(CreativeModeTab.TAB_TOOLS));
    }

    public static GlobalPos getCatacombsLocation(Entity entity, CompoundTag compoundTag) {
        if (!(entity.level.dimensionType().natural() && compoundTag.contains("CatacombsPos")))
            return null;

        return GlobalPos.of(entity.level.dimension(), NbtUtils.readBlockPos(compoundTag.getCompound("CatacombsPos")));
    }

    @Override
    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        if (pLevel instanceof ServerLevel serverlevel) {
            BlockPos blockpos = serverlevel.findNearestMapStructure(ModTags.WAYWARD_COMPASS_LOCATOR, pPlayer.blockPosition(), 100, false);
            if (blockpos != null) {
                var tag = pStack.getOrCreateTag();
                tag.put("CatacombsPos", NbtUtils.writeBlockPos(blockpos));

            }

        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.irons_spellbooks.wayward_compass_desc").withStyle(ChatFormatting.DARK_AQUA));
    }
}
