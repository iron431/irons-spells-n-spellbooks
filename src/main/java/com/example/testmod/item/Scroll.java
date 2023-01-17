package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.capabilities.scroll.ScrollData;
import com.example.testmod.capabilities.scroll.ScrollDataProvider;
import com.example.testmod.network.PacketCancelCast;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.CastType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Scroll extends Item implements IScroll {

    public static final String PARENT = "Parent";

    public Scroll() {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
    }

    protected void removeScrollAfterCast(ServerPlayer serverPlayer, ItemStack stack) {
        TestMod.LOGGER.debug("removeScrollAfterCast {}", serverPlayer.getName().getString());
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
    }

    public static boolean attemptRemoveScrollAfterCast(ServerPlayer serverPlayer) {
        TestMod.LOGGER.debug("attemptRemoveScrollAfterCast {}", serverPlayer.getName().getString());
        ItemStack potentialScroll = PlayerMagicData.getPlayerMagicData(serverPlayer).getPlayerCastingItem();
        if (potentialScroll.getItem() instanceof Scroll scroll) {
            scroll.removeScrollAfterCast(serverPlayer, potentialScroll);
            return true;
        } else
            return false;
    }

    public ScrollData getScrollData(ItemStack stack) {
        //TestMod.LOGGER.debug("Scroll.getScrollData.1 {}", stack.hashCode());
        return stack.getCapability(ScrollDataProvider.SCROLL_DATA).resolve().get();
    }

    public LazyOptional<ScrollData> getScrollDataProvider(ItemStack stack) {
        //TestMod.LOGGER.debug("Scroll.getScrollData.2 {}", stack.hashCode());
        return stack.getCapability(ScrollDataProvider.SCROLL_DATA);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var spell =  getScrollData(stack).getSpell();

        if (level.isClientSide) {
            if (ClientMagicData.isCasting) {
                return InteractionResultHolder.fail(stack);
            } else {
                spell.onClientPreCast(level, player, hand, null);

                if (spell.getCastType() == CastType.CONTINUOUS) {
                    player.startUsingItem(hand);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        if (spell.attemptInitiateCast(stack, level, player, true, false)) {
            if (spell.getCastType() == CastType.INSTANT) {
                removeScrollAfterCast((ServerPlayer) player, stack);
            }
            return InteractionResultHolder.success(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack) {
        return 7200;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, LivingEntity entity, int ticksUsed) {
        entity.stopUsingItem();
        if (getUseDuration(itemStack) - ticksUsed >= 10)
            Messages.sendToServer(new PacketCancelCast(true));
        super.releaseUsing(itemStack, level, entity, ticksUsed);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        var scrollData = getScrollData(itemStack);
        return scrollData.getDisplayName();

    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, List<Component> lines, @NotNull TooltipFlag flag) {
        lines.addAll(getScrollData(itemStack).getHoverText());
        super.appendHoverText(itemStack, level, lines, flag);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag shareTag = new CompoundTag();
        CompoundTag tag = stack.getTag();
        //TestMod.LOGGER.debug("Scroll.getShareTag.1: {}, {}, {}", spellType, level, tag);
        if (tag != null) {
            shareTag.put("tag", tag);
        }

        getScrollDataProvider(stack).ifPresent(
                (scrollDataProvider) -> {
                    var newNbt = scrollDataProvider.saveNBTData();
                    //TestMod.LOGGER.debug("Scroll.getShareTag.2: {}, {}, {}", spellType, level, newNbt);
                    shareTag.put("cap", newNbt);
                }
        );

        return shareTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            //TestMod.LOGGER.debug("Scroll.readShareTag.1: {}, {}, {}", spellType, level, nbt);
            stack.setTag(nbt.contains("tag") ? nbt.getCompound("tag") : null);
            if (nbt.contains("cap")) {
                getScrollData(stack).loadNBTData(nbt.getCompound("cap"));
            }
        } else {
            //TestMod.LOGGER.debug("Scroll.readShareTag.2: {}, {}", spellType, level);
            stack.setTag(null);
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        var scrollDataProvider = new ScrollDataProvider();

        if (nbt != null) {
            //TestMod.LOGGER.debug("Scroll.initCapabilities.1: {}, {}, {}", spellType, level, nbt);
            scrollDataProvider.deserializeNBT(nbt.getCompound(PARENT));
        } else {
            //TestMod.LOGGER.debug("Scroll.initCapabilities.2: {}, {}", spellType, level);
            scrollDataProvider.getOrCreateScrollData();
        }
        return scrollDataProvider;
    }
}
