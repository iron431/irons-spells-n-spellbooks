package io.redspace.ironsspellbooks.item.consumables;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class DrinkableItem extends Item {
    public DrinkableItem(Properties pProperties, BiConsumer<ItemStack, LivingEntity> drinkAction, @Nullable Item returnItem, boolean showDescription) {
        super(pProperties);
        this.drinkAction = drinkAction;
        this.returnItem = returnItem;
        this.showDesc = showDescription;
    }

    public DrinkableItem(Properties pProperties, BiConsumer<ItemStack, LivingEntity> drinkAction) {
        this(pProperties, drinkAction, null, true);
    }

    private final BiConsumer<ItemStack, LivingEntity> drinkAction;
    private final Item returnItem;
    private final boolean showDesc;

    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
        Player player = pEntityLiving instanceof Player ? (Player) pEntityLiving : null;
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, pStack);
        }

        if (!pLevel.isClientSide) {
            this.drinkAction.accept(pStack, pEntityLiving);
        }

        if (player != null && !player.getAbilities().instabuild) {
            pStack.shrink(1);
        }

        if (returnItem != null && (player == null || !player.getAbilities().instabuild)) {
            if (pStack.isEmpty()) {
                return new ItemStack(returnItem);
            }

            if (player != null) {
                player.getInventory().add(new ItemStack(returnItem));
            }
        }

        pEntityLiving.gameEvent(GameEvent.DRINK);
        return pStack;
    }

    public int getUseDuration(ItemStack pStack) {
        return 32;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.DRINK;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @org.jetbrains.annotations.Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if (showDesc) {
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            pTooltipComponents.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.BLUE));
        }
    }
}
