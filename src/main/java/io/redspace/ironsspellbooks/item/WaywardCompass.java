package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.item.WaywardCompassData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaywardCompass extends Item {
    private static final Component description = Component.translatable("item.irons_spellbooks.wayward_compass_desc").withStyle(ChatFormatting.DARK_AQUA);

    public WaywardCompass() {
        super(ItemPropertiesHelper.equipment());
    }

    @Nullable
    public static GlobalPos getCatacombsLocation(Entity entity, ItemStack stack) {
        if (!(entity.level.dimension() == Level.OVERWORLD && stack.has(ComponentRegistry.WAYWARD_COMPASS)))
            return null;

        return GlobalPos.of(entity.level.dimension(), stack.get(ComponentRegistry.WAYWARD_COMPASS).blockPos());
    }

    @Override
    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        findCatacombs(pStack, pLevel, pPlayer);
    }

    private static void findCatacombs(ItemStack pStack, Level pLevel, Player pPlayer) {
        if (pLevel instanceof ServerLevel serverlevel) {
            BlockPos blockpos = serverlevel.findNearestMapStructure(ModTags.WAYWARD_COMPASS_LOCATOR, pPlayer.blockPosition(), 100, false);
            if (blockpos != null) {
                pStack.set(ComponentRegistry.WAYWARD_COMPASS, new WaywardCompassData(blockpos));
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (missingWarning(itemStack)) {
            findCatacombs(itemStack, pLevel, pPlayer);
            pPlayer.getCooldowns().addCooldown(ItemRegistry.WAYWARD_COMPASS.get(), 200);
            return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    public boolean missingWarning(ItemStack itemStack) {
        return !itemStack.has(ComponentRegistry.WAYWARD_COMPASS);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(description);
        if (missingWarning(pStack)) {
            pTooltipComponents.add(Component.translatable("item.irons_spellbooks.wayward_compass.error", Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage()).withStyle(ChatFormatting.RED));
        }
    }
}
